package jenkins.plugins.horreum_upload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
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
import com.google.common.io.CharStreams;

import hudson.util.Secret;

public class HorreumPluginTestBase {
	private static final String HORREUM_KEYCLOAK_REALM = "horreum";
	private static final String HORREUM_KEYCLOAK_BASE_URL = "http://172.17.0.1:8180";
	private static final String HORREUM_BASE_URL = "http://localhost:8082/api/run/data";
	private static final String HORREUM_CLIENT_ID = "horreum-ui";

	public static final String HORREUM_UPLOAD_CREDENTIALS = "horreum-creds";
	private static final String HORREUM_CLIENT_SECRET = "horreum-secret";

	//	@ClassRule  //Inject Docker compose container env
	public static DockerComposeContainer environment = null;

	private static ServerRunning SERVER;
	static final String ALL_IS_WELL = "All is well";
	private static boolean spinUpHorreumInfra = true;
	private static boolean dumpHorreumLogs = true;


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


	static void registerHandler(String target, HttpMode method, SimpleHandler handler) {
		Map<HttpMode, Handler> handlerByMethod = SERVER.handlersByMethodByTarget.get(target);
		if (handlerByMethod == null) {
			SERVER.handlersByMethodByTarget.put(target, handlerByMethod = new HashMap<>());
		}
		handlerByMethod.put(method, handler);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		if (SERVER != null) {
			return;
		}
		SERVER = new ServerRunning();

		if (spinUpHorreumInfra) {

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
		if ( dumpHorreumLogs ){
			Optional<ContainerState> containerState =  environment.getContainerByServiceName("horreum_1"); //TODO: dynamic resolve
			if (containerState.isPresent()){
				String logs = containerState.get().getLogs(OutputType.STDOUT);
				File tmpFile = File.createTempFile("horreum-jenkins", ".log");
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
				writer.write(logs);
				writer.close();
				System.out.println("Logs written to: " + tmpFile.getAbsolutePath());
			}

		}
		if (environment != null){
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

		//TODO register Horreum global config
		HorreumUploadGlobalConfig globalConfig = HorreumUploadGlobalConfig.get(); //  j.jenkins.getDescriptorList(GlobalConfiguration.class).get(HorreumUploadGlobalConfig.class);
		if (globalConfig != null) {
			globalConfig.setBaseUrl("http://127.0.0.1:8082");
			globalConfig.setKeycloakRealm(HORREUM_KEYCLOAK_REALM);
			globalConfig.setClientId(HORREUM_CLIENT_ID);
			globalConfig.setKeycloakBaseUrl(HORREUM_KEYCLOAK_BASE_URL);
			globalConfig.setBaseUrl(HORREUM_BASE_URL);

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

	public static abstract class SimpleHandler extends DefaultHandler {
		@Override
		public final void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			doHandle(target, baseRequest, request, response);
			baseRequest.setHandled(true);
		}

		String requestBody(HttpServletRequest request) throws IOException {
			try (BufferedReader reader = request.getReader()) {
				return CharStreams.toString(reader);
			}
		}

		void okAllIsWell(HttpServletResponse response) throws IOException {
			okText(response, ALL_IS_WELL);
		}

		void okText(HttpServletResponse response, String body) throws IOException {
			body(response, HttpServletResponse.SC_OK, ContentType.TEXT_PLAIN, body);
		}

		void body(HttpServletResponse response, int status, ContentType contentType, String body) throws IOException {
			response.setContentType(contentType != null ? contentType.toString() : "");
			response.setStatus(status);
			response.getWriter().append(body);
		}

		abstract void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
	}

	private static final class ServerRunning {
		private final Server server;
		private final int port;
		private final String baseURL;
		private final Map<String, Map<HttpMode, Handler>> handlersByMethodByTarget = new HashMap<>();

		private ServerRunning() throws Exception {
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

//	void createNewTest(String dummy) {
//		HorreumUploadConfig config = new HorreumUploadConfig();
//
//		HorreumUploadExecutionContext.from(config, null, () -> null, () -> null);
//
//
//	}

}
