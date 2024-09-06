package io.hyperfoil.tools.horreum.it;

import io.hyperfoil.tools.horreum.infra.common.HorreumResources;
import io.hyperfoil.tools.horreum.infra.common.ResourceLifecycleManager;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.hyperfoil.tools.horreum.it.Const.*;
import static java.lang.System.setProperty;

public class HorreumProperResource implements ResourceLifecycleManager {

    private static GenericContainer<?> horreumContainer;
    private StringBuilder javaOptions = new StringBuilder();

    private String networkAlias = "";

    public void init(Map<String, String> initArgs) {
        if (!initArgs.containsKey(HORREUM_DEV_HORREUM_IMAGE)) {
            throw new RuntimeException("Horreum dev image argument not configured");
        }

        final String HORREUM_IMAGE = initArgs.get(HORREUM_DEV_HORREUM_IMAGE);

        networkAlias = initArgs.get(HORREUM_DEV_HORREUM_NETWORK_ALIAS);
        Network network = HorreumResources.getNetwork();

        horreumContainer = (initArgs.containsKey(HORREUM_DEV_HORREUM_CONTAINER_PORT)) ?
            new FixedHostPortGenericContainer<>(HORREUM_IMAGE).withFixedExposedPort(Integer.parseInt(initArgs.get(HORREUM_DEV_HORREUM_CONTAINER_PORT)), 8080) :
            new GenericContainer<>(HORREUM_IMAGE).withExposedPorts(8080);
        String keycloakHost = initArgs.get("keycloak.host");

        keycloakHost = keycloakHost.replace("localhost", networkAlias);
        String keycloakUrl = String.format("%s/realms/horreum", keycloakHost);
        String horreumUrl = "http://" + networkAlias + ":8081";
        String jdbcUrl = initArgs.get("quarkus.datasource.jdbc.url");
        jdbcUrl = jdbcUrl.replace("localhost", networkAlias);

        prop("horreum.keycloak.url", keycloakHost);
        prop("quarkus.oidc.auth-server-url", keycloakUrl);
        prop("quarkus.keycloak.admin-client.server-url", keycloakHost);
        prop("quarkus.keycloak.admin-client.client-id", "horreum");
        prop("quarkus.keycloak.admin-client.realm", "horreum");
        prop("quarkus.keycloak.admin-client.client-secret", "**********");
        prop("quarkus.keycloak.admin-client.grant-type", "CLIENT_CREDENTIALS");
        prop("quarkus.oidc.token.issuer", "https://server.example.com ");
        prop("smallrye.jwt.sign.key.location", "/privateKey.jwk");
        prop("horreum.url", horreumUrl);
        prop("horreum.test-mode", "true");
        prop("horreum.privacy", "/path/to/privacy/statement/link");
        prop("quarkus.datasource.migration.devservices.enabled", "false");
        prop("quarkus.datasource.jdbc.url", jdbcUrl);
        prop("quarkus.datasource.migration.jdbc.url", jdbcUrl);
        prop("quarkus.datasource.jdbc.additional-jdbc-properties.sslmode", "require");
        prop("amqp-host", initArgs.get("amqp.host"));
        prop("amqp-port", initArgs.get("amqp.mapped.port"));
        prop("amqp-username", initArgs.get("amqp-username"));
        prop("amqp-password", initArgs.get("amqp-password"));
        prop("amqp-reconnect-attempts" , "100");
        prop("amqp-reconnect-interval", "1000");
        prop("quarkus.profile", "test");
        prop("quarkus.test.profile", "test");
        prop("horreum.bootstrap.password", "secret");
        prop("horreum.roles.provider", "database");
        String javaOpts = javaOptions.toString();

        horreumContainer
            .withEnv("JAVA_OPTIONS", javaOpts)
            .withNetwork(network)
            .withNetworkAliases(networkAlias)
            .withCommand("/deployments/horreum.sh ");
    }

    private void prop(String key, String value) {
        javaOptions.append(" -D").append(key).append("=").append(value).append(" ");
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

        return Map.of("horreum.container.name", horreumContainerName,
            "horreum.container.port", port.toString()
        );
    }

    @Override
    public void stop() {
        if (horreumContainer != null) {
            horreumContainer.stop();
        }
    }
}
