package jenkins.plugins.horreum_upload.auth;

import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;

import jenkins.plugins.horreum_upload.HorreumUploadGlobalConfig;

//import jenkins.plugins.horreum_upload.HorreumPluginTestBase;

//public class OidcClientRequestFilter extends AbstractTokensProducer implements ClientRequestFilter {
@Provider
public class KeycloakClientRequestFilter implements ClientRequestFilter {

	private static final Logger LOG = Logger.getLogger(KeycloakClientRequestFilter.class);
	private static final String BEARER_SCHEME_WITH_SPACE = "Bearer ";

	protected static Keycloak keycloak;

	String clientName = "horreum";

	public KeycloakClientRequestFilter() {
		KeycloakAuthentication keycloakAuthentication = HorreumUploadGlobalConfig.getKeycloakAuthentication();

		keycloak = KeycloakBuilder.builder()
				.serverUrl(keycloakAuthentication.getKeycloakBaseUrl() + "/auth")
				.realm(keycloakAuthentication.getKeycloakRealm())
				.username(keycloakAuthentication.getUsername())
				.password(keycloakAuthentication.getPassword())
				.clientId(keycloakAuthentication.getClientId())
				.clientSecret(keycloakAuthentication.getClient_secret())
				.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(20).build())
				.build();
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		try {
			final String accessToken = getAccessToken();
			requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, BEARER_SCHEME_WITH_SPACE + accessToken);
		} catch (Exception ex) {
			LOG.debugf("Access token is not available, aborting the request with HTTP 401 error: %s", ex.getMessage());
			requestContext.abortWith(Response.status(401).build());
		}
	}

	private String getAccessToken() {
		AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
		return tokenResponse.getToken();
	}

	protected Optional<String> clientId() {
		return Optional.of(clientName);
	}
}
