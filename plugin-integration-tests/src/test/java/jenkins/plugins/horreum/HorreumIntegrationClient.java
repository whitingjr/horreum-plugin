package jenkins.plugins.horreum;

import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.data.Test;
import io.hyperfoil.tools.horreum.svc.Roles;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

import static io.hyperfoil.tools.horreum.it.Const.*;
import static java.lang.System.getProperty;

public class HorreumIntegrationClient {

    private static HorreumClient horreumClient;
//    private static boolean isDevTeamInitialised = false;
    private static Test dummyTest;

    public static void instantiateClient() {
        String port = getProperty(HORREUM_DEV_HORREUM_CONTAINER_PORT);
        if (horreumClient == null) {
            horreumClient = new HorreumClient.Builder()
                    .horreumUrl("http://172.17.0.1:".concat(port).concat("/"))
                    .horreumUser("user")
                    .horreumPassword("secret")
                    .build();

            Assertions.assertNotNull(horreumClient);
        }

//        if (!isDevTeamInitialised) {
//            horreumClient.userService.addTeam("dev-team");
//            horreumClient.userService.updateTeamMembers("dev-team", Map.of("user", List.of(Roles.TESTER, Roles.UPLOADER)));
//
//            // close the client so that a new instance is created, with a new auth token with the necessary roles
//            horreumClient.close();
//            horreumClient = null;
//            isDevTeamInitialised = true;
//        }
    }

    public static HorreumClient getHorreumClient() {
        if (horreumClient == null) {
            instantiateClient();
        }
        return horreumClient;
    }
    public static Test getDummyTest() {
        return dummyTest;
    }

    public static void closeClient() {
        if (horreumClient != null){
            horreumClient.close();
        }
        horreumClient = null;
    }
}
