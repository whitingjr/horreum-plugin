package jenkins.plugins.horreum_upload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame.OutputType;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.util.Secret;

public class HorreumPluginTestBase {

	static Properties configProperties;
	private static final String HORREUM_KEYCLOAK_REALM = "horreum";
	private static final String HORREUM_KEYCLOAK_BASE_URL;
	private static final String HORREUM_BASE_URL;
	private static final String HORREUM_BASE_PATH;
	private static final String HORREUM_CLIENT_ID;

	public static final String HORREUM_UPLOAD_CREDENTIALS = "horreum-creds";
	private static final String HORREUM_CLIENT_SECRET = "horreum-secret";

	private static boolean START_HORREUM_INFRA;
	private static boolean HORREUM_DUMP_LOGS;

	static {
		configProperties = new Properties();
		InputStream propertyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("testcontainers/env.properties"); //TODO: make configurable
		try {
			if (propertyStream != null) {
				configProperties.load(propertyStream);

				HORREUM_KEYCLOAK_BASE_URL = configProperties.getProperty("horreum.keycloak.base-url");
				HORREUM_BASE_URL = configProperties.getProperty("horreum.base-url");
				HORREUM_BASE_PATH = configProperties.getProperty("horreum.base-path");
				HORREUM_CLIENT_ID = configProperties.getProperty("horreum.client-id");

				START_HORREUM_INFRA = Boolean.valueOf(configProperties.getProperty("horreum.start-infra"));
				HORREUM_DUMP_LOGS = Boolean.valueOf(configProperties.getProperty("horreum.dump-logs"));
			} else {
				throw new RuntimeException("Could not load test configuration");
			}
		} catch (IOException ioException) {
			throw new RuntimeException("Failed to load configuration properties");
		}
	}

	//	@ClassRule  //Inject Docker compose container env
	public static DockerComposeContainer environment = null;

	private static JenkinsServer SERVER;
	static final String ALL_IS_WELL = "All is well";


	@Rule
	public JenkinsRule j = new JenkinsRule();
	private Map<Domain, List<Credentials>> credentials;

	final String baseURL() {
		return SERVER.baseURL;
	}

	void registerBasicCredential(String id, String username, String password) {
		credentials.get(Domain.global()).add(
				new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
						id, "", username, password));
		SystemCredentialsProvider.getInstance().setDomainCredentialsMap(credentials);
	}


	void registerSecret(String id, String secret) {
		credentials.get(Domain.global()).add(
				new StringCredentialsImpl(CredentialsScope.GLOBAL, id, "", Secret.fromString(secret)));
		SystemCredentialsProvider.getInstance().setDomainCredentialsMap(credentials);
	}



	@BeforeClass
	public static void beforeClass() throws Exception {
		if (SERVER != null) {
			return;
		}
		SERVER = new JenkinsServer();

		if (START_HORREUM_INFRA) {

			environment = new DockerComposeContainer(new File("src/test/resources/testcontainers/docker-compose.yml"))
					.withExposedService("postgres_1", 5432)
			;
			environment.start();
			//wait for Horreum infrastructure to spin up
			System.out.println("Waiting for Horreum infrastructure to start");
			Optional<ContainerState> optionalContainer = environment.getContainerByServiceName("horreum_1");
			if (optionalContainer.isPresent()) {
				ContainerState horreumState = optionalContainer.get();
				while (!horreumState.getLogs(OutputType.STDOUT).contains("started in")) {
					Thread.sleep(1000);
				}
			} else {
				System.out.println("Could not find running Horreum container");
			}
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (SERVER != null) {
			SERVER.server.stop();
			SERVER = null;
		}
		if (HORREUM_DUMP_LOGS) {
			Optional<ContainerState> containerState = environment.getContainerByServiceName("horreum_1"); //TODO: dynamic resolve
			if (containerState.isPresent()) {
				String logs = containerState.get().getLogs(OutputType.STDOUT);
				File tmpFile = File.createTempFile("horreum-jenkins", ".log");
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
				writer.write(logs);
				writer.close();
				System.out.println("Logs written to: " + tmpFile.getAbsolutePath());
			}

		}
		if (environment != null) {
			environment.stop();
			environment = null;
		}
	}

	@Before
	public void init() {
		credentials = new HashMap<>();
		credentials.put(Domain.global(), new ArrayList<Credentials>());
		this.registerBasicCredential(HORREUM_UPLOAD_CREDENTIALS, "user", "secret");
		this.registerSecret(HORREUM_CLIENT_SECRET, "96af3ebe-bf3c-4c70-8ca9-ee952ab42fec");

		HorreumUploadGlobalConfig globalConfig = HorreumUploadGlobalConfig.get();
		if (globalConfig != null) {
			globalConfig.setKeycloakRealm(HORREUM_KEYCLOAK_REALM);
			globalConfig.setClientId(HORREUM_CLIENT_ID);
			globalConfig.setKeycloakBaseUrl(HORREUM_KEYCLOAK_BASE_URL);
			globalConfig.setBaseUrl(HORREUM_BASE_URL + HORREUM_BASE_PATH);

			globalConfig.setCredentialsId(HORREUM_UPLOAD_CREDENTIALS);
			globalConfig.setClientSecretId(HORREUM_CLIENT_SECRET);
		} else {
			System.out.println("Can not find Horreum Global Config");
		}

	}

	@After
	public void cleanHandlers() {
		if (SERVER != null) {
			SERVER.handlersByMethodByTarget.clear();
		}
	}

	private static final class JenkinsServer {
		private final Server server;
		private final int port;
		private final String baseURL;
		private final Map<String, Map<HttpMode, Handler>> handlersByMethodByTarget = new HashMap<>();

		private JenkinsServer() throws Exception {
			server = new Server();
			ServerConnector connector = new ServerConnector(server);
			server.setConnectors(new Connector[]{connector});

			ContextHandler context = new ContextHandler();
			context.setContextPath("/");
			context.setHandler(new DefaultHandler() {
				@Override
				public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
					Map<HttpMode, Handler> handlerByMethod = handlersByMethodByTarget.get(target);
					if (handlerByMethod != null) {
						Handler handler = handlerByMethod.get(HttpMode.valueOf(request.getMethod()));
						if (handler != null) {
							handler.handle(target, baseRequest, request, response);
							return;
						}
					}

					super.handle(target, baseRequest, request, response);
				}
			});
			server.setHandler(context);

			server.start();
			port = connector.getLocalPort();
			baseURL = "http://127.0.0.1:" + port;
		}
	}
}
