package jenkins.plugins.horreum_upload.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import io.hyperfoil.tools.yaup.json.Json;
import jenkins.model.Jenkins;
import jenkins.plugins.horreum_upload.HorreumUploadGlobalConfig;
import jenkins.plugins.horreum_upload.MimeType;
import jenkins.plugins.horreum_upload.util.HttpClientUtil;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;
import jenkins.plugins.horreum_upload.util.RequestAction;

/**
 * @author John O'Hara
 */
public class KeycloakAuthentication extends AbstractDescribableImpl<KeycloakAuthentication>
		implements Authenticator {

	private static final long serialVersionUID = -4370238820425771639L;
	private static final String keyName = "keycloak";

	private String keycloakBaseUrl;
	private String keycloakRealm;
	private String clientId;
	private String horreumClientSecretID;
	private String HorreumCredentialsID;

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

	@Override
	public CloseableHttpClient authenticate(HttpClientBuilder clientBuilder, HttpContext context,
											HttpRequestBase requestBase, PrintStream logger) throws IOException, InterruptedException {


		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder
				.append(this.keycloakBaseUrl)
				.append("/auth/realms/")
				.append(this.keycloakRealm)
				.append("/protocol/openid-connect/token")
		;

		final CloseableHttpClient client = clientBuilder.build();
		final HttpClientUtil clientUtil = new HttpClientUtil();

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

		StandardCredentials clientSecretCredentials = CredentialsMatchers.firstOrNull(
				credentialsList,
				CredentialsMatchers.withId(this.horreumClientSecretID)
		);

		if (usernameCredentials != null && usernameCredentials instanceof UsernamePasswordCredentials &&
				clientSecretCredentials != null && clientSecretCredentials instanceof StringCredentials) {
			final List<HttpRequestNameValuePair> params = new ArrayList<>();
			params.add(new HttpRequestNameValuePair("client_id", this.clientId));
			params.add(new HttpRequestNameValuePair("grant_type", "password"));
			params.add(new HttpRequestNameValuePair("scope", "openid"));

			//Add Secrets
			params.add(new HttpRequestNameValuePair("client_secret", ((StringCredentials) clientSecretCredentials).getSecret().getPlainText(), true));
			params.add(new HttpRequestNameValuePair("username", ((UsernamePasswordCredentials) usernameCredentials).getUsername(), true));
			params.add(new HttpRequestNameValuePair("password", ((UsernamePasswordCredentials) usernameCredentials).getPassword().getPlainText(), true));

			final List<HttpRequestNameValuePair> headers = new ArrayList<>();
			headers.add(new HttpRequestNameValuePair("content_type", ContentType.APPLICATION_FORM_URLENCODED.toString()));

			URL authUrl = new URL(urlBuilder.toString());

			RequestAction requestAction = new RequestAction(authUrl, null, params, headers);

			try {
				final HttpResponse authResponse = clientUtil.execute(
						client,
						context,
						clientUtil.createRequestBase(requestAction),
						logger
				);

				//from 400(client error) to 599(server error)
				if ((authResponse.getStatusLine().getStatusCode() >= 400
						&& authResponse.getStatusLine().getStatusCode() <= 599)) {
					throw new IllegalStateException("Error doing authentication");

				} else {
					if (!authResponse.getEntity().getContentType().getValue().equals(MimeType.APPLICATION_JSON.getValue())) {
						throw new IllegalStateException("Auth request did not return json object");
					}
					String jsonvalue;
					try {
						InputStreamReader streamReader = new InputStreamReader(authResponse.getEntity().getContent(), StandardCharsets.UTF_8);
						BufferedReader bufferedReader = new BufferedReader(streamReader);
						jsonvalue = bufferedReader
								.lines()
								.collect(Collectors.joining("\n"));
						bufferedReader.close();
						streamReader.close();
					} catch (IOException ioe) {
						throw new IllegalStateException("Error reading authentication response");
					}

					Json json = Json.fromString(jsonvalue);

					context.setAttribute("http.auth.access_token", json.getString("access_token"));

				}
			} catch (IOException ioException) {
				throw new IllegalStateException("Error sending request to: " + authUrl);
			}
		} else {
			throw new IllegalStateException("Could not retrieve Horreum Credentials. Please check the Horreum plugin configuration in Global Settings");
		}


		return client;
	}

	@Extension
	public static class OAuthAuthenticationDescriptor extends Descriptor<KeycloakAuthentication> {

		public FormValidation doCheckKeyName(@QueryParameter String value) {
			return HorreumUploadGlobalConfig.validateKeyName(value);
		}

		@Override
		public String getDisplayName() {
			return "OAuth 2.0 Authentication";
		}

	}
}
