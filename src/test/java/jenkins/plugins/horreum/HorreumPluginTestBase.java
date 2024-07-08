package jenkins.plugins.horreum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import static jenkins.plugins.horreum.HorreumIntegrationClient.instantiateClient;

public class HorreumPluginTestBase implements QuarkusTestBeforeEachCallback, QuarkusTestAfterEachCallback, QuarkusTestAfterConstructCallback {
	public static final String HORREUM_UPLOAD_CREDENTIALS = "horreum-creds";

	protected JenkinsResource j;
	private Map<Domain, List<Credentials>> credentials;
	protected HorreumClient horreumClient;
	protected Test dummyTest;

	void registerBasicCredential(String id, String username, String password) {
		credentials.get(Domain.global()).add(
				new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
						id, "", username, password));
		SystemCredentialsProvider.getInstance().setDomainCredentialsMap(credentials);
	}

	@Override
	public void beforeEach(QuarkusTestMethodContext context) {
		credentials.put(Domain.global(), new ArrayList<Credentials>());
		this.registerBasicCredential(HORREUM_UPLOAD_CREDENTIALS, "user", "secret");

		HorreumGlobalConfig globalConfig = HorreumGlobalConfig.get();
		if (globalConfig != null) {
			globalConfig.setKeycloakRealm("horreum");
			globalConfig.setClientId("horreum-ui");
			globalConfig.setKeycloakBaseUrl(ConfigService.KEYCLOAK_BOOTSTRAP_URL);
			globalConfig.setBaseUrl("http://localhost:8080/");
		} else {
			System.out.println("Can not find Horreum Global Config");
		}
		j.setTestDescription(
			context.getTestInstance().getClass().getSimpleName(),
			context.getTestMethod().getName()
		);
		try {
			j.before();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}

	}

	@Override
	public void afterEach(QuarkusTestMethodContext context) {
		credentials.clear();
		j.afterEach(context);
	}

	@Override

	public void afterConstruct(Object testInstance) {
		credentials = new HashMap<>();
		j = new JenkinsResource();
		instantiateClient();
	}
}
