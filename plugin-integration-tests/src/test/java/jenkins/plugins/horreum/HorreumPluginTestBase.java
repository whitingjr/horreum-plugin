package jenkins.plugins.horreum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.ItemGroup;
import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.data.Test;
import io.hyperfoil.tools.horreum.api.services.ConfigService;
import io.quarkus.test.junit.callback.*;
import jakarta.inject.Inject;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
//import org.jboss.resteasy.client.jaxrs.ResteasyClient;
//import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static jenkins.plugins.horreum.HorreumIntegrationClient.getHorreumClient;
import static jenkins.plugins.horreum.HorreumIntegrationClient.instantiateClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HorreumPluginTestBase implements QuarkusTestBeforeEachCallback, QuarkusTestAfterEachCallback, QuarkusTestAfterConstructCallback {
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
		dummyTest = new Test();
		dummyTest.name = System.getProperty("horreum.test.name");
		dummyTest.owner = System.getProperty("horreum.test.owner");
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
			//TODO: set base url for horreum container
			globalConfig.setBaseUrl("CHANGE_ME_HORREUM_BASE_URL");
		} else {
			System.out.println("Can not find Horreum Global Config");
		}
		credentials.put(Domain.global(), new ArrayList<Credentials>());
		this.registerBasicCredential(HORREUM_UPLOAD_CREDENTIALS, "user", "secret");
	}

	@Override
	public void afterEach(QuarkusTestMethodContext context) {
		credentials.clear();
		j.afterEach(context);
	}

	@Override
	public void afterConstruct(Object testInstance) { // this never gets called
		credentials = new HashMap<>();
//		j = new JenkinsResource();
		instantiateClient();
	}
}
