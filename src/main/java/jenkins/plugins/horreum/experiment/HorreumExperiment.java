package jenkins.plugins.horreum.experiment;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import jenkins.plugins.horreum.HorreumBaseBuilder;
import jenkins.plugins.horreum.HorreumBaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

public class HorreumExperiment extends HorreumBaseBuilder<HorreumExperimentConfig> {
    @DataBoundConstructor
    public HorreumExperiment (@Nonnull String credentials, @Nonnull String id,
                              @Nonnull String uri, @Nonnull String limit,
                              @Nonnull String page, @Nonnull String sort,
                              @Nonnull String direction, @Nonnull String test,
                              @Nonnull String profile){
        super(new HorreumExperimentConfig(credentials, id, uri, limit, page, sort, direction, test, profile));
    }

    public String getId() { return config.getId(); }

    @DataBoundSetter
    public void setId(String id){ config.setId(id); }

    public String getUri() { return config.getUri(); }

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

    @Override
    protected HorreumExperimentExecutionContext createExecutionContext(AbstractBuild<?, ?> build, BuildListener listener, EnvVars envVars) {
        return HorreumExperimentExecutionContext.from(config, envVars, listener);
    }

    @Extension
    public static final class DescriptorImpl extends HorreumBaseDescriptor {
        public static final boolean abortOnFailure = true;
        public static final Boolean quiet = false;

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Horreum Experiment Executor";
        }
    }
}
