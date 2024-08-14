package io.hyperfoil.tools.horreum.it;

import io.hyperfoil.tools.horreum.infra.common.HorreumResources;
import org.testcontainers.containers.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JenkinsResources extends HorreumResources {

    public static HorreumProperResource horreumResource = new HorreumProperResource();

    public static Map<String, String> startHorreumContainer(Map<String, String> initArgs) {
        Map<String, String> envVariables = new HashMap<>(initArgs);
        Optional<Network> optionalNetwork= Optional.of(HorreumResources.getNetwork());
        horreumResource.init(envVariables);
        horreumResource.start(optionalNetwork);
        return envVariables;
    }

    public static void stopHorreumContainer() {
        horreumResource.stop();
    }
}
