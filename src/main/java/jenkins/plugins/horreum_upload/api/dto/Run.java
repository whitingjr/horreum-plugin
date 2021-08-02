package jenkins.plugins.horreum_upload.api.dto;

import java.time.Instant;

import io.hyperfoil.tools.yaup.json.Json;

public class Run {
   public static final String EVENT_NEW = "run/new";
   public static final String EVENT_TRASHED = "run/trashed";

   public Integer id;

   public Instant start;

   public Instant stop;

   public String description;

   public Integer testid;

   public Json data;

   public boolean trashed;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Instant getStart() {
		return start;
	}

	public void setStart(Instant start) {
		this.start = start;
	}

	public Instant getStop() {
		return stop;
	}

	public void setStop(Instant stop) {
		this.stop = stop;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getTestid() {
		return testid;
	}

	public void setTestid(Integer testid) {
		this.testid = testid;
	}

	public Json getData() {
		return data;
	}

	public void setData(Json data) {
		this.data = data;
	}

	public boolean isTrashed() {
		return trashed;
	}

	public void setTrashed(boolean trashed) {
		this.trashed = trashed;
	}
}
