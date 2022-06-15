package jenkins.plugins.horreum;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.hyperfoil.tools.horreum.api.RunService;
import jenkins.plugins.horreum.upload.HorreumUpload;

public class HorreumUploadTest extends HorreumPluginTestBase {
	@Test
	public void testUpload() throws Exception {
		createOrLookupTest();
		URL jsonResource = Thread.currentThread().getContextClassLoader().getResource("data/config-quickstart.jvm.json");

		// Prepare HttpRequest#
		HorreumUpload horreumUpload = new HorreumUpload(
				dummyTest.name,
				dummyTest.owner,
				"PUBLIC",
				"$.build-timestamp",
				"$.build-timestamp",
				"",
				jsonResource.getPath(),
				true
		);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject("Horreum-Upload-Freestyle");
		project.getBuildersList().add(horreumUpload);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatusSuccess(build);

		RunService.RunsSummary summary = horreumClient.runService.listTestRuns(dummyTest.id, false, null, null, "", null);
		assertEquals(1, summary.total);
		assertEquals(1, summary.runs.size());
	}
}
