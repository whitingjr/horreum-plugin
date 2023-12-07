package jenkins.plugins.horreum.check;

import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumBaseDescriptor;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import hudson.Extension;
import jenkins.plugins.horreum.HorreumBaseStep;
import jenkins.plugins.horreum.HorreumGlobalConfig;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.inject.Inject;

public final class HorreumCheckStep extends HorreumBaseStep<HorreumCheckConfig> {

    @DataBoundConstructor
    public HorreumCheckStep(String credentials, String test, String profile) {
        super(new HorreumCheckConfig(credentials, test, profile));
        HorreumGlobalConfig globalConfig = HorreumGlobalConfig.get();
        this.config.setKeycloakRealm(globalConfig.getKeycloakRealm());
        this.config.setClientId(globalConfig.getClientId());
    }

    public String getTest() {
        return config.getTest();
    }

    @DataBoundSetter
    public void setTest(String test) {
        this.config.setTest(test);
    }

    public String getProfile() {
        return this.getProfile();
    }

    @DataBoundSetter
    public void setProfile(String profile) {
        this.config.setProfile(profile);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() { super(Execution.class); }

        @Override
        public String getFunctionName() { return "horreumCheck"; }

        @Override
        public String getDisplayName() { return "Check with Horreum a Test Experiment Profile name exists."; }

        public ListBoxModel doFillAuthenticationItems(
            @AncestorInPath Item project, @QueryParameter String url) {
            return HorreumBaseDescriptor.fillAuthenticationItems(project, url);
        }
    }

    public static final class Execution extends HorreumBaseStep.Execution<String> {
        @Inject
        private transient HorreumCheckStep step;

        @Override
        protected BaseExecutionContext<String> createExecutionContext() throws Exception {
            return HorreumCheckExecutionContext.from(step.config, null, getContext().get(TaskListener.class));
        }
        private static final long serialVersionUID = 1L;

        public static long getSerialVersionUID() {
            return serialVersionUID;
        }
    }
}