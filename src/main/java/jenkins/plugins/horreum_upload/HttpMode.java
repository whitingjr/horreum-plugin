package jenkins.plugins.horreum_upload;

import hudson.util.ListBoxModel;

/**
 * @author Janario Oliveira
 */
public enum HttpMode {
	POST;

	public static ListBoxModel getFillItems() {
		ListBoxModel items = new ListBoxModel();
		for (HttpMode httpMode : values()) {
			items.add(httpMode.name());
		}
		return items;
	}
}
