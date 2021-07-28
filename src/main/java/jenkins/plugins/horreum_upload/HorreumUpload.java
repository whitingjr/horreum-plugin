package jenkins.plugins.horreum_upload;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
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
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.plugins.horreum_upload.auth.Authenticator;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;

/**
 * @author Janario Oliveira
 */

//TODO: Make safe functionality as upload step
public class HorreumUpload extends Builder {

	HorreumUploadConfig config;

	@DataBoundConstructor
	public HorreumUpload(@Nonnull String test, @Nonnull String owner,
						 @Nonnull String access, @Nonnull String startAccessor,
						 @Nonnull String stopAccessor, @NotNull String schema, @Nonnull String jsonFile) {
		this.config = new HorreumUploadConfig(test, owner, access, startAccessor, stopAccessor, schema, jsonFile);
	}

	public boolean isIgnoreSslErrors() {
		return config.getIgnoreSslErrors();
	}

	public boolean getAbortOnFailure() {
		return config.getAbortOnFailure();
	}

	@DataBoundSetter
	public void setAbortOnFailure(boolean abortOnFailure) {
		this.config.setAbortOnFailure(abortOnFailure);
	}

	@DataBoundSetter
	public void setIgnoreSslErrors(boolean ignoreSslErrors) {
		this.config.setIgnoreSslErrors(ignoreSslErrors);
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
	public void setContentType(MimeType contentType) {
		this.config.setContentType(contentType);
	}

	public MimeType getContentType() {
		return config.getContentType();
	}

	@DataBoundSetter
	public void setTimeout(Integer timeout) {
		this.config.setTimeout(timeout);
	}

	public Integer getTimeout() {
		return config.getTimeout();
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

	public ResponseHandle getResponseHandle() {
		return config.getResponseHandle();
	}


	@DataBoundSetter
	public void setResponseHandle(ResponseHandle responseHandle) {
		this.config.setResponseHandle(responseHandle);
	}

	public String getJsonFile() {
		return config.getJsonFile();
	}

	@DataBoundSetter
	public void setJsonFile(String jsonFile) {
		this.config.setJsonFile(jsonFile);
	}


//	@Override
//	public HorreumUploadStep.DescriptorImpl getDescriptor() {
//		return (HorreumUploadStep.DescriptorImpl) super.getDescriptor();
//	}

	@Initializer(before = InitMilestone.PLUGINS_STARTED)
	public static void xStreamCompatibility() {
		Items.XSTREAM2.aliasField("logResponseBody", HorreumUpload.class, "consoleLogResponseBody");
		Items.XSTREAM2.aliasField("consoleLogResponseBody", HorreumUpload.class, "consoleLogResponseBody");
		Items.XSTREAM2.alias("pair", HttpRequestNameValuePair.class);
	}

	protected Object readResolve() {
		if (config.getCustomHeaders() == null) {
			config.setCustomHeaders(DescriptorImpl.customHeaders);
		}
//		if (ignoreSslErrors == null) {
//			//default for new job false(DescriptorImpl.ignoreSslErrors) for old ones true to keep same behavior
//			ignoreSslErrors = true;
//		}
		if (config.getQuiet() == null) {
			config.setQuiet(DescriptorImpl.quiet);
		}
		return this;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		EnvVars envVars = build.getEnvironment(listener);
		for (Map.Entry<String, String> e : build.getBuildVariables().entrySet()) {
			envVars.put(e.getKey(), e.getValue());
		}

		HorreumUploadExecutionContext exec = HorreumUploadExecutionContext.from(this.config, envVars,
				this.getQuiet() ? TaskListener.NULL : listener, () -> this.config.resolveUploadFile(envVars, build));

		exec.initialiseContext();

		VirtualChannel channel = launcher.getChannel();
		if (channel == null) {
			throw new IllegalStateException("Launcher doesn't support remoting but it is required");
		}
		channel.call(exec);

		return true;
	}

	public List<HttpRequestNameValuePair> resolveHeaders(EnvVars envVars) {
		return config.resolveHeaders(envVars);
	}

	public FilePath resolveUploadFile(EnvVars envVars, AbstractBuild<?, ?> build) {
		return config.resolveUploadFile(envVars, build);
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public static final boolean ignoreSslErrors = true;
		public static final boolean abortOnFailure = true;
		public static final Boolean passBuildParameters = false;
		public static final String validResponseCodes = "100:399";
		public static final String validResponseContent = "";
		public static final MimeType contentType = MimeType.APPLICATION_JSON;
		public static final int timeout = 0;
		public static final Boolean consoleLogResponseBody = false;
		public static final Boolean quiet = false;
		public static final String authentication = "keycloak";
		public static final String requestBody = "";
		public static final String jsonFile = "";
		public static final List<HttpRequestNameValuePair> customHeaders = Collections.emptyList();

		public DescriptorImpl() {
			load();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Horreum Upload";
		}

		public ListBoxModel doFillHttpModeItems() {
			return HttpMode.getFillItems();
		}

		public ListBoxModel doFillAcceptTypeItems() {
			return MimeType.getContentTypeFillItems();
		}

		public ListBoxModel doFillContentTypeItems() {
			return MimeType.getContentTypeFillItems();
		}

		public ListBoxModel doFillAuthenticationItems(@AncestorInPath Item project,
													  @QueryParameter String url) {
			return fillAuthenticationItems(project, url);
		}

		public ListBoxModel doFillProxyAuthenticationItems(@AncestorInPath Item project,
														   @QueryParameter String url) {
			if (project == null || !project.hasPermission(Item.CONFIGURE)) {
				return new StandardListBoxModel();
			} else {
				return new StandardListBoxModel()
						.includeEmptyValue()
						.includeAs(ACL.SYSTEM,
								project, StandardUsernamePasswordCredentials.class,
								URIRequirementBuilder.fromUri(url).build());
			}
		}

		public static ListBoxModel fillAuthenticationItems(Item project, String url) {
			if (project == null || !project.hasPermission(Item.CONFIGURE)) {
				return new StandardListBoxModel();
			}

			List<Option> options = new ArrayList<>();

			for (Authenticator authenticator : HorreumUploadGlobalConfig.get().getAuthentications()) {
				options.add(new Option(authenticator.getKeyName()));
			}

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


		public static List<Range<Integer>> parseToRange(String value) {
			List<Range<Integer>> validRanges = new ArrayList<>();

			if (Strings.isNullOrEmpty(value)) {
				value = HorreumUpload.DescriptorImpl.validResponseCodes;
			}

			String[] codes = value.split(",");
			for (String code : codes) {
				String[] fromTo = code.trim().split(":");
				checkArgument(fromTo.length <= 2, "Code %s should be an interval from:to or a single value", code);

				Integer from;
				try {
					from = Integer.parseInt(fromTo[0]);
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException("Invalid number " + fromTo[0]);
				}

				Integer to = from;
				if (fromTo.length != 1) {
					try {
						to = Integer.parseInt(fromTo[1]);
					} catch (NumberFormatException nfe) {
						throw new IllegalArgumentException("Invalid number " + fromTo[1]);
					}
				}

				checkArgument(from <= to, "Interval %s should be FROM less than TO", code);
				validRanges.add(Ranges.closed(from, to));
			}

			return validRanges;
		}

		public FormValidation doCheckValidResponseCodes(@QueryParameter String value) {
			return checkValidResponseCodes(value);
		}

		public static FormValidation checkValidResponseCodes(String value) {
			if (value == null || value.trim().isEmpty()) {
				return FormValidation.ok();
			}

			try {
				parseToRange(value);
			} catch (IllegalArgumentException iae) {
				return FormValidation.error("Response codes expected is wrong. " + iae.getMessage());
			}
			return FormValidation.ok();

		}
	}

}
