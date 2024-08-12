package jenkins.plugins.horreum;

import static jenkins.plugins.horreum.HorreumIntegrationClient.getHorreumClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import io.hyperfoil.tools.horreum.api.alerting.RunExpectation;

import io.hyperfoil.tools.horreum.it.profile.InContainerProfile;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@QuarkusIntegrationTest
@TestProfile(InContainerProfile.class)
public class HorreumExpectStepTest extends HorreumPluginTestBase {
   @Test
   public void testExpect(TestInfo info) throws Exception {
      WorkflowJob proj = j.jenkins.createProject(WorkflowJob.class, "Horreum-Expect-Pipeline");
      proj.setDefinition(new CpsFlowDefinition(
            "node {\n" +
            "horreumExpect(\n" +
            "credentials: '" + HorreumPluginTestBase.HORREUM_UPLOAD_CREDENTIALS + "',\n" +
            "test: '" + info.getDisplayName() + "',\n" +
            "timeout: 60,\n" +
            "expectedBy: 'Jenkins CI',\n" +
            "backlink: \"${env.BUILD_URL}\",\n" +
            ")\n" +
            "}\n",
            true));

      WorkflowRun run = proj.scheduleBuild2(0).get();

      j.assertBuildStatusSuccess(run);

      List<RunExpectation> expectations = getHorreumClient().alertingService.expectations();
      expectations = expectations.stream().filter(e -> e.testId == dummyTest.id).collect(Collectors.toList());
      assertEquals(1, expectations.size());
      RunExpectation runExpectation = expectations.get(0);
      assertEquals("Jenkins CI", runExpectation.expectedBy);
      assertTrue(runExpectation.backlink.contains("jenkins/job/Horreum-Expect-Pipeline"));
   }
}
