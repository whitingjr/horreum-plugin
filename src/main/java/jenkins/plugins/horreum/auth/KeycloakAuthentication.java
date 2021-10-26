package jenkins.plugins.horreum.auth;

import java.io.Serializable;
import java.util.List;

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

	private String baseUrl;
	private String realm;
	private String clientId;
	private String credentialsID;

	//retrieve from creds store before
	private String username;
	private String password;

	public String getKeyName() {
		return keyName;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getRealm() {
		return realm;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getCredentialsID() {
		return credentialsID;
	}

	public void setCredentialsID(String credentialsID) {
		this.credentialsID = credentialsID;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	private void validateConfiguration() throws IllegalStateException {
		if ( baseUrl == null ){
			throw  new IllegalStateException("Keycloak Base URL can not be empty");
		}
		if ( realm == null ){
			throw  new IllegalStateException("Keycloak Realm can not be empty");
		}
		if ( clientId == null ){
			throw  new IllegalStateException("Keycloak Client ID can not be empty");
		}
		if ( credentialsID == null ){
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
				CredentialsMatchers.withId(this.credentialsID)
		);

		if (usernameCredentials instanceof UsernamePasswordCredentials) {
			this.username = ((UsernamePasswordCredentials) usernameCredentials).getUsername();
			this.password = ((UsernamePasswordCredentials) usernameCredentials).getPassword().getPlainText();
		} else {
			throw new IllegalStateException("Could not retrieve Horreum Credentials. Please check the Horreum plugin configuration in Global Settings");
		}
	}

	@Extension
	public static class OAuthAuthenticationDescriptor extends Descriptor<KeycloakAuthentication> {

//		public FormValidation doCheckKeyName(@QueryParameter String value) {
//			return HorreumGlobalConfig.validateKeyName(value);
//		}

		@Override
		public String getDisplayName() {
			return "OAuth 2.0 Authentication";
		}

	}
}
