package jenkins.plugins.horreum_upload;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.http.HttpHeaders;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;

public class HorreumUploadConfig {

	private @Nonnull String test;
	private @Nonnull String owner;
	private @Nonnull String access;
	private @Nonnull String startAccessor;
	private @Nonnull String stopAccessor;
	private @Nonnull String schema;
	private @Nonnull String jsonFile;

	private boolean abortOnFailure = HorreumUploadStep.DescriptorImpl.abortOnFailure;
	private String validResponseCodes         = HorreumUploadStep.DescriptorImpl.validResponseCodes;
	private String validResponseContent       = HorreumUploadStep.DescriptorImpl.validResponseContent;
	private Boolean consoleLogResponseBody    = HorreumUploadStep.DescriptorImpl.consoleLogResponseBody;
	private Boolean quiet                     = HorreumUploadStep.DescriptorImpl.quiet;
	private String authentication             = HorreumUploadStep.DescriptorImpl.authentication;
	private List<HttpRequestNameValuePair> customHeaders = HorreumUploadStep.DescriptorImpl.customHeaders;

	private String keycloakRealm;
	private String clientId;
	private String horreumClientSecretID;
	private String HorreumCredentialsID;

	public HorreumUploadConfig(@Nonnull String test, @Nonnull String owner, @Nonnull String access, @Nonnull String startAccessor, @Nonnull String stopAccessor, @Nonnull String schema, @Nonnull String jsonFile) {
		this.test = test;
		this.owner = owner;
		this.access = access;
		this.startAccessor = startAccessor;
		this.stopAccessor = stopAccessor;
		this.schema = schema;
		this.jsonFile = jsonFile;
	}

	@Nonnull
	public String getTest() {
		return test;
	}

	public void setTest(@Nonnull String test) {
		this.test = test;
	}

	@Nonnull
	public String getOwner() {
		return owner;
	}

	public void setOwner(@Nonnull String owner) {
		this.owner = owner;
	}

	@Nonnull
	public String getAccess() {
		return access;
	}

	public void setAccess(@Nonnull String access) {
		this.access = access;
	}

	@Nonnull
	public String getStartAccessor() {
		return startAccessor;
	}

	public void setStartAccessor(@Nonnull String startAccessor) {
		this.startAccessor = startAccessor;
	}

	@Nonnull
	public String getStopAccessor() {
		return stopAccessor;
	}

	public void setStopAccessor(@Nonnull String stopAccessor) {
		this.stopAccessor = stopAccessor;
	}

	@Nonnull
	public String getSchema() {
		return schema;
	}

	public void setSchema(@Nonnull String schema) {
		this.schema = schema;
	}

	@Nonnull
	public String getJsonFile() {
		return jsonFile;
	}

	public void setJsonFile(@Nonnull String jsonFile) {
		this.jsonFile = jsonFile;
	}

	public boolean getAbortOnFailure() {
		return abortOnFailure;
	}

	public void setAbortOnFailure(boolean abortOnFailure) {
		this.abortOnFailure = abortOnFailure;
	}

	public String getValidResponseCodes() {
		return validResponseCodes;
	}

	public void setValidResponseCodes(String validResponseCodes) {
		this.validResponseCodes = validResponseCodes;
	}

	public String getValidResponseContent() {
		return validResponseContent;
	}

	public void setValidResponseContent(String validResponseContent) {
		this.validResponseContent = validResponseContent;
	}

	public Boolean getConsoleLogResponseBody() {
		return consoleLogResponseBody;
	}

	public void setConsoleLogResponseBody(Boolean consoleLogResponseBody) {
		this.consoleLogResponseBody = consoleLogResponseBody;
	}

	public Boolean getQuiet() {
		return quiet;
	}

	public void setQuiet(Boolean quiet) {
		this.quiet = quiet;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	public List<HttpRequestNameValuePair> getCustomHeaders() {
		return customHeaders;
	}

	public void setCustomHeaders(List<HttpRequestNameValuePair> customHeaders) {
		this.customHeaders = customHeaders;
	}


	//TODO:: abstract away keycloak specific config
	public String getKeycloakRealm() {
		return keycloakRealm;
	}

	public void setKeycloakRealm(String keycloakRealm) {
		this.keycloakRealm = keycloakRealm;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getHorreumClientSecretID() {
		return horreumClientSecretID;
	}

	public void setHorreumClientSecretID(String horreumClientSecretID) {
		this.horreumClientSecretID = horreumClientSecretID;
	}

	public String getHorreumCredentialsID() {
		return HorreumCredentialsID;
	}

	public void setHorreumCredentialsID(String horreumCredentialsID) {
		HorreumCredentialsID = horreumCredentialsID;
	}


	FilePath resolveUploadFile(EnvVars envVars, AbstractBuild<?,?> build) {
		if (jsonFile == null || jsonFile.trim().isEmpty()) {
			return null;
		}
		String filePath = envVars.expand(jsonFile);
		try {
			FilePath workspace = build.getWorkspace();
			if (workspace == null) {
				throw new IllegalStateException("Could not find workspace to check existence of upload file: " + jsonFile +
						". You should use it inside a 'node' block");
			}
			FilePath uploadFilePath = workspace.child(filePath);
			if (!uploadFilePath.exists()) {
				throw new IllegalStateException("Could not find upload file: " + jsonFile);
			}
			return uploadFilePath;
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	public List<HttpRequestNameValuePair> resolveParams() {
		List<HttpRequestNameValuePair> params = new ArrayList<>();
		params.add(new HttpRequestNameValuePair("test",this.test));
		if (this.owner != null) {
			params.add(new HttpRequestNameValuePair("owner",this.owner));
		}
		params.add(new HttpRequestNameValuePair("access",this.access));
		params.add(new HttpRequestNameValuePair("start",this.startAccessor));
		params.add(new HttpRequestNameValuePair("stop",this.stopAccessor));
		if (this.schema != null) {
			params.add(new HttpRequestNameValuePair("schema", this.schema));
		}
		return params;
	}
}
