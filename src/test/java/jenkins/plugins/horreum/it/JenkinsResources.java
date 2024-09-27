package jenkins.plugins.horreum.it;

import io.hyperfoil.tools.horreum.infra.common.HorreumResources;
import io.hyperfoil.tools.horreum.infra.common.resources.ArtemisMQResource;
import io.hyperfoil.tools.horreum.infra.common.resources.HorreumResource;
import org.testcontainers.containers.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JenkinsResources extends HorreumResources {
    public static ArtemisMQResource amqpResource = new ArtemisMQResource();
    public static HorreumResource horreumResource = new HorreumResource();

    public static Map<String, String> startAMQPContainer(Map<String, String> initArgs) {
        Map<String, String> envVariables = new HashMap<>(initArgs);
        Optional<Network> optionalNetwork= Optional.of(HorreumResources.getNetwork());

        amqpResource.init(envVariables);
        envVariables.putAll(amqpResource.start(optionalNetwork));
        return envVariables;
    }


    public static Map<String, String> startHorreumContainer(Map<String, String> initArgs) {
        Map<String, String> envVariables = new HashMap<>(initArgs);
        Optional<Network> optionalNetwork= Optional.of(HorreumResources.getNetwork());
        horreumResource.init(envVariables);
        envVariables.putAll(horreumResource.start(optionalNetwork));
        return envVariables;
    }

    public static void stopContainers() {
        stopAMQPContainer();
        stopHorreumContainer();
    }

    public static void stopAMQPContainer() {
        if (amqpResource != null) {
            amqpResource.stop();
        }
    }
    public static void stopHorreumContainer() {
        if (horreumResource != null) {
            horreumResource.stop();
        }
    }
}
