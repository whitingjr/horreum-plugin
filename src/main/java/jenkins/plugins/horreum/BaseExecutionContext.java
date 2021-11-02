package jenkins.plugins.horreum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.CloseProofOutputStream;
import hudson.remoting.RemoteOutputStream;
import io.hyperfoil.tools.HorreumClient;
import jenkins.plugins.horreum.auth.KeycloakAuthentication;
import jenkins.security.MasterToSlaveCallable;

public abstract class BaseExecutionContext<R> extends MasterToSlaveCallable<R, RuntimeException> {
   protected final String url;
   protected final KeycloakAuthentication keycloak;
   protected final OutputStream remoteLogger;
   protected transient PrintStream localLogger;

   public BaseExecutionContext(String url, PrintStream logger) {
      this.url = url;
      keycloak = HorreumGlobalConfig.get().getAuthentication();
      this.remoteLogger = new RemoteOutputStream(new CloseProofOutputStream(logger));
      this.localLogger = logger;
   }

   protected PrintStream logger() {
      if (localLogger == null) {
         try {
            localLogger = new PrintStream(remoteLogger, true, StandardCharsets.UTF_8.name());
         } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
         }
      }
      return localLogger;
   }

   @Override
   @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "Undeclared exception can be thrown from response.getEntity()")
   public R call() {
      HorreumClient client = createClient();

      try {
         return invoke(client);
      } catch (Exception e) {
         Throwable cause = e;
         for (;;) {
            if (cause instanceof WebApplicationException) {
               Response response = ((WebApplicationException) cause).getResponse();
               Object entity = null;
               try {
                  entity = response.getEntity();
                  if (entity instanceof InputStream) {
                     entity = toByteArrayOutputStream((InputStream) entity).toString(StandardCharsets.UTF_8.name());
                  }
               } catch (Exception e2) {
                  // ignore e.g. IllegalStateException: RESTEASY003765: Response is closed.
               }
               logger().printf("Request failed with status %d, message: %s", response.getStatus(), entity);
            }
            if (cause.getCause() != null && cause.getCause() != cause) {
               cause = cause.getCause();
            } else {
               break;
            }
         }
         throw e;
      }
   }

   private static ByteArrayOutputStream toByteArrayOutputStream(InputStream stream) throws IOException {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = stream.read(buffer)) != -1) {
         result.write(buffer, 0, length);
      }
      stream.close();
      return result;
   }

   protected abstract R invoke(HorreumClient client);

   protected HorreumClient createClient() {
      HorreumClient.Builder clientBuilder = new HorreumClient.Builder()
            .horreumUrl(url)
            .keycloakUrl(keycloak.getBaseUrl())
            .keycloakRealm(keycloak.getRealm())
            .clientId(keycloak.getClientId())
            .horreumUser(keycloak.getUsername())
            .horreumPassword(keycloak.getPassword());
      HorreumClient client = clientBuilder.build();
      return client;
   }
}
