package jenkins.plugins.horreum_upload.api.dto;

import java.util.List;

/**
 * Security model: the access to view is limited by access to the referenced test.
 */
public class View {
   public Integer id;

   public String name;

   public HorreumTest test;

   public List<ViewComponent> components;

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

	public HorreumTest getTest() {
		return test;
	}

	public void setTest(HorreumTest test) {
		this.test = test;
	}

	public List<ViewComponent> getComponents() {
		return components;
	}

	public void setComponents(List<ViewComponent> components) {
		this.components = components;
	}
}
