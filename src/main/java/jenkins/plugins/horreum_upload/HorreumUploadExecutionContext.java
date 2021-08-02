package jenkins.plugins.horreum_upload;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.microprofile.client.impl.MpClientBuilderImpl;

import hudson.CloseProofOutputStream;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.RemoteOutputStream;
import io.hyperfoil.tools.yaup.json.Json;
import jenkins.plugins.horreum_upload.api.HorreumRunService;
import jenkins.plugins.horreum_upload.api.dto.Access;
import jenkins.plugins.horreum_upload.auth.KeycloakAuthentication;
import jenkins.plugins.horreum_upload.auth.KeycloakClientRequestFilter;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;
import jenkins.security.MasterToSlaveCallable;

public class HorreumUploadExecutionContext extends MasterToSlaveCallable<ResponseContentSupplier, RuntimeException> {

	private static final long serialVersionUID = -2066857816168989599L;
	private final String url;

	private final Map<String, HttpRequestNameValuePair> params;

	private final FilePath uploadFile;

	private final boolean consoleLogResponseBody;

	private final KeycloakAuthentication keycloakAuthentication;

	private final OutputStream remoteLogger;
	private transient PrintStream localLogger;
	private boolean abortOnFailure;

	static HorreumUploadExecutionContext from(HorreumUploadConfig config,
											  EnvVars envVars, TaskListener taskListener, Supplier<FilePath> filePathSupplier) {
		String url = envVars != null ? envVars.expand(HorreumUploadGlobalConfig.get().getBaseUrl()) : HorreumUploadGlobalConfig.get().getBaseUrl(); //http.resolveUrl(envVars, build, taskListener);
		List<HttpRequestNameValuePair> params = config.resolveParams(); //Need to define params in freestyle project
		FilePath uploadFile = filePathSupplier.get();

		return new HorreumUploadExecutionContext(
				url,
				config.getAbortOnFailure(),
				params,
				uploadFile,
				config.getConsoleLogResponseBody(),
				taskListener.getLogger());
	}

	private HorreumUploadExecutionContext(
			String url, boolean abortOnFailure,
			List<HttpRequestNameValuePair> params,
			FilePath uploadFile,
			Boolean consoleLogResponseBody,
			PrintStream logger
	) {
		this.url = url;
		this.abortOnFailure = abortOnFailure;

		this.params = new HashMap<>();

		params.forEach(param -> this.params.put(param.getName(), param));

		this.keycloakAuthentication = HorreumUploadGlobalConfig.get().getAuthentication();
		this.uploadFile = uploadFile;
		this.consoleLogResponseBody = Boolean.TRUE.equals(consoleLogResponseBody);

		this.localLogger = logger;
		this.remoteLogger = new RemoteOutputStream(new CloseProofOutputStream(logger));
	}

	@Override
	public ResponseContentSupplier call() throws RuntimeException {
		logger().println("URL: " + url);

		MpClientBuilderImpl clientBuilder = new MpClientBuilderImpl();

		clientBuilder.register(KeycloakClientRequestFilter.class);

		ResteasyClient client = clientBuilder.build();
		ResteasyWebTarget target = client.target(UriBuilder.fromPath(url));

		HorreumRunService horreumRunService = target.proxyBuilder(HorreumRunService.class).build();

		Json json = Json.fromFile(uploadFile.getRemote());

		ClientResponse response = horreumRunService.addRunFromData(
				params.get("start").getValue(),
				params.get("stop").getValue(),
				params.get("test").getValue(),
				params.get("owner").getValue(),
				Access.valueOf(params.get("access").getValue()),
				"".equals(params.get("schema").getValue()) ? null : params.get("schema").getValue(),
				null,
				null,
				json.toString()
		);

		ResponseContentSupplier responseContentSupplier = new ResponseContentSupplier(response.getEntity() != null ? response.getEntity().toString() : "", response.getStatus());

		processResponse(responseContentSupplier);

		return responseContentSupplier;
	}

	private PrintStream logger() {
		if (localLogger == null) {
			try {
				localLogger = new PrintStream(remoteLogger, true, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}
		return localLogger;
	}

	private void processResponse(ResponseContentSupplier response) {
		//logs
		if (consoleLogResponseBody) {
			logger().println("Response: \n" + response.getContent());
		}
		if ((response.getStatus()) >= 400) {
			logger().println("Request Failed: \n" + response.getContent());
		}

		responseCodeIsValid(response);

	}

	private void responseCodeIsValid(ResponseContentSupplier response) throws RuntimeException {
		//TODO: enable configuration
		if ((response.getStatus()) < 400 && abortOnFailure) {
			return;
		}
		throw new RuntimeException("Fail: the returned code " + response.getStatus() + " is not in the accepted range");
	}

	public void initialiseContext() {
		keycloakAuthentication.resolveCredentials();
	}
}
