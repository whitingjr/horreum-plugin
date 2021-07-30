package jenkins.plugins.horreum_upload;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.antlr.v4.runtime.misc.NotNull;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;

public final class HorreumUploadStep extends AbstractStepImpl {

	HorreumUploadConfig config;

	@DataBoundConstructor
	public HorreumUploadStep(@Nonnull String test, @Nonnull String owner,
							 @Nonnull String access, @Nonnull String startAccessor,
							 @Nonnull String stopAccessor, @NotNull String schema, @Nonnull String jsonFile) {
		this.config = new HorreumUploadConfig(test, owner, access, startAccessor, stopAccessor, schema, jsonFile);

		//Populate step config from Global state
		HorreumUploadGlobalConfig globalConfig =  HorreumUploadGlobalConfig.get();
		this.config.setKeycloakRealm(globalConfig.getKeycloakRealm());
		this.config.setHorreumCredentialsID(globalConfig.getCredentialsId());
		this.config.setKeycloakRealm(globalConfig.getKeycloakRealm());
		this.config.setHorreumClientSecretID(globalConfig.getClientSecretId());

	}

	public boolean getAbortOnFailure() {
		return config.getAbortOnFailure();
	}

	@DataBoundSetter
	public void setAbortOnFailure(boolean abortOnFailure) {
		this.config.setAbortOnFailure(abortOnFailure);
	}

	@DataBoundSetter
	public void setValidResponseCodes(String validResponseCodes) {
		this.config.setValidResponseCodes(validResponseCodes);
	}

	public String getValidResponseCodes() {
		return this.config.getValidResponseCodes();
	}

	@DataBoundSetter
	public void setValidResponseContent(String validResponseContent) {
		this.config.setValidResponseContent(validResponseContent);
	}

	public String getValidResponseContent() {
		return config.getValidResponseContent();
	}

	@DataBoundSetter
	public void setConsoleLogResponseBody(Boolean consoleLogResponseBody) {
		this.config.setConsoleLogResponseBody(consoleLogResponseBody);
	}

	public Boolean getConsoleLogResponseBody() {
		return config.getConsoleLogResponseBody();
	}

	@DataBoundSetter
	public void setQuiet(Boolean quiet) {
		this.config.setQuiet(quiet);
	}

	public Boolean getQuiet() {
		return config.getQuiet();
	}

	@DataBoundSetter
	public void setAuthentication(String authentication) {
		this.config.setAuthentication(authentication);
	}

	public String getAuthentication() {
		return config.getAuthentication();
	}

	@DataBoundSetter
	public void setCustomHeaders(List<HttpRequestNameValuePair> customHeaders) {
		this.config.setCustomHeaders(customHeaders);
	}

	public String getTest() {
		return config.getTest();
	}

	@DataBoundSetter
	public void setTest(String test) {
		this.config.setTest(test);
	}

	public String getOwner() {
		return config.getOwner();
	}

	@DataBoundSetter
	public void setOwner(String owner) {
		this.config.setOwner(owner);
	}

	public String getAccess() {
		return this.config.getAccess();
	}

	@DataBoundSetter
	public void setAccess(String access) {
		this.config.setAccess(access);
	}

	public String getStartAccessor() {
		return config.getStartAccessor();
	}

	@DataBoundSetter
	public void setStartAccessor(String startAccessor) {
		this.config.setStartAccessor(startAccessor);
	}

	public String getStopAccessor() {
		return config.getStopAccessor();
	}

	@DataBoundSetter
	public void setSchema(String schema) {
		this.config.setSchema(schema);
	}

	public String getSchema() {
		return config.getSchema();
	}

	@DataBoundSetter
	public void setStopAccessor(String stopAccessor) {
		this.config.setStopAccessor(stopAccessor);
	}

	public List<HttpRequestNameValuePair> getCustomHeaders() {
		return config.getCustomHeaders();
	}

	public String getJsonFile() {
		return config.getJsonFile();
	}

	@DataBoundSetter
	public void setJsonFile(String jsonFile) {
		this.config.setJsonFile(jsonFile);
	}


	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}


	@Extension
	public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
		public static final boolean abortOnFailure = HorreumUpload.DescriptorImpl.abortOnFailure;
		public static final String validResponseCodes = HorreumUpload.DescriptorImpl.validResponseCodes;
		public static final String validResponseContent = HorreumUpload.DescriptorImpl.validResponseContent;
		public static final Boolean consoleLogResponseBody = HorreumUpload.DescriptorImpl.consoleLogResponseBody;
		public static final Boolean quiet = HorreumUpload.DescriptorImpl.quiet;
		public static final String authentication = HorreumUpload.DescriptorImpl.authentication;
		public static final String jsonFile = HorreumUpload.DescriptorImpl.jsonFile;
		public static final List<HttpRequestNameValuePair> customHeaders = Collections.emptyList();

		public DescriptorImpl() {
			super(Execution.class);
		}

		@Override
		public String getFunctionName() {
			return "horreumUpload";
		}

		@Override
		public String getDisplayName() {
			return "Uplaod a JSON object to a Horreum instance";
		}

		public ListBoxModel doFillAuthenticationItems(@AncestorInPath Item project,
													  @QueryParameter String url) {
			return HorreumUpload.DescriptorImpl.fillAuthenticationItems(project, url);
		}

		public ListBoxModel doFillProxyAuthenticationItems(@AncestorInPath Item project,
														   @QueryParameter String url) {
			return HorreumUpload.DescriptorImpl.fillAuthenticationItems(project, url);
		}

		public FormValidation doCheckValidResponseCodes(@QueryParameter String value) {
			return HorreumUpload.DescriptorImpl.checkValidResponseCodes(value);
		}

	}

	public static final class Execution extends AbstractSynchronousNonBlockingStepExecution<ResponseContentSupplier> {

		@Inject
		private transient HorreumUploadStep step;

		@StepContextParameter
		private transient Run<?, ?> run;
		@StepContextParameter
		private transient TaskListener listener;

		@Override
		protected ResponseContentSupplier run() throws Exception {
			HorreumUploadExecutionContext exec = HorreumUploadExecutionContext.from(step.config, null, //TODO:: obtain reference to envVars
					step.getQuiet() ? TaskListener.NULL : listener,
					() -> this.resolveUploadFile());

			exec.initialiseContext();

			Launcher launcher = getContext().get(Launcher.class);
			if (launcher != null) {
				VirtualChannel channel = launcher.getChannel();
				if (channel == null) {
					throw new IllegalStateException("Launcher doesn't support remoting but it is required");
				}
				return channel.call(exec);
			}

			return exec.call();
		}

		private static final long serialVersionUID = 1L;

		FilePath resolveUploadFile() {
			String uploadFile = step.getJsonFile();
			if (uploadFile == null || uploadFile.trim().isEmpty()) {
				return null;
			}

			try {
				FilePath workspace = getContext().get(FilePath.class);
				if (workspace == null) {
					throw new IllegalStateException("Could not find workspace to check existence of upload file: " + uploadFile +
							". You should use it inside a 'node' block");
				}
				FilePath uploadFilePath = workspace.child(uploadFile);
				if (!uploadFilePath.exists()) {
					throw new IllegalStateException("Could not find upload file: " + uploadFile);
				}
				return uploadFilePath;
			} catch (IOException | InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		public Item getProject() {
			return run.getParent();
		}
	}
}
