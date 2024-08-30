package io.hyperfoil.tools.horreum.infra.common.resoures;

import io.hyperfoil.tools.horreum.infra.common.ResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.hyperfoil.tools.horreum.it.Const.*;

public class AMQPResource implements ResourceLifecycleManager {
    private GenericContainer<?> amqpContainer;
    private boolean inContainer = false;
    private String networkAlias = "";

    @Override
    public void init(Map<String, String> initArgs) {
        ResourceLifecycleManager.super.init(initArgs);
        if (initArgs.containsKey(HORREUM_DEV_AMQP_ENABLED) && initArgs.get(HORREUM_DEV_AMQP_ENABLED).equals("true")) {
            if (!initArgs.containsKey(HORREUM_DEV_AMQP_IMAGE)) {
                throw new RuntimeException("Arguments did not contain AMQP image.");
            }
            final String AMQP_IMAGE = initArgs.get(HORREUM_DEV_AMQP_IMAGE);
            inContainer = initArgs.containsKey("inContainer") && initArgs.get("inContainer").equals("true");
            networkAlias = initArgs.get(HORREUM_DEV_AMQP_NETWORK_ALIAS);

            amqpContainer = new GenericContainer<>(AMQP_IMAGE);
            amqpContainer.withEnv("ARTEMIS_USER", initArgs.get("amqp-username"))
                .withEnv("ARTEMIS_PASSWORD", initArgs.get("amqp-password"))
                .withEnv("AMQ_ROLE", "admin")
                .withEnv("EXTRA_ARGS", " --role admin --name broker --allow-anonymous --force --no-autotune --mapped --no-fsync  --relax-jolokia ")
                .withExposedPorts(5672)
                .withCopyFileToContainer(MountableFile.forClasspathResource("amqp/broker.xml"), "/var/lib/artemis-instance/etc-override/broker.xml");
        }
    }

    @Override
    public Map<String, String> start(Optional<Network> network) {
        if (amqpContainer == null) {
            return Collections.emptyMap();
        }
        if (network.isPresent()) {
            amqpContainer.withNetwork(network.get());
            amqpContainer.withNetworkAliases(networkAlias);
        }
        amqpContainer.start();
        String mappedPort = amqpContainer.getMappedPort(5672).toString();
        String host = inContainer ? IN_CONTAINER_IP : "localhost";

        return Map.of("amqp.mapped.port", mappedPort,
            "amqp.host", host);
    }

    @Override
    public void stop() {
        if (amqpContainer != null) {
            amqpContainer.stop();
        }
    }

    public GenericContainer<?> getAmqpContainer() {
        return this.amqpContainer;
    }
}
