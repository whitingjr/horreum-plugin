package jenkins.plugins.horreum.expect;

import javax.inject.Inject;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumBaseDescriptor;
import jenkins.plugins.horreum.HorreumBaseStep;

public final class HorreumExpectStep extends HorreumBaseStep<HorreumExpectConfig> {

	@DataBoundConstructor
	public HorreumExpectStep(String credentials,
									 String test,
									 long timeout,
									 String expectedBy,
									 String backlink) {
		super(new HorreumExpectConfig(credentials, test, timeout, expectedBy, backlink));
	}

	public String getTest() {
		return config.getTest();
	}

	@DataBoundSetter
	public void setTest(String test) {
		this.config.setTest(test);
	}

	public long getTimeout() {
		return config.getTimeout();
	}

	@DataBoundSetter
	public void setTimeout(long timeout) {
		this.config.setTimeout(timeout);
	}

	public String getExpectedBy() {
		return config.getExpectedBy();
	}

	@DataBoundSetter
	public void setExpectedBy(String expectedBy) {
		this.config.setExpectedBy(expectedBy);
	}

	public String getBacklink() {
		return config.getBacklink();
	}

	@DataBoundSetter
	public void setBacklink(String backlink) {
		this.config.setBacklink(backlink);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
		public static final Boolean quiet = HorreumExpect.DescriptorImpl.quiet;
		public static final Boolean abortOnFailure = HorreumExpect.DescriptorImpl.abortOnFailure;

		public DescriptorImpl() {
			super(Execution.class);
		}

		@Override
		public String getFunctionName() {
			return "horreumExpect";
		}

		@Override
		public String getDisplayName() {
			return "Notify Horreum that a run will be uploaded.";
		}

		public ListBoxModel doFillAuthenticationItems(
				@AncestorInPath Item project, @QueryParameter String url) {
			return HorreumBaseDescriptor.fillAuthenticationItems(project, url);
		}
	}

	public static final class Execution extends HorreumBaseStep.Execution<Void> {
		@Inject
		private transient HorreumExpectStep step;

		@Override
		protected BaseExecutionContext<Void> createExecutionContext() throws Exception {
			StepContext context = getContext();
			return HorreumExpectExecutionContext.from(step.config, null, context.get(TaskListener.class));
		}


		private static final long serialVersionUID = 1L;
	}
}
