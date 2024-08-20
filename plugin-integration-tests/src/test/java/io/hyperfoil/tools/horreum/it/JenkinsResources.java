package io.hyperfoil.tools.horreum.it;

import io.hyperfoil.tools.horreum.infra.common.HorreumResources;
import org.testcontainers.containers.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.hyperfoil.tools.horreum.it.Const.*;
import static java.lang.System.getProperty;

public class JenkinsResources extends HorreumResources {

    public static HorreumProperResource horreumResource = new HorreumProperResource();

    public static Map<String, String> startHorreumContainer(Map<String, String> initArgs) {
        Map<String, String> envVariables = new HashMap<>(initArgs);
        Optional<Network> optionalNetwork= Optional.of(HorreumResources.getNetwork());

        envVariables.put(HORREUM_DEV_HORREUM_IMAGE, getProperty("dev.images.horreum"));

        horreumResource.init(envVariables);
        horreumResource.start(optionalNetwork);
        return envVariables;
    }

    public static void stopHorreumContainer() {
        horreumResource.stop();
    }
}
