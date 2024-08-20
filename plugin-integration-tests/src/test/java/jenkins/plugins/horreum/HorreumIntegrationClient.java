package jenkins.plugins.horreum;

import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.data.Test;
import org.junit.jupiter.api.Assertions;

import static io.hyperfoil.tools.horreum.it.Const.HORREUM_DEV_HORREUM_CONTAINER_PORT;

public class HorreumIntegrationClient {

    private static HorreumClient horreumClient;

    private static Test dummyTest;

    public static void instantiateClient() {
        if (horreumClient == null) {
            horreumClient = new HorreumClient.Builder()
                    .horreumUrl("http://localhost:".concat(System.getProperty(HORREUM_DEV_HORREUM_CONTAINER_PORT)))
                    .horreumUser("horreum.bootstrap")
                    .horreumPassword("secret")
                    .build();

            Assertions.assertNotNull(horreumClient);
        }
    }
    /*
        @Override
        public void beforeClass(Class<?> testClass) {
            instantiateClient();

            horreumClient.userService.addTeam("dev-team");
            horreumClient.userService.updateTeamMembers("dev-team", Map.of("horreum.bootstrap", List.of(Roles.TESTER, Roles.UPLOADER)));

            // close the client so that a new instance is created, with a new auth token with the necessary roles
            horreumClient.close();
            horreumClient = null;
        }
    */
    public static HorreumClient getHorreumClient() {
        return horreumClient;
    }
    public static Test getDummyTest() {
        return dummyTest;
    }

    public static void dereferenceClient() {
        horreumClient = null;
    }

}
