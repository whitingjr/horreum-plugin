package io.hyperfoil.tools.horreum.infra.common.resoures;

import io.hyperfoil.tools.horreum.infra.common.ResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class AMQPResource implements ResourceLifecycleManager {
    private static final Logger log = Logger.getLogger(AMQPResource.class);
    public static final String AMQP_CONFIG_PROPERTIES = "amqp.config.properties";
    private GenericContainer<?> amqpContainer;

    private String networkAlias = "";

    @Override
    public void init(Map<String, String> initArgs) {
        ResourceLifecycleManager.super.init(initArgs);

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

        return Map.of("amqp.mapped.port", mappedPort);
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
