package jenkins.plugins.horreum.upload;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;

import hudson.CloseProofOutputStream;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.RemoteOutputStream;
import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.entity.json.Access;
import io.hyperfoil.tools.yaup.json.Json;
import jenkins.plugins.horreum.HorreumGlobalConfig;
import jenkins.plugins.horreum.auth.KeycloakAuthentication;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;
import jenkins.security.MasterToSlaveCallable;

public class HorreumUploadExecutionContext extends MasterToSlaveCallable<String, RuntimeException> {

	private static final long serialVersionUID = -2066857816168989599L;
	private final String url;

	private final Map<String, HttpRequestNameValuePair> params;

	private final FilePath uploadFile;

	private final KeycloakAuthentication keycloak;

	private final OutputStream remoteLogger;
	private transient PrintStream localLogger;
	private boolean abortOnFailure;

	static HorreumUploadExecutionContext from(HorreumUploadConfig config,
											  EnvVars envVars, TaskListener listener, Supplier<FilePath> filePathSupplier) {
		String url = envVars != null ? envVars.expand(HorreumGlobalConfig.get().getBaseUrl()) : HorreumGlobalConfig.get().getBaseUrl(); //http.resolveUrl(envVars, build, taskListener);
		List<HttpRequestNameValuePair> params = config.resolveParams(); //Need to define params in freestyle project
		FilePath uploadFile = filePathSupplier.get();
		TaskListener taskListener = config.getQuiet() ? TaskListener.NULL : listener;

		HorreumUploadExecutionContext context = new HorreumUploadExecutionContext(
				url,
				config.getAbortOnFailure(),
				params,
				uploadFile,
				taskListener.getLogger());
		context.keycloak.resolveCredentials();
		return context;
	}

	private HorreumUploadExecutionContext(
			String url, boolean abortOnFailure,
			List<HttpRequestNameValuePair> params,
			FilePath uploadFile,
			PrintStream logger
	) {
		this.url = url;
		this.abortOnFailure = abortOnFailure;

		this.params = new HashMap<>();

		params.forEach(param -> this.params.put(param.getName(), param));

		keycloak = HorreumGlobalConfig.get().getAuthentication();

		this.uploadFile = uploadFile;

		this.localLogger = logger;
		this.remoteLogger = new RemoteOutputStream(new CloseProofOutputStream(logger));
	}

	@Override
	public String call() throws RuntimeException {
		logger().println("URL: " + url);

		HorreumClient.Builder clientBuilder = new HorreumClient.Builder()
				.horreumUrl(url)
				.keycloakUrl(keycloak.getKeycloakBaseUrl())
				.keycloakRealm(keycloak.getKeycloakRealm())
				.clientId(keycloak.getClientId())
				.horreumUser(keycloak.getUsername())
				.horreumPassword(keycloak.getPassword());


		HorreumClient client = clientBuilder.build();

		Json json = Json.fromFile(uploadFile.getRemote());

		String schema = params.get("schema").getValue();
		try {
			String id = client.runService.addRunFromData(
					params.get("start").getValue(),
					params.get("stop").getValue(),
					params.get("test").getValue(),
					params.get("owner").getValue(),
					Access.valueOf(params.get("access").getValue()),
					null,
					"".equals(schema) ? null : schema,
					null,
					json
			);
			logger().printf("Uploaded run ID: %s%n", id);
			return id;
		} catch (WebApplicationException e) {
			logger().printf("Request failed with status %d, message: %s", e.getResponse().getStatus(), e.getResponse().getEntity());
			throw e;
		}
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

}
