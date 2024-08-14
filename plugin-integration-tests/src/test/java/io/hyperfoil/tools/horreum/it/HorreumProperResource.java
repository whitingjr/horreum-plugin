package io.hyperfoil.tools.horreum.it;

import io.hyperfoil.tools.horreum.infra.common.ResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.hyperfoil.tools.horreum.it.Const.HORREUM_DEV_HORREUM_CONTAINER_PORT;
import static io.hyperfoil.tools.horreum.it.Const.HORREUM_DEV_HORREUM_IMAGE;
import static io.hyperfoil.tools.horreum.infra.common.Const.HORREUM_DEV_KEYCLOAK_NETWORK_ALIAS;
import static io.hyperfoil.tools.horreum.it.JenkinsResources.configProperties;
import static io.hyperfoil.tools.horreum.it.JenkinsResources.horreumResource;

public class HorreumProperResource implements ResourceLifecycleManager {

    private static GenericContainer<?> horreumContainer;

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

        addPropertytoEnv("quarkus.datasource.db-kind");
        addPropertytoEnv("quarkus.datasource.username");
        addPropertytoEnv("quarkus.datasource.password");
        addPropertytoEnv("quarkus.datasource.jdbc.min-size");
        addPropertytoEnv("quarkus.datasource.jdbc.max-size");
        addPropertytoEnv("quarkus.datasource.jdbc.initial-size");
        addPropertytoEnv("quarkus.oidc.auth-server-url");
        horreumContainer.withCommand("/deployments/horreum.sh ");
    }

    private void addPropertytoEnv(String name) {
        horreumContainer.withEnv(name, configProperties.getProperty(name));
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
