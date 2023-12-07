package jenkins.plugins.horreum.check;

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

public class HorreumCheck extends HorreumBaseBuilder<HorreumCheckConfig> {

    @DataBoundConstructor
    public HorreumCheck(@Nonnull String credentials,
                        @Nonnull String test,
                        @Nonnull String profile) {
        super(new HorreumCheckConfig(credentials, test, profile));
    }

    public String getTest() { return config.getTest(); }

    @DataBoundSetter
    public void setTest(String test) { this.config.setTest(test); }

    public String getProfile() { return this.config.getProfile(); }

    @DataBoundSetter
    public void setProfile(String profile) { this.config.setProfile(profile); }

    @Override
    protected HorreumCheckExecutionContext createExecutionContext(AbstractBuild<?,?> build, BuildListener listener, EnvVars envVars) {
        return HorreumCheckExecutionContext.from(config, envVars, listener);
    }

    @Extension
    public static final class DescriptorImpl extends HorreumBaseDescriptor {
        public static final boolean abortOnFailure = true;
        public static final Boolean quiet = false;

        public DescriptorImpl () { load(); }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Horreum Check Profile name";
        }
    }
}
