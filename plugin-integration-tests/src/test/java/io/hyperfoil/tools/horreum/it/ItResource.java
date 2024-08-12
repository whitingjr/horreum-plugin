package io.hyperfoil.tools.horreum.it;

import io.hyperfoil.tools.horreum.infra.common.SelfSignedCert;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;

import java.util.Map;

import static io.hyperfoil.tools.horreum.infra.common.Const.*;
import static io.hyperfoil.tools.horreum.infra.common.HorreumResources.startContainers;
import static io.hyperfoil.tools.horreum.infra.common.HorreumResources.stopContainers;
import static java.lang.System.getProperty;

public class ItResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger log = Logger.getLogger(ItResource.class);
    private static boolean started = false;


    @Override
    public Map<String, String> start() {
        synchronized (ItResource.class) {
            if (!started) {
                log.info("Starting Horreum IT resources");
                started = true;
                try {

                    String keycloakImage = getProperty(HORREUM_DEV_KEYCLOAK_IMAGE);
                    String postgresImage = getProperty(HORREUM_DEV_POSTGRES_IMAGE);

                    if ( keycloakImage == null || postgresImage == null ){
                        throw new RuntimeException("Test container images are not defined");
                    }

                    SelfSignedCert postgresSelfSignedCert = new SelfSignedCert("RSA", "SHA256withRSA", "localhost", 123);

                    //todo: pick up from configuration
                    Map<String, String> containerArgs = Map.ofEntries(
                            Map.entry(HORREUM_DEV_KEYCLOAK_ENABLED, "true"),
                            Map.entry(HORREUM_DEV_KEYCLOAK_IMAGE, keycloakImage),
                            Map.entry(HORREUM_DEV_KEYCLOAK_NETWORK_ALIAS, DEFAULT_KEYCLOAK_NETWORK_ALIAS),
                            Map.entry(HORREUM_DEV_POSTGRES_ENABLED, "true"),
                            Map.entry(HORREUM_DEV_POSTGRES_IMAGE, postgresImage),
                            Map.entry(HORREUM_DEV_POSTGRES_NETWORK_ALIAS, DEFAULT_POSTGRES_NETWORK_ALIAS),
                            Map.entry(HORREUM_DEV_POSTGRES_SSL_CERTIFICATE, postgresSelfSignedCert.getCertString()),
                            Map.entry(HORREUM_DEV_POSTGRES_SSL_CERTIFICATE_KEY, postgresSelfSignedCert.getKeyString()),
                            Map.entry(HORREUM_DEV_KEYCLOAK_DB_USERNAME, DEFAULT_KC_DB_USERNAME),
                            Map.entry(HORREUM_DEV_KEYCLOAK_DB_PASSWORD, DEFAULT_KC_DB_PASSWORD),
                            Map.entry(HORREUM_DEV_KEYCLOAK_ADMIN_USERNAME, DEFAULT_KC_ADMIN_USERNAME),
                            Map.entry(HORREUM_DEV_KEYCLOAK_ADMIN_PASSWORD, DEFAULT_KC_ADMIN_PASSWORD)
                    );
                    return startContainers(containerArgs);
                } catch (Exception e){
                    log.fatal("Could not start Horreum services", e);
                    stopContainers();
                    throw new RuntimeException("Could not start Horreum services", e);
                }
            }
        }
        return null;
    }

    @Override
    public void stop() {
        synchronized (ItResource.class) {
            try {
                log.info("Stopping Horreum IT resources");
                stopContainers();
                started = false;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }



}