package jenkins.plugins.horreum_upload.api.dto;

import java.util.Collection;

public class HorreumTest {
	public static final String EVENT_NEW = "test/new";

	public Integer id;

	public String name;

	public String description;

	public String owner;

	public Access access = Access.PUBLIC;

	public Collection<TestToken> tokens;

	public String tags;

	public View defaultView;

	public String compareUrl;

	public Collection<StalenessSettings> stalenessSettings;

	public Boolean notificationsEnabled;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	public Collection<TestToken> getTokens() {
		return tokens;
	}

	public void setTokens(Collection<TestToken> tokens) {
		this.tokens = tokens;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public View getDefaultView() {
		return defaultView;
	}

	public void setDefaultView(View defaultView) {
		this.defaultView = defaultView;
	}

	public String getCompareUrl() {
		return compareUrl;
	}

	public void setCompareUrl(String compareUrl) {
		this.compareUrl = compareUrl;
	}

	public Collection<StalenessSettings> getStalenessSettings() {
		return stalenessSettings;
	}

	public void setStalenessSettings(Collection<StalenessSettings> stalenessSettings) {
		this.stalenessSettings = stalenessSettings;
	}

	public Boolean getNotificationsEnabled() {
		return notificationsEnabled;
	}

	public void setNotificationsEnabled(Boolean notificationsEnabled) {
		this.notificationsEnabled = notificationsEnabled;
	}
}