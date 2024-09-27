package jenkins.plugins.horreum.junit;

import io.hyperfoil.tools.HorreumClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class HorreumTestClientExtension extends HorreumTestExtension implements BeforeAllCallback {
    private static HorreumClient horreumClient;

    public static void instantiateClient() {
        if (horreumClient == null) {
            String horreumUrl = "http://".concat(horreumHost).concat(":").concat(horreumPort).concat("/");
            horreumClient = new HorreumClient.Builder()
                .horreumUrl(horreumUrl)
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

    @Override
    public void beforeAll(ExtensionContext extensionContext)  {
    }
}
