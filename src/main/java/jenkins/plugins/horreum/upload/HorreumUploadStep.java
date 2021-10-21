package jenkins.plugins.horreum.upload;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

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
import hudson.util.ListBoxModel;
import jenkins.plugins.horreum.HorreumGlobalConfig;

public final class HorreumUploadStep extends AbstractStepImpl {

	HorreumUploadConfig config;

	@DataBoundConstructor
	public HorreumUploadStep(String test,
									 String owner,
							   	 String access,
									 String start,
									 String stop,
									 String schema,
									 String jsonFile) {
		if (test == null || test.isEmpty()) {
			throw new IllegalArgumentException("Test name (or ID) must be set.");
		}
		owner = orEmpty(owner);
		access = orEmpty(access);
		if (start == null || start.isEmpty()) {
			throw new IllegalArgumentException("Start timestamp must be set.");
		}
		if (stop == null || stop.isEmpty()) {
			throw new IllegalArgumentException("Stop timestamp must be set.");
		}
		schema = orEmpty(schema);
		if (jsonFile == null || jsonFile.isEmpty()) {
			throw new IllegalArgumentException("JSON file must be set.");
		}
		this.config = new HorreumUploadConfig(orEmpty(test), owner, access, start, stop, schema, jsonFile);

		//Populate step config from Global state
		HorreumGlobalConfig globalConfig = HorreumGlobalConfig.get();
		this.config.setKeycloakRealm(globalConfig.getKeycloakRealm());
		this.config.setClientId(globalConfig.getClientId());
		this.config.setHorreumCredentialsID(globalConfig.getCredentialsId());
	}

	private @Nonnull String orEmpty(String value) {
		return value == null ? "" : value;
	}

	public boolean getAbortOnFailure() {
		return config.getAbortOnFailure();
	}

	@DataBoundSetter
	public void setAbortOnFailure(boolean abortOnFailure) {
		this.config.setAbortOnFailure(abortOnFailure);
	}

	public Boolean getQuiet() {
		return config.getQuiet();
	}

	@DataBoundSetter
	public void setQuiet(Boolean quiet) {
		this.config.setQuiet(quiet);
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

	public String getStart() {
		return config.getStart();
	}

	@DataBoundSetter
	public void setStart(String start) {
		this.config.setStart(start);
	}

	public String getStop() {
		return config.getStop();
	}

	@DataBoundSetter
	public void setStop(String stop) {
		this.config.setStop(stop);
	}

	public String getSchema() {
		return config.getSchema();
	}

	@DataBoundSetter
	public void setSchema(String schema) {
		this.config.setSchema(schema);
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
		public static final Boolean quiet = HorreumUpload.DescriptorImpl.quiet;
		public static final Boolean abortOnFailure = HorreumUpload.DescriptorImpl.abortOnFailure;
		public static final String jsonFile = HorreumUpload.DescriptorImpl.jsonFile;

		public DescriptorImpl() {
			super(Execution.class);
		}

		@Override
		public String getFunctionName() {
			return "horreumUpload";
		}

		@Override
		public String getDisplayName() {
			return "Upload a JSON object to a Horreum instance";
		}

		public ListBoxModel doFillAuthenticationItems(@AncestorInPath Item project,
													  @QueryParameter String url) {
			return HorreumUpload.DescriptorImpl.fillAuthenticationItems(project, url);
		}
	}

	public static final class Execution extends AbstractSynchronousNonBlockingStepExecution<String> {

		@Inject
		private transient HorreumUploadStep step;

		@StepContextParameter
		private transient Run<?, ?> run;
		@StepContextParameter
		private transient TaskListener listener;

		@Override
		protected String run() throws Exception {
			HorreumUploadExecutionContext exec = HorreumUploadExecutionContext.from(step.config, null, //TODO:: obtain reference to envVars
					listener, this::resolveUploadFile);

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

		private FilePath resolveUploadFile() {
			String uploadFile = step.getJsonFile();
			if (uploadFile.trim().isEmpty()) {
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
