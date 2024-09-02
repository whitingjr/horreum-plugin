package jenkins.plugins.horreum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.data.Test;
import io.hyperfoil.tools.horreum.api.services.ConfigService;
import io.quarkus.test.junit.callback.*;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import jakarta.ws.rs.client.ClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;

import static io.hyperfoil.tools.horreum.infra.common.Const.HORREUM_DEV_KEYCLOAK_ADMIN_PASSWORD;
import static io.hyperfoil.tools.horreum.infra.common.Const.HORREUM_DEV_KEYCLOAK_ADMIN_USERNAME;
import static jenkins.plugins.horreum.HorreumIntegrationClient.getHorreumClient;
import static jenkins.plugins.horreum.HorreumIntegrationClient.instantiateClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static java.lang.System.getProperty;
import static io.hyperfoil.tools.horreum.it.Const.HORREUM_DEV_HORREUM_CONTAINER_PORT;

public class HorreumPluginTestBase implements QuarkusTestBeforeEachCallback, QuarkusTestAfterEachCallback, QuarkusTestAfterConstructCallback{
	public static final String HORREUM_UPLOAD_CREDENTIALS = "horreum-creds";
	private static final Logger LOGGER = Logger.getLogger(HorreumPluginTestBase.class.getName());

	protected static JenkinsResource j = new JenkinsResource();
	private Map<Domain, List<Credentials>> credentials = new HashMap<>();
	protected static Test dummyTest;

	void registerBasicCredential(String id, String username, String password) {
		credentials.get(Domain.global()).add(
				new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
						id, "", username, password));
		SystemCredentialsProvider scp = SystemCredentialsProvider.getInstance();
		assertNotNull(scp);
		scp.setDomainCredentialsMap(credentials);
		//TODO: register credentials using the REST API
		// https://jenkins.example.com/job/example-folder/credentials/store/folder/domain/testing/createCredentials
//		ResteasyClientBuilder.newBuilder().
	}

	@Override
	public void beforeEach(QuarkusTestMethodContext context) {

		j.setTestDescription(
			context.getTestInstance().getClass().getSimpleName(),
			context.getTestMethod().getName()
		);
		try {
			j.before();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
		HorreumGlobalConfig globalConfig = HorreumGlobalConfig.get();
		if (globalConfig != null) {
			globalConfig.setKeycloakRealm("horreum");
			globalConfig.setClientId("horreum-ui");
			globalConfig.setKeycloakBaseUrl(ConfigService.KEYCLOAK_BOOTSTRAP_URL);
			String port = getProperty(HORREUM_DEV_HORREUM_CONTAINER_PORT);
			String baseUrl = String.format("http://172.17.0.1:%s", port);
			globalConfig.setBaseUrl(baseUrl);
		} else {
			System.out.println("Can not find Horreum Global Config");
		}
		credentials.put(Domain.global(), new ArrayList<Credentials>());
		this.registerBasicCredential(HORREUM_UPLOAD_CREDENTIALS, "user", "secret");
		instantiateClient();

		KeycloakBuilder keycloakBuilder = KeycloakBuilder.builder()
			.serverUrl(getProperty("keycloak.host"))
			.realm("master")
			.username(getProperty(HORREUM_DEV_KEYCLOAK_ADMIN_USERNAME))
			.password(getProperty(HORREUM_DEV_KEYCLOAK_ADMIN_PASSWORD))
			.clientId("admin-cli")
			.resteasyClient(((ResteasyClientBuilder) ClientBuilder.newBuilder()).disableTrustManager().build());

		try (Keycloak keycloak = keycloakBuilder.build()) {
			String httpPort = getProperty("horreum.container.port");
			String httpHost = "172.17.0.1";

			ClientRepresentation clientRepresentation = keycloak.realm("horreum").clients().findByClientId("horreum-ui").get(0);
//			clientRepresentation.getWebOrigins().add("http://".concat(httpHost).concat(":").concat(httpPort));
//			clientRepresentation.getRedirectUris().add("http://".concat(httpHost).concat(":").concat(httpPort).concat("/*"));
			List<String> webOigins = clientRepresentation.getWebOrigins();
			webOigins.clear();
			webOigins.add("http://".concat(httpHost).concat(":").concat(httpPort));
			List<String> redirects = clientRepresentation.getRedirectUris();
			redirects.clear();
			redirects.add("http://".concat(httpHost).concat(":").concat(httpPort).concat("/*"));

//			envVariables.put("quarkus.oidc.credentials.secret", getClientSecret.apply("horreum"));

			keycloak.realm("horreum").clients().get(clientRepresentation.getId()).update(clientRepresentation);
		} catch (Exception e) {
			LOGGER.severe("Unable to re-configure keycloak instance: ".concat(e.getLocalizedMessage()));
		}
		dummyTest = new Test();
		dummyTest.name = "dummy-test";
		dummyTest.owner = "dev-team";
		HorreumClient client = getHorreumClient();
		dummyTest = client.testService.add(dummyTest);
	}

	@Override
	public void afterEach(QuarkusTestMethodContext context) {
		credentials.clear();
		j.afterEach(context);
	}

	@Override
	public void afterConstruct(Object testInstance) {

	}
}
