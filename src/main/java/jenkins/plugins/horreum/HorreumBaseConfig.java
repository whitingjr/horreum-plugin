package jenkins.plugins.horreum;

import java.io.Serializable;

import javax.annotation.Nonnull;

public abstract class HorreumBaseConfig implements Serializable {
   static Boolean DEFAULT_QUIET = false;
   static Boolean DEFAULT_ABORT_ON_FAILURE = true;

   private Boolean quiet = DEFAULT_QUIET;
   private Boolean abortOnFailure = DEFAULT_ABORT_ON_FAILURE;
   private String keycloakRealm;
   private String clientId;
   private String credentialsID;

   public boolean getAbortOnFailure() {
      return abortOnFailure;
   }

   public void setAbortOnFailure(boolean abortOnFailure) {
      this.abortOnFailure = abortOnFailure;
   }

   public Boolean getQuiet() {
      return quiet;
   }

   public void setQuiet(@Nonnull Boolean quiet) {
      this.quiet = quiet;
   }

   //TODO:: abstract away keycloak specific config
   public String getKeycloakRealm() {
      return keycloakRealm;
   }

   public void setKeycloakRealm(String keycloakRealm) {
      this.keycloakRealm = keycloakRealm;
   }

   public String getClientId() {
      return clientId;
   }

   public void setClientId(String clientId) {
      this.clientId = clientId;
   }

   public String getHorreumCredentialsID() {
      return credentialsID;
   }

   public void setHorreumCredentialsID(String credentialsID) {
      this.credentialsID = credentialsID;
   }

   @Nonnull
   protected static String orEmpty(String value) {
      return value == null ? "" : value;
   }
}
