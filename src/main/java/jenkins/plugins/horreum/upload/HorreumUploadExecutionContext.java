package jenkins.plugins.horreum.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.entity.json.Access;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumGlobalConfig;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

public class HorreumUploadExecutionContext extends BaseExecutionContext<String> {

	private static final long serialVersionUID = -2066857816168989599L;
	private final Map<String, HttpRequestNameValuePair> params;
	private final FilePath uploadFile;

	static HorreumUploadExecutionContext from(HorreumUploadConfig config,
											  EnvVars envVars, TaskListener listener, Supplier<FilePath> filePathSupplier) {
		String url = envVars != null ? envVars.expand(HorreumGlobalConfig.get().getBaseUrl()) : HorreumGlobalConfig.get().getBaseUrl(); //http.resolveUrl(envVars, build, taskListener);
		List<HttpRequestNameValuePair> params = config.resolveParams(); //Need to define params in freestyle project
		FilePath uploadFile = filePathSupplier.get();
		TaskListener taskListener = config.getQuiet() ? TaskListener.NULL : listener;

		HorreumUploadExecutionContext context = new HorreumUploadExecutionContext(
				url,
				params,
				uploadFile,
				taskListener.getLogger());
		context.keycloak.resolveCredentials();
		return context;
	}

	private HorreumUploadExecutionContext(
			String url,
			List<HttpRequestNameValuePair> params,
			FilePath uploadFile,
			PrintStream logger
	) {
		super(url, logger);
		this.params = new HashMap<>();
		params.forEach(param -> this.params.put(param.getName(), param));
		this.uploadFile = uploadFile;
	}

	@Override
	protected String invoke(HorreumClient client) {
		JsonNode json = null;
		try {
			json = new ObjectMapper().readTree(new File(uploadFile.getRemote()));
		} catch (IOException e) {
			throw new RuntimeException("File for upload cannot be read: " + uploadFile.getRemote(), e);
		}
		String schema = params.get("schema").getValue();
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
	}
}
