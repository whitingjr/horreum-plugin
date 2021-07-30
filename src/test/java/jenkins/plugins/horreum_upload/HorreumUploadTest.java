package jenkins.plugins.horreum_upload;

import java.net.URL;

import org.junit.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

public class HorreumUploadTest extends HorreumPluginTestBase {
	@Test
	public void simpleGetTest() throws Exception {

		URL jsonResource = Thread.currentThread().getContextClassLoader().getResource("data/config-quickstart.jvm.json");

		// Prepare HttpRequest#
		HorreumUpload horreumUpload = new HorreumUpload(
				configProperties.getProperty("horreum.test.name"),
				configProperties.getProperty("horreum.test.owner"),
				configProperties.getProperty("horreum.test.access"),
				configProperties.getProperty("horreum.test.start-accessor"),
				configProperties.getProperty("horreum.test.stop-accessor"),
				configProperties.getProperty("horreum.test.schema"),
				jsonResource.getPath()
		);

		horreumUpload.setQuiet(false);
		horreumUpload.setConsoleLogResponseBody(true);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject("Horreum-Upload-Freestyle");
		project.getBuildersList().add(horreumUpload);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		this.j.assertBuildStatusSuccess(build);
		this.j.assertLogContains(ALL_IS_WELL, build);
	}

}
