package jenkins.plugins.horreum_upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.antlr.v4.runtime.misc.NotNull;
import org.apache.http.HttpHeaders;
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

/**
 * @author Martin d'Anjou
 */
public final class HorreumUploadStep extends AbstractStepImpl {

    private @Nonnull String test;
    private @Nonnull String owner;
    private @Nonnull String access;
    private @Nonnull String startAccessor;
    private @Nonnull String stopAccessor;
    private @Nonnull String schema;
	private @Nonnull String jsonFile = DescriptorImpl.jsonFile;

	private boolean ignoreSslErrors = DescriptorImpl.ignoreSslErrors;
	private boolean abortOnFailure = DescriptorImpl.abortOnFailure;
    private String validResponseCodes         = DescriptorImpl.validResponseCodes;
    private String validResponseContent       = DescriptorImpl.validResponseContent;
    private MimeType contentType              = DescriptorImpl.contentType;
    private Integer timeout                   = DescriptorImpl.timeout;
    private Boolean consoleLogResponseBody    = DescriptorImpl.consoleLogResponseBody;
    private Boolean quiet                     = DescriptorImpl.quiet;
    private String authentication             = DescriptorImpl.authentication;
    private List<HttpRequestNameValuePair> customHeaders = DescriptorImpl.customHeaders;
	private ResponseHandle responseHandle = DescriptorImpl.responseHandle;

	public boolean isIgnoreSslErrors() {
		return ignoreSslErrors;
	}

	@DataBoundConstructor
	public HorreumUploadStep(@Nonnull String test, @Nonnull String owner,
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

	public boolean getAbortOnFailure() {
		return abortOnFailure;
	}

	@DataBoundSetter
	public void setAbortOnFailure(boolean abortOnFailure) {
		this.abortOnFailure = abortOnFailure;
	}

	@DataBoundSetter
	public void setIgnoreSslErrors(boolean ignoreSslErrors) {
		this.ignoreSslErrors = ignoreSslErrors;
	}

    @DataBoundSetter
    public void setValidResponseCodes(String validResponseCodes) {
        this.validResponseCodes = validResponseCodes;
    }

    public String getValidResponseCodes() {
        return validResponseCodes;
    }

    @DataBoundSetter
    public void setValidResponseContent(String validResponseContent) {
        this.validResponseContent = validResponseContent;
    }

    public String getValidResponseContent() {
        return validResponseContent;
    }

    @DataBoundSetter
    public void setContentType(MimeType contentType) {
        this.contentType = contentType;
    }

    public MimeType getContentType() {
        return contentType;
    }

    @DataBoundSetter
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getTimeout() {
        return timeout;
    }

    @DataBoundSetter
    public void setConsoleLogResponseBody(Boolean consoleLogResponseBody) {
        this.consoleLogResponseBody = consoleLogResponseBody;
    }

    public Boolean getConsoleLogResponseBody() {
        return consoleLogResponseBody;
    }

    @DataBoundSetter
    public void setQuiet(Boolean quiet) {
        this.quiet = quiet;
    }

    public Boolean getQuiet() {
        return quiet;
    }

    @DataBoundSetter
    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getAuthentication() {
        return authentication;
    }

    @DataBoundSetter
    public void setCustomHeaders(List<HttpRequestNameValuePair> customHeaders) {
        this.customHeaders = customHeaders;
    }

	public String getTest() {
		return test;
	}

	@DataBoundSetter
	public void setTest(String test) {
		this.test = test;
	}

	public String getOwner() {
		return owner;
	}

	@DataBoundSetter
	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getAccess() {
		return access;
	}

	@DataBoundSetter
	public void setAccess(String access) {
		this.access = access;
	}

	public String getStartAccessor() {
		return startAccessor;
	}

	@DataBoundSetter
	public void setStartAccessor(String startAccessor) {
		this.startAccessor = startAccessor;
	}

	public String getStopAccessor() {
		return stopAccessor;
	}

	@DataBoundSetter
	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		return schema;
	}

	@DataBoundSetter
	public void setStopAccessor(String stopAccessor) {
		this.stopAccessor = stopAccessor;
	}

	public List<HttpRequestNameValuePair> getCustomHeaders() {
        return customHeaders;
    }

	public ResponseHandle getResponseHandle() {
		return responseHandle;
	}


	@DataBoundSetter
	public void setResponseHandle(ResponseHandle responseHandle) {
		this.responseHandle = responseHandle;
	}

	public String getJsonFile() {
		return jsonFile;
	}

	@DataBoundSetter
	public void setJsonFile(String jsonFile) {
		this.jsonFile = jsonFile;
	}


	@Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

	List<HttpRequestNameValuePair> resolveHeaders() {
		final List<HttpRequestNameValuePair> headers = new ArrayList<>();
		if (contentType != null && contentType != MimeType.NOT_SET) {
			headers.add(new HttpRequestNameValuePair(HttpHeaders.CONTENT_TYPE, contentType.getContentType().toString()));
		}
		for (HttpRequestNameValuePair header : customHeaders) {
			String headerName = header.getName();
			String headerValue = header.getValue();
			boolean maskValue = headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION) ||
					header.getMaskValue();

			headers.add(new HttpRequestNameValuePair(headerName, headerValue, maskValue));
		}
		return headers;
	}

	public List<HttpRequestNameValuePair> resolveParams() {
		List<HttpRequestNameValuePair> params = new ArrayList<>();
		params.add(new HttpRequestNameValuePair("test",this.test));
		params.add(new HttpRequestNameValuePair("owner",this.owner));
		params.add(new HttpRequestNameValuePair("access",this.access));
		params.add(new HttpRequestNameValuePair("start",this.startAccessor));
		params.add(new HttpRequestNameValuePair("stop",this.stopAccessor));
		params.add(new HttpRequestNameValuePair("schema",this.schema));
		return params;
	}


	@Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
        public static final boolean ignoreSslErrors = HorreumUpload.DescriptorImpl.ignoreSslErrors;
        public static final boolean abortOnFailure = HorreumUpload.DescriptorImpl.abortOnFailure;
		public static final String   validResponseCodes        = HorreumUpload.DescriptorImpl.validResponseCodes;
		public static final String   validResponseContent      = HorreumUpload.DescriptorImpl.validResponseContent;
		public static final MimeType contentType               = HorreumUpload.DescriptorImpl.contentType;
        public static final int      timeout                   = HorreumUpload.DescriptorImpl.timeout;
        public static final Boolean  consoleLogResponseBody    = HorreumUpload.DescriptorImpl.consoleLogResponseBody;
        public static final Boolean  quiet                     = HorreumUpload.DescriptorImpl.quiet;
        public static final String   authentication            = HorreumUpload.DescriptorImpl.authentication;
        public static final String   requestBody               = HorreumUpload.DescriptorImpl.requestBody;
        public static final String jsonFile = HorreumUpload.DescriptorImpl.jsonFile;
        public static final List <HttpRequestNameValuePair> customHeaders = Collections.emptyList();
		public static final ResponseHandle responseHandle = ResponseHandle.STRING;

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

        public ListBoxModel doFillHttpModeItems() {
            return HttpMode.getFillItems();
        }

        public ListBoxModel doFillAcceptTypeItems() {
            return MimeType.getContentTypeFillItems();
        }

        public ListBoxModel doFillContentTypeItems() {
            return MimeType.getContentTypeFillItems();
        }

		public ListBoxModel doFillResponseHandleItems() {
			ListBoxModel items = new ListBoxModel();
			for (ResponseHandle responseHandle : ResponseHandle.values()) {
				items.add(responseHandle.name());
			}
			return items;
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
			HorreumUploadExecution exec = HorreumUploadExecution.from(step,
					step.getQuiet() ? TaskListener.NULL : listener,
					this);

			exec.getAuthenticator().resolveCredentials();

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
