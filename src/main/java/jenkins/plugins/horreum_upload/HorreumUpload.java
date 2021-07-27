package jenkins.plugins.horreum_upload;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.misc.NotNull;
import org.apache.http.HttpHeaders;
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

import jenkins.plugins.horreum_upload.HorreumUploadStep.DescriptorImpl;
import jenkins.plugins.horreum_upload.auth.Authenticator;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;

/**
 * @author Janario Oliveira
 */

//TODO: Make safe functionality as upload step
public class HorreumUpload extends Builder {

	private @Nonnull String test;
	private @Nonnull String owner;
	private @Nonnull String access;
	private @Nonnull String startAccessor;
	private @Nonnull String stopAccessor;
	private @Nonnull String schema;
	private @Nonnull String jsonFile = HorreumUploadStep.DescriptorImpl.jsonFile;

	private boolean ignoreSslErrors = HorreumUploadStep.DescriptorImpl.ignoreSslErrors;
	private boolean abortOnFailure = HorreumUploadStep.DescriptorImpl.abortOnFailure;
	private String validResponseCodes         = HorreumUploadStep.DescriptorImpl.validResponseCodes;
	private String validResponseContent       = HorreumUploadStep.DescriptorImpl.validResponseContent;
	private MimeType contentType              = HorreumUploadStep.DescriptorImpl.contentType;
	private Integer timeout                   = HorreumUploadStep.DescriptorImpl.timeout;
	private Boolean consoleLogResponseBody    = HorreumUploadStep.DescriptorImpl.consoleLogResponseBody;
	private Boolean quiet                     = HorreumUploadStep.DescriptorImpl.quiet;
	private String authentication             = HorreumUploadStep.DescriptorImpl.authentication;
	private String requestBody                = HorreumUploadStep.DescriptorImpl.requestBody;
	private List<HttpRequestNameValuePair> customHeaders = HorreumUploadStep.DescriptorImpl.customHeaders;
	private ResponseHandle responseHandle = HorreumUploadStep.DescriptorImpl.responseHandle;

	@DataBoundConstructor
	public HorreumUpload(@Nonnull String test, @Nonnull String owner,
							 @Nonnull String access, @Nonnull String startAccessor,
							 @Nonnull String stopAccessor, @NotNull String schema, @Nonnull String jsonFile) {
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

	public Boolean getIgnoreSslErrors() {
		return ignoreSslErrors;
	}

	public boolean getAbortOnFailure() {
		return abortOnFailure;
	}

	@DataBoundSetter
	public void setAbortOnfailure(Boolean abortOnfailure) {
		this.abortOnFailure = abortOnfailure;
	}

	@DataBoundSetter
	public void setIgnoreSslErrors(Boolean ignoreSslErrors) {
		this.ignoreSslErrors = ignoreSslErrors;
	}


	@Nonnull
	public String getValidResponseCodes() {
		return validResponseCodes;
	}

	@DataBoundSetter
	public void setValidResponseCodes(String validResponseCodes) {
		this.validResponseCodes = validResponseCodes;
	}

	@Nonnull
	public String getOwner() {
		return owner;
	}

	@DataBoundSetter
	public void setOwner(@Nonnull String owner) {
		this.owner = owner;
	}

	@DataBoundSetter
	public void setTest(@Nonnull String test) {
		this.test = test;
	}

	@Nonnull
	public String getAccess() {
		return access;
	}

	@DataBoundSetter
	public void setAccess(@Nonnull String access) {
		this.access = access;
	}

	@Nonnull
	public String getStartAccessor() {
		return startAccessor;
	}

	@DataBoundSetter
	public void setStartAccessor(@Nonnull String startAccessor) {
		this.startAccessor = startAccessor;
	}

	@Nonnull
	public String getStopAccessor() {
		return stopAccessor;
	}

	@DataBoundSetter
	public void setStopAccessor(@Nonnull String stopAccessor) {
		this.stopAccessor = stopAccessor;
	}

	@Nonnull
	public String getSchema() {
		return schema;
	}

	@DataBoundSetter
	public void setSchema(@Nonnull String schema) {
		this.schema = schema;
	}

	public String getValidResponseContent() {
		return validResponseContent;
	}

	@DataBoundSetter
	public void setValidResponseContent(String validResponseContent) {
		this.validResponseContent = validResponseContent;
	}


	public MimeType getContentType() {
		return contentType;
	}

	@DataBoundSetter
	public void setContentType(MimeType contentType) {
		this.contentType = contentType;
	}

	public Integer getTimeout() {
		return timeout;
	}

	@DataBoundSetter
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Boolean getConsoleLogResponseBody() {
		return consoleLogResponseBody;
	}

	@DataBoundSetter
	public void setConsoleLogResponseBody(Boolean consoleLogResponseBody) {
		this.consoleLogResponseBody = consoleLogResponseBody;
	}

	public Boolean getQuiet() {
		return quiet;
	}

	@DataBoundSetter
	public void setQuiet(Boolean quiet) {
		this.quiet = quiet;
	}

	public String getAuthentication() {
		return authentication;
	}

	@DataBoundSetter
	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	public String getRequestBody() {
		return requestBody;
	}

	@DataBoundSetter
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public List<HttpRequestNameValuePair> getCustomHeaders() {
		return customHeaders;
	}

	@DataBoundSetter
	public void setCustomHeaders(List<HttpRequestNameValuePair> customHeaders) {
		this.customHeaders = customHeaders;
	}

	public String getJsonFile() {
		return jsonFile;
	}

	@DataBoundSetter
	public void setJsonFile(String jsonFile) {
		this.jsonFile = jsonFile;
	}

	@Initializer(before = InitMilestone.PLUGINS_STARTED)
	public static void xStreamCompatibility() {
		Items.XSTREAM2.aliasField("logResponseBody", HorreumUpload.class, "consoleLogResponseBody");
		Items.XSTREAM2.aliasField("consoleLogResponseBody", HorreumUpload.class, "consoleLogResponseBody");
		Items.XSTREAM2.alias("pair", HttpRequestNameValuePair.class);
	}

	protected Object readResolve() {
		if (customHeaders == null) {
			customHeaders = DescriptorImpl.customHeaders;
		}
//		if (ignoreSslErrors == null) {
//			//default for new job false(DescriptorImpl.ignoreSslErrors) for old ones true to keep same behavior
//			ignoreSslErrors = true;
//		}
		if (quiet == null) {
			quiet = DescriptorImpl.quiet;
		}
		return this;
	}

	private List<HttpRequestNameValuePair> createParams(EnvVars envVars, AbstractBuild<?, ?> build, TaskListener listener) throws IOException {
		Map<String, String> buildVariables = build.getBuildVariables();
		if (buildVariables.isEmpty()) {
			return Collections.emptyList();
		}
		PrintStream logger = listener.getLogger();
		logger.println("Parameters: ");

		List<HttpRequestNameValuePair> l = new ArrayList<>();
		for (Map.Entry<String, String> entry : buildVariables.entrySet()) {
			String value = envVars.expand(entry.getValue());
			logger.println("  " + entry.getKey() + " = " + value);

			l.add(new HttpRequestNameValuePair(entry.getKey(), value));
		}
		return l;
	}

	List<HttpRequestNameValuePair> resolveHeaders(EnvVars envVars) {
		final List<HttpRequestNameValuePair> headers = new ArrayList<>();
		if (contentType != null && contentType != MimeType.NOT_SET) {
			headers.add(new HttpRequestNameValuePair(HttpHeaders.CONTENT_TYPE, contentType.getContentType().toString()));
		}
		for (HttpRequestNameValuePair header : customHeaders) {
			String headerName = envVars.expand(header.getName());
			String headerValue = envVars.expand(header.getValue());
			boolean maskValue = headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION) ||
					header.getMaskValue();

			headers.add(new HttpRequestNameValuePair(headerName, headerValue, maskValue));
		}
		return headers;
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

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
    throws InterruptedException, IOException
    {
		EnvVars envVars = build.getEnvironment(listener);
		for (Map.Entry<String, String> e : build.getBuildVariables().entrySet()) {
			envVars.put(e.getKey(), e.getValue());
		}

		HorreumUploadExecution exec = HorreumUploadExecution.from(this, envVars, build,
				this.getQuiet() ? TaskListener.NULL : listener);

		exec.getAuthenticator().resolveCredentials();

		VirtualChannel channel = launcher.getChannel();
		if (channel == null) {
			throw new IllegalStateException("Launcher doesn't support remoting but it is required");
		}
		channel.call(exec);

        return true;
    }

	public List<HttpRequestNameValuePair> resolveParams() {
		List<HttpRequestNameValuePair> params = new ArrayList<>();
		params.add(new HttpRequestNameValuePair("test",this.test));
		params.add(new HttpRequestNameValuePair("owner",this.owner));
		params.add(new HttpRequestNameValuePair("access",this.access));
		params.add(new HttpRequestNameValuePair("start",this.startAccessor));
		params.add(new HttpRequestNameValuePair("stop",this.stopAccessor));
		if (this.schema != null) {
			params.add(new HttpRequestNameValuePair("schema", this.schema));
		}
		return params;
	}

	@Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public static final boolean ignoreSslErrors = true;
		public static final boolean abortOnFailure = true;
        public static final Boolean  passBuildParameters       = false;
        public static final String   validResponseCodes        = "100:399";
        public static final String   validResponseContent      = "";
        public static final MimeType contentType               = MimeType.APPLICATION_JSON;
        public static final int      timeout                   = 0;
        public static final Boolean  consoleLogResponseBody    = false;
        public static final Boolean  quiet                     = false;
        public static final String   authentication            = "keycloak";
        public static final String   requestBody               = "";
        public static final String jsonFile = "";
        public static final List <HttpRequestNameValuePair> customHeaders = Collections.emptyList();

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
                    throw new IllegalArgumentException("Invalid number "+fromTo[0]);
                }

                Integer to = from;
                if (fromTo.length != 1) {
                    try {
                        to = Integer.parseInt(fromTo[1]);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Invalid number "+fromTo[1]);
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
                return FormValidation.error("Response codes expected is wrong. "+iae.getMessage());
            }
            return FormValidation.ok();

        }
    }

}
