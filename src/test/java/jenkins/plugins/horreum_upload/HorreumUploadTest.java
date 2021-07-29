package jenkins.plugins.horreum_upload;

import org.junit.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

public class HorreumUploadTest extends HorreumPlpuginTestBase{
	@Test
	public void simpleGetTest() throws Exception {

		// Prepare the server
//		registerRequestChecker(HttpMode.GET);

		// Prepare HttpRequest#
		HorreumUpload horreumUpload = new HorreumUpload("Dummy", "dev-team",
				"PUBLIC", "$.build-timestamp",
				"$.build-timestamp", null, "/working/projects/redHat/Hyperfoil/qdup-jenkins-plugin/src/test/resources/script/json/config-quickstart.jvm.json");

		horreumUpload.setQuiet(false);
		horreumUpload.setConsoleLogResponseBody(true);

//		HttpRequest httpRequest = new HttpRequest(baseURL() + "/doGET");
//		httpRequest.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject("Horreum-Upload-Freestyle");
		project.getBuildersList().add(horreumUpload);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}
}
