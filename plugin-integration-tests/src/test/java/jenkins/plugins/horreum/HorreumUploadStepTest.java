package jenkins.plugins.horreum;

import static jenkins.plugins.horreum.HorreumIntegrationClient.getHorreumClient;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import hudson.FilePath;
import io.hyperfoil.tools.horreum.api.services.RunService;
import io.hyperfoil.tools.horreum.it.profile.InContainerProfile;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
@TestProfile(InContainerProfile.class)
public class HorreumUploadStepTest extends HorreumPluginTestBase {
   @Test
   public void testUpload() throws Exception {
      URL jsonResource = Thread.currentThread().getContextClassLoader().getResource("data/config-quickstart.jvm.json");
      WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "Horreum-Upload-Pipeline");
      proj.setDefinition(new CpsFlowDefinition(
            "node {\n" +
            "def id = horreumUpload(\n" +
            "credentials: '" + HorreumPluginTestBase.HORREUM_UPLOAD_CREDENTIALS + "',\n" +
            "test: '" + dummyTest.name + "',\n" +
            "owner: '" + dummyTest.owner + "',\n" +
            "access: 'PUBLIC',\n" +
            "start: '$.build-timestamp',\n" +
            "stop: '$.build-timestamp',\n" +
            "jsonFile: '" + jsonResource.getPath() + "',\n" +
            "addBuildInfo: true\n" +
            ")\n" +
            "println(id)\n" +
            "}\n",
            true));

      WorkflowRun run = proj.scheduleBuild2(0).get();

      j.assertBuildStatusSuccess(run);

      RunService.RunsSummary summary = getHorreumClient().runService.listTestRuns(dummyTest.id, false, null, null, "", null);
      assertEquals(1, summary.total);
      assertEquals(1, summary.runs.size());
   }

   @Test
   public void testUploadMultiple() throws Exception {
      URL jsonResource1 = Thread.currentThread().getContextClassLoader().getResource("data/config-quickstart.jvm.json");
      URL jsonResource2 = Thread.currentThread().getContextClassLoader().getResource("data/another-file.json");
      assertNotNull(j);
      LOGGER.info("logging j");
      assertNotNull(j.jenkins);
      WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "Horreum-Upload-Pipeline");
      FilePath folder = j.jenkins.getWorkspaceFor(proj).child("run");
      folder.child("config-quickstart.jvm.json").copyFrom(jsonResource1);
      folder.child("another-file.json").copyFrom(jsonResource2);
      proj.setDefinition(new CpsFlowDefinition(
           "node {\n" +
           "def id = horreumUpload(\n" +
           "credentials: '" + HorreumPluginTestBase.HORREUM_UPLOAD_CREDENTIALS + "',\n" +
           "test: '" + dummyTest.name + "',\n" +
           "owner: '" + dummyTest.owner + "',\n" +
           "access: 'PUBLIC',\n" +
           "start: '1970-01-01T00:00:00.00Z',\n" +
           "stop: '1970-01-01T00:00:01.00Z',\n" +
           "files: '**/*.json',\n"+
           "addBuildInfo: true\n" +
           ")\n" +
           "println(id)\n" +
           "}\n",
           true));
      WorkflowRun run = proj.scheduleBuild2(0).get();
      j.assertBuildStatusSuccess(run);
      RunService.RunsSummary summary = getHorreumClient().runService.listTestRuns(dummyTest.id, false, null, null, "", null);
      assertEquals(1, summary.total);
      assertEquals(1, summary.runs.size());
      Object runObject = getHorreumClient().runService.getRun(summary.runs.get(0).id,summary.runs.get(0).token);
      assertNotNull(runObject);
      assertTrue(runObject instanceof Map,"run should return a map");
      Object data = ((Map)runObject).get("data");
      assertNotNull(data);
      assertTrue(data instanceof Map,"data should be a map");
      assertEquals(2,((Map<?, ?>) data).size(),"data should have an entry for each file");
   }

   private static final Logger LOGGER = Logger.getLogger(HorreumUploadStepTest.class.getName());
}
