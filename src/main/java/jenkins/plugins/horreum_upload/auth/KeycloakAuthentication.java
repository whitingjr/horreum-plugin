package jenkins.plugins.horreum_upload.auth;

import java.io.Serializable;
import java.util.List;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import jenkins.model.Jenkins;

public class KeycloakAuthentication extends AbstractDescribableImpl<KeycloakAuthentication> implements Serializable {

	private static final long serialVersionUID = -4370238820425771639L;
	private static final String keyName = "keycloak";

	private String keycloakBaseUrl;
	private String keycloakRealm;
	private String clientId;
	private String horreumClientSecretID;
	private String HorreumCredentialsID;

	//retrieve from creds store before
	private String client_secret;
	private String username;
	private String password;

	public String getKeyName() {
		return keyName;
	}

	public void setKeycloakBaseUrl(String keycloakBaseUrl) {
		this.keycloakBaseUrl = keycloakBaseUrl;
	}

	public void setKeycloakRealm(String keycloakRealm) {
		this.keycloakRealm = keycloakRealm;
	}

	public String getKeycloakBaseUrl() {
		return keycloakBaseUrl;
	}

	public String getKeycloakRealm() {
		return keycloakRealm;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getHorreumClientSecretID() {
		return horreumClientSecretID;
	}

	public void setHorreumClientSecretID(String clientSecret) {
		this.horreumClientSecretID = clientSecret;
	}

	public String getHorreumCredentialsID() {
		return HorreumCredentialsID;
	}

	public void setHorreumCredentialsID(String horreumCredentialsID) {
		HorreumCredentialsID = horreumCredentialsID;
	}

	public String getClient_secret() {
		return client_secret;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	private void validateConfiguration() throws IllegalStateException {
		if ( keycloakBaseUrl == null ){
			throw  new IllegalStateException("Keycloak Base URL can not be empty");
		}
		if ( keycloakRealm == null ){
			throw  new IllegalStateException("Keycloak Realm can not be empty");
		}
		if ( clientId == null ){
			throw  new IllegalStateException("Keycloak Client ID can not be empty");
		}
		if ( HorreumCredentialsID == null ){
			throw  new IllegalStateException("Horreum Credentials can not be empty");
		}
	}

	public void resolveCredentials() {
		//Retrieve Credentials
		//TODO:: pass in DomainRequirement
		List<StandardCredentials> credentialsList = CredentialsProvider.lookupCredentials(
				StandardCredentials.class, // (1)
				Jenkins.get(), // (1)
				ACL.SYSTEM
		) ;

		StandardCredentials usernameCredentials = CredentialsMatchers.firstOrNull(
				credentialsList,
				CredentialsMatchers.withId(this.HorreumCredentialsID)
		);

		StandardCredentials clientSecretCredentials = null;
		if ( this.horreumClientSecretID != null ) {
			clientSecretCredentials = CredentialsMatchers.firstOrNull(
					credentialsList,
					CredentialsMatchers.withId(this.horreumClientSecretID)
			);
		}

		if (usernameCredentials != null && usernameCredentials instanceof UsernamePasswordCredentials ) {
			this.client_secret = clientSecretCredentials != null ? ((StringCredentials) clientSecretCredentials).getSecret().getPlainText() : null;
			this.username = ((UsernamePasswordCredentials) usernameCredentials).getUsername();
			this.password = ((UsernamePasswordCredentials) usernameCredentials).getPassword().getPlainText();
		} else {
			throw new IllegalStateException("Could not retrieve Horreum Credentials. Please check the Horreum plugin configuration in Global Settings");

		}
	}

	@Extension
	public static class OAuthAuthenticationDescriptor extends Descriptor<KeycloakAuthentication> {

//		public FormValidation doCheckKeyName(@QueryParameter String value) {
//			return HorreumUploadGlobalConfig.validateKeyName(value);
//		}

		@Override
		public String getDisplayName() {
			return "OAuth 2.0 Authentication";
		}

	}
}
