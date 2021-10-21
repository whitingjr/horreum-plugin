package jenkins.plugins.horreum.upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.misc.NotNull;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.common.AbstractIdCredentialsListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Items;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.plugins.horreum.HorreumGlobalConfig;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

//TODO: Make safe functionality as upload step
public class HorreumUpload extends Builder {

	HorreumUploadConfig config;

	@DataBoundConstructor
	public HorreumUpload(@Nonnull String test,
								@Nonnull String owner,
								@Nonnull String access,
								@Nonnull String start,
								@Nonnull String stop,
								@NotNull String schema,
								@Nonnull String jsonFile) {
		this.config = new HorreumUploadConfig(test, owner, access, start, stop, schema, jsonFile);
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

	@DataBoundSetter
	public void setSchema(String schema) {
		this.config.setSchema(schema);
	}

	public String getSchema() {
		return config.getSchema();
	}

	public String getJsonFile() {
		return config.getJsonFile();
	}

	@DataBoundSetter
	public void setJsonFile(String jsonFile) {
		this.config.setJsonFile(jsonFile);
	}

	@Initializer(before = InitMilestone.PLUGINS_STARTED)
	public static void xStreamCompatibility() {
		Items.XSTREAM2.alias("pair", HttpRequestNameValuePair.class);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		EnvVars envVars = build.getEnvironment(listener);
		for (Map.Entry<String, String> e : build.getBuildVariables().entrySet()) {
			envVars.put(e.getKey(), e.getValue());
		}

		HorreumUploadExecutionContext exec = HorreumUploadExecutionContext.from(config, envVars,
				listener, () -> this.config.resolveUploadFile(envVars, build));

		VirtualChannel channel = launcher.getChannel();
		if (channel == null) {
			throw new IllegalStateException("Launcher doesn't support remoting but it is required");
		}
		channel.call(exec);

		return true;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public static final boolean abortOnFailure = true;
		public static final Boolean quiet = false;
		public static final String jsonFile = "";

		public DescriptorImpl() {
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Horreum Upload";
		}

		public ListBoxModel doFillAuthenticationItems(@AncestorInPath Item project,
													  @QueryParameter String url) {
			return fillAuthenticationItems(project, url);
		}

		public static ListBoxModel fillAuthenticationItems(Item project, String url) {
			if (project == null || !project.hasPermission(Item.CONFIGURE)) {
				return new StandardListBoxModel();
			}

			List<Option> options = new ArrayList<>();

			options.add(new Option(HorreumGlobalConfig.get().getAuthentication().getKeyName()));

			AbstractIdCredentialsListBoxModel<StandardListBoxModel, StandardCredentials> items = new StandardListBoxModel()
					.includeEmptyValue()
					.includeAs(ACL.SYSTEM,
							project, StandardUsernamePasswordCredentials.class,
							URIRequirementBuilder.fromUri(url).build());
			items.addMissing(options);
			return items;
		}

		public ListBoxModel doFillAccessItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
			ListBoxModel items = new ListBoxModel();
			items.add("PUBLIC");
			items.add("PROTECTED");
			items.add("PRIVATE");
			return items;
		}
	}
}
