package jenkins.plugins.horreum.check;

import jenkins.plugins.horreum.HorreumBaseConfig;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HorreumCheckConfig extends HorreumBaseConfig {

    private @NotNull String test;

    private @NotNull String profile;

    public HorreumCheckConfig (String credentials, String test, String profile){
        this.setCredentials(credentials);

        if (test == null || test.trim().isEmpty()){
            throw new IllegalArgumentException("Test name must be set");
        }
        if (profile == null || profile.trim().isEmpty()){
            throw new IllegalArgumentException("Profile name must be set");
        }
        this.test = Objects.requireNonNull(test.trim());
        this.profile = Objects.requireNonNull(profile.trim());
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public List<HttpRequestNameValuePair> resolveParams() {
        List<HttpRequestNameValuePair> params = new ArrayList<>();
        params.add(new HttpRequestNameValuePair("test", this.test));
        params.add(new HttpRequestNameValuePair("profile",this.profile));
        return params;
    }
}
