package jenkins.plugins.horreum.experiment;

import jenkins.plugins.horreum.HorreumBaseConfig;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HorreumExperimentConfig extends HorreumBaseConfig {
    private @Nonnull String id;
    private @Nonnull String uri;
    private @Nonnull Integer limit;
    private @Nonnull Integer page;
    private @Nonnull String sort;
    private @Nonnull String direction;
    private @Nonnull String test;
    private @Nonnull String profile;

    public HorreumExperimentConfig ( String credentials, String id, String uri,
                                     String limit, String page, String sort,
                                     String direction, String test,
                                     String profile) {
        this.setCredentials(credentials);

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Id must be set");
        }
        if (isNotInteger(id)){
            throw new IllegalArgumentException("Id must be an integer");
        }
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Uri must be set");
        }
        if (limit == null || limit.trim().isEmpty()) {
            throw new IllegalArgumentException("Limit must be set");
        }
        if (isNotInteger(limit)){
            throw new IllegalArgumentException("Limit must be an integer");
        }
        if (page == null || page.trim().isEmpty()) {
            throw new IllegalArgumentException("Page must be set");
        }
        if (isNotInteger(page)){
            throw new IllegalArgumentException("Page must be an integer");
        }
        if (sort == null || sort.trim().isEmpty()) {
            throw new IllegalArgumentException("Sort must be set");
        }
        if (direction == null || direction.trim().isEmpty()) {
            throw new IllegalArgumentException("Direction must be set");
        }
        if (test == null || test.trim().isEmpty()) {
            throw new IllegalArgumentException("Test must be set");
        }
        if (profile == null || profile.trim().isEmpty()){
            throw new IllegalArgumentException("Profile must be set");
        }
        this.id = Objects.requireNonNull(id.trim());
        this.uri = Objects.requireNonNull(uri.trim());
        this.limit = Integer.parseInt(Objects.requireNonNull(limit.trim()));
        this.page = Integer.parseInt(Objects.requireNonNull(page.trim()));
        this.sort = Objects.requireNonNull(sort.trim());
        this.direction = Objects.requireNonNull(direction.trim());
        this.test = Objects.requireNonNull(test.trim());
        this.profile = Objects.requireNonNull(profile.trim());
    }

    @Nonnull
    public String getId(){ return id; }

    public void setId(String id){ this.id = id; }

    @Nonnull
    public String getUri(){ return uri; }

    public void setUri(String uri){ this.uri = uri; }

    @Nonnull
    public Integer getLimit(){ return limit; }

    public void setLimit(Integer limit){ this.limit = limit; }

    @Nonnull
    public Integer getPage(){ return page; }

    public void setPage(Integer page){ this.page = page; }

    @Nonnull
    public String getSort(){ return sort; }

    public void setSort(String sort){ this.sort = sort; }

    @Nonnull
    public String getDirection(){ return direction; }

    public void setDirection(String direction) { this.direction = direction; }

    @Nonnull
    public String getTest(){ return test; }

    public void setTest(String test) { this.test = test; }

    @Nonnull String getProfile(){ return this.profile; }

    public void setProfile(String profile) { this.profile = profile; }

    public List<HttpRequestNameValuePair> resolveParams() {
        List<HttpRequestNameValuePair> params = new ArrayList<>();
        params.add(new HttpRequestNameValuePair("id", this.id));
        params.add(new HttpRequestNameValuePair("uri", this.uri));
        params.add(new HttpRequestNameValuePair("limit", this.limit.toString()));
        params.add(new HttpRequestNameValuePair("page", this.page.toString()));
        params.add(new HttpRequestNameValuePair("sort", this.sort));
        params.add(new HttpRequestNameValuePair("direction", this.direction));
        params.add(new HttpRequestNameValuePair("test", this.test));
        params.add(new HttpRequestNameValuePair("profile", this.profile));
        return params;
    }

    private boolean isNotInteger(String val){
        try {
            Integer.parseInt(val);
            return false;
        } catch (NumberFormatException nfe){
            return true;
        }
    }
}