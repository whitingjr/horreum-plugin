package jenkins.plugins.horreum.upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

public class HorreumUploadConfig {

	private @Nonnull String test;
	private @Nonnull String owner;
	private @Nonnull String access;
	private @Nonnull String start;
	private @Nonnull String stop;
	private @Nonnull String schema;
	private @Nonnull String jsonFile;

	private Boolean quiet                     = HorreumUploadStep.DescriptorImpl.quiet;
	private Boolean abortOnFailure            = HorreumUploadStep.DescriptorImpl.abortOnFailure;

	private String keycloakRealm;
	private String clientId;
	private String credentialsID;

	public HorreumUploadConfig(@Nonnull String test, @Nonnull String owner, @Nonnull String access, @Nonnull String start, @Nonnull String stop, @Nonnull String schema, @Nonnull String jsonFile) {
		this.test = Objects.requireNonNull(test);
		this.owner = Objects.requireNonNull(owner);
		this.access = Objects.requireNonNull(access);
		this.start = Objects.requireNonNull(start);
		this.stop = Objects.requireNonNull(stop);
		this.schema = Objects.requireNonNull(schema);
		this.jsonFile = Objects.requireNonNull(jsonFile);
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
	public String getStart() {
		return start;
	}

	public void setStart(@Nonnull String start) {
		this.start = start;
	}

	@Nonnull
	public String getStop() {
		return stop;
	}

	public void setStop(@Nonnull String stop) {
		this.stop = stop;
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

	public Boolean getQuiet() {
		return quiet;
	}

	public void setQuiet(@Nonnull Boolean quiet) {
		this.quiet = quiet;
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

	public String getHorreumCredentialsID() {
		return credentialsID;
	}

	public void setHorreumCredentialsID(String credentialsID) {
		this.credentialsID = credentialsID;
	}

	FilePath resolveUploadFile(EnvVars envVars, AbstractBuild<?,?> build) {
		if (jsonFile.trim().isEmpty()) {
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
		params.add(new HttpRequestNameValuePair("owner",this.owner));
		params.add(new HttpRequestNameValuePair("access",this.access));
		params.add(new HttpRequestNameValuePair("start",this.start));
		params.add(new HttpRequestNameValuePair("stop",this.stop));
		params.add(new HttpRequestNameValuePair("schema", this.schema));
		return params;
	}
}
