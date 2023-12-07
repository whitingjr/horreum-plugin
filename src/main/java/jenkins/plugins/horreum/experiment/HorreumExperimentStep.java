package jenkins.plugins.horreum.experiment;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumBaseDescriptor;
import jenkins.plugins.horreum.HorreumBaseStep;
import jenkins.plugins.horreum.HorreumGlobalConfig;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.inject.Inject;

public class HorreumExperimentStep extends HorreumBaseStep<HorreumExperimentConfig> {

    @DataBoundConstructor
    public HorreumExperimentStep(String credentials, String id, String uri,
                                 String limit, String page, String sort,
                                 String direction, String test, String profile) {
        super(new HorreumExperimentConfig(credentials, id, uri, limit, page, sort, direction, test, profile));
        HorreumGlobalConfig globalConfig = HorreumGlobalConfig.get();
        this.config.setKeycloakRealm(globalConfig.getKeycloakRealm());
        this.config.setClientId(globalConfig.getClientId());
    }

    public String getId() { return config.getId(); }

    @DataBoundSetter
    public void setId(String id){ this.config.setId(id); }

    public String getUri() { return this.config.getUri(); }

    @DataBoundSetter
    public void setUri(String uri){ config.setUri(uri); }

    public Integer getLimit(){ return config.getLimit(); }

    @DataBoundSetter
    public void setLimit(Integer limit){ config.setLimit(limit); }

    public Integer getPage() { return config.getPage(); }

    @DataBoundSetter
    public void setPage(Integer page) { config.setPage(page);}

    public String getSort() { return config.getSort(); }

    @DataBoundSetter
    public void setSort(String sort){ config.setSort(sort); }

    public String getDirection() { return config.getDirection(); }

    @DataBoundSetter
    public void setDirection(String direction) { config.setDirection( direction); }

    public String getTest(){ return config.getTest(); }

    @DataBoundSetter
    public void setTest(String test){ config.setTest(test);}

    public String getProfile(){ return config.getProfile(); }

    @DataBoundSetter
    public void setProfile(String profile){ config.setProfile(profile); }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() { super(HorreumExperimentStep.Execution.class); }

        @Override
        public String getFunctionName() { return "horreumExperiment"; }

        @Override
        public String getDisplayName() { return "Execute the Test Experiments."; }

        public ListBoxModel doFillAuthenticationItems(
            @AncestorInPath Item project, @QueryParameter String url) {
            return HorreumBaseDescriptor.fillAuthenticationItems(project, url);
        }
    }

    public static final class Execution extends HorreumBaseStep.Execution<String> {
        @Inject
        private transient HorreumExperimentStep step;

        @Override
        protected BaseExecutionContext<String> createExecutionContext() throws Exception {
            return HorreumExperimentExecutionContext.from(step.config, null, getContext().get(TaskListener.class));
        }
        private static final long serialVersionUID = 1L;
    }
}
