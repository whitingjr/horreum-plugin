package jenkins.plugins.horreum;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import hudson.PluginManager;
import hudson.PluginWrapper;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.junit.callback.QuarkusTestAfterConstructCallback;
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

import jakarta.enterprise.context.RequestScoped;
import jenkins.RestartRequiredException;
import org.junit.runner.Description;

import static java.util.Map.of;

@RequestScoped
public class JenkinsResource extends org.jvnet.hudson.test.JenkinsRule implements QuarkusTestResourceLifecycleManager, QuarkusTestBeforeEachCallback, QuarkusTestAfterEachCallback {

   @Override
   public void beforeEach(QuarkusTestMethodContext context)  {
      this.testDescription = Description.createTestDescription(
          context.getTestInstance().getClass().getSimpleName(),
          context.getTestMethod().getName()
      );
      try {
         before();
      } catch (Throwable throwable) {
         throw new RuntimeException(throwable);
      }
   }

   @Override
   public Map<String, String> start() {
      return of();
   }

   @Override
   public void stop() {
      try {
         after();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      /*try {
         StaplerRequest req = null;
         //TODO: implement StaplerRequest ???e
         Jenkins.getInstance().doSafeExit(req);
      } catch (IOException ioException) {
         throw new RuntimeException(ioException);
      }*/
   }

   @Override
   public void afterEach(QuarkusTestMethodContext context) {
//      try {
//         after();
//      } catch (Exception e) {
//         throw new RuntimeException(e);
//      }
   }
   public void setTestDescription(String testclass, String testmethod) {
      this.testDescription = Description.createTestDescription(
          testclass,
          testmethod
      );
   }
}
