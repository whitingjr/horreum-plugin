package jenkins.plugins.horreum;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

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

   protected HorreumClient createClient() {
      HorreumClient.Builder clientBuilder = new HorreumClient.Builder()
            .horreumUrl(url)
            .keycloakUrl(keycloak.getKeycloakBaseUrl())
            .keycloakRealm(keycloak.getKeycloakRealm())
            .clientId(keycloak.getClientId())
            .horreumUser(keycloak.getUsername())
            .horreumPassword(keycloak.getPassword());
      HorreumClient client = clientBuilder.build();
      return client;
   }
}
