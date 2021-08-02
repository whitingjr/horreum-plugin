package jenkins.plugins.horreum_upload.api.dto;

public class TestToken {
   public static final int READ = 1;
   // e.g. change config, or delete
   public static final int MODIFY = 2;
   // e.g. for test this grants upload of runs
   public static final int UPLOAD = 4;

   public Integer id;

   public HorreumTest test;

   private String value;

   public int permissions;

   public String description;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public HorreumTest getTest() {
		return test;
	}

	public void setTest(HorreumTest test) {
		this.test = test;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getPermissions() {
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
