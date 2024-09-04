package jenkins.plugins.horreum;

import io.hyperfoil.tools.HorreumClient;
import org.junit.jupiter.api.Assertions;

import static io.hyperfoil.tools.horreum.it.Const.*;
import static java.lang.System.getProperty;

public class HorreumIntegrationClient {

    private static HorreumClient horreumClient;

    public static void instantiateClient() {
        String port = getProperty(HORREUM_DEV_HORREUM_CONTAINER_PORT);
        if (horreumClient == null) {
            horreumClient = new HorreumClient.Builder()
                    .horreumUrl("http://172.17.0.1:".concat(port).concat("/"))
                    .horreumUser("horreum.bootstrap")
                    .horreumPassword("secret")
                    .build();

            Assertions.assertNotNull(horreumClient);
        }
    }

    public static HorreumClient getHorreumClient() {
        if (horreumClient == null) {
            instantiateClient();
        }
        return horreumClient;
    }
    public static void closeClient() {
        if (horreumClient != null){
            horreumClient.close();
        }
        horreumClient = null;
    }
}
