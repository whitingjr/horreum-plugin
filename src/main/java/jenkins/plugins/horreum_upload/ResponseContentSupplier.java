package jenkins.plugins.horreum_upload;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

public class ResponseContentSupplier implements Serializable {

	private static final long serialVersionUID = 1L;

	private int status;
	private Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private String charset;

	private String content;

	public ResponseContentSupplier(String content, int status) {
		this.content = content;
		this.status = status;
	}

	@Whitelisted
	public int getStatus() {
		return this.status;
	}

	@Whitelisted
	public Map<String, List<String>> getHeaders() {
		return this.headers;
	}

	@Whitelisted
	public String getCharset() {
		return charset;
	}

	@Whitelisted
	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "Status: " + this.status;
	}

}
