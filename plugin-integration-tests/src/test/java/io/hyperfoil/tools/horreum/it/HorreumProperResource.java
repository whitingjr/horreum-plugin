package io.hyperfoil.tools.horreum.it;

import io.hyperfoil.tools.horreum.infra.common.ResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.hyperfoil.tools.horreum.it.Const.HORREUM_DEV_HORREUM_CONTAINER_PORT;
import static io.hyperfoil.tools.horreum.it.Const.HORREUM_DEV_HORREUM_IMAGE;
import static io.hyperfoil.tools.horreum.infra.common.Const.*;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

public class HorreumProperResource implements ResourceLifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(HorreumProperResource.class);
    private static GenericContainer<?> horreumContainer;
    private StringBuilder javaOptions = new StringBuilder();
    private StringBuilder format = new StringBuilder();

    private String networkAlias = "";

    public void init(Map<String, String> initArgs) {
        if (!initArgs.containsKey(HORREUM_DEV_HORREUM_IMAGE)) {
            throw new RuntimeException("Horreum dev image argument not configured");
        }

        final String HORREUM_IMAGE = initArgs.get(HORREUM_DEV_HORREUM_IMAGE);

        networkAlias = initArgs.get(HORREUM_DEV_KEYCLOAK_NETWORK_ALIAS);

        horreumContainer = (initArgs.containsKey(HORREUM_DEV_HORREUM_CONTAINER_PORT)) ?
            new FixedHostPortGenericContainer<>(HORREUM_IMAGE).withFixedExposedPort(Integer.parseInt(initArgs.get(HORREUM_DEV_HORREUM_CONTAINER_PORT)), 8080) :
            new GenericContainer<>(HORREUM_IMAGE).withExposedPorts(8080);
        String keycloakUrl = String.format("%s/realms/horreum", getProperty("keycloak.host"));

        prop("quarkus.oidc.auth-server-url", keycloakUrl);
        prop("quarkus.keycloak.admin-client.server-url", keycloakUrl);
        prop("quarkus.oidc.token.issuer", "https://server.example.com ");
        prop("smallrye.jwt.sign.key.location", "/privateKey.jwk");
        prop("horreum.url", "http://localhost:8081");
        prop("horreum.test-mode", "true");
        prop("horreum.privacy", "/path/to/privacy/statement/link");
        prop("quarkus.datasource.migration.devservices.enabled", "false");
        prop("quarkus.datasource.migration.jdbc.url", getProperty("quarkus.datasource.migration.jdbc.url"));
        String javaOpts = String.format(" %s ", javaOptions.toString());

        horreumContainer
            .withEnv("JAVA_OPTIONS", javaOpts)
            .withCommand("/deployments/horreum.sh ");
    }

    private void prop(String key, String value) {
        javaOptions.append(" -D").append(key).append("=").append(value).append(" ");
        format.append(" %s ");
    }

    @Override
    public Map<String, String> start(Optional<Network> network) {
        if (horreumContainer == null) {
            return Collections.emptyMap();
        }
        if (!network.isPresent()) {
            horreumContainer.withNetwork(network.get());
            horreumContainer.withNetworkAliases(networkAlias);
        }

        horreumContainer.start();
        String horreumContainerName = horreumContainer.getContainerName().replaceAll("/", "");
        Integer port = horreumContainer.getMappedPort(8080);

        setProperty(HORREUM_DEV_HORREUM_CONTAINER_PORT, port.toString());

        Map<String,String> env = Map.of("horreum.container.name", horreumContainerName,
            "horreum.container.port", port.toString()
        );
        env.forEach(System::setProperty);
        log.info(env.toString());
        return env;
    }

    @Override
    public void stop() {
        if (horreumContainer != null) {
            horreumContainer.stop();
        }
    }
}
