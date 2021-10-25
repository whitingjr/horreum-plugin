package jenkins.plugins.horreum;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;

import io.hyperfoil.tools.horreum.api.RunService;

public class HorreumUploadStepTest extends HorreumPluginTestBase {
   @Test
   public void testUpload() throws Exception {
      createOrLookupTest();

      URL jsonResource = Thread.currentThread().getContextClassLoader().getResource("data/config-quickstart.jvm.json");
      WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "Horreum-Upload-Pipeline");
      proj.setDefinition(new CpsFlowDefinition(
            "node {\n" +
            "def id = horreumUpload(\n" +
            "test: '" + dummyTest.name + "',\n" +
            "owner: '" + dummyTest.owner + "',\n" +
            "access: 'PUBLIC',\n" +
            "start: '$.build-timestamp',\n" +
            "stop: '$.build-timestamp',\n" +
            "jsonFile: '" + jsonResource.getPath() + "'\n" +
            ")\n" +
            "println('Run ID: '+ id )\n" +
            "}\n",
            true));

      WorkflowRun run = proj.scheduleBuild2(0).get();

      j.assertBuildStatusSuccess(run);

      RunService.TestRunsSummary summary = horreumClient.runService.testList(dummyTest.id, false, null, null, null, "", null);
      assertEquals(1, summary.total);
      assertEquals(1, summary.runs.size());
   }
}
