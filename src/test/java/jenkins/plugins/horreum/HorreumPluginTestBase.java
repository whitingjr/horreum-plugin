package jenkins.plugins.horreum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import io.hyperfoil.tools.HorreumTestBase;

public class HorreumPluginTestBase extends HorreumTestBase {
	public static final String HORREUM_UPLOAD_CREDENTIALS = "horreum-creds";

	@RegisterExtension
	public JenkinsExtension j = new JenkinsExtension();
	private Map<Domain, List<Credentials>> credentials;

	void registerBasicCredential(String id, String username, String password) {
		credentials.get(Domain.global()).add(
				new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
						id, "", username, password));
		SystemCredentialsProvider.getInstance().setDomainCredentialsMap(credentials);
	}

	@BeforeEach
	public void init() {
		credentials = new HashMap<>();
		credentials.put(Domain.global(), new ArrayList<Credentials>());
		this.registerBasicCredential(HORREUM_UPLOAD_CREDENTIALS, "user", "secret");

		HorreumGlobalConfig globalConfig = HorreumGlobalConfig.get();
		if (globalConfig != null) {
			globalConfig.setKeycloakRealm("horreum");
			globalConfig.setClientId("horreum-ui");
			globalConfig.setKeycloakBaseUrl(HORREUM_KEYCLOAK_BASE_URL);
			globalConfig.setBaseUrl(HORREUM_BASE_URL);
			globalConfig.setCredentialsId(HORREUM_UPLOAD_CREDENTIALS);
		} else {
			System.out.println("Can not find Horreum Global Config");
		}

		//Lookup Credentials from secrets
		HorreumGlobalConfig.getKeycloakAuthentication().resolveCredentials();
	}
}
