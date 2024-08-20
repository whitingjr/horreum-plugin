package io.hyperfoil.tools.horreum.it;

import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.hyperfoil.tools.horreum.it.JenkinsResources.*;

public class JenkinsItResource extends ItResource {

    private static final Logger log = Logger.getLogger(JenkinsItResource.class);
    private static boolean horreumStarted = false;

    @Override
    public Map<String, String> start() {
        Map<String, String> containerArgs = null;
        synchronized (JenkinsItResource.class) {
            try {
                if (!horreumStarted) {
                    log.info("Starting Horreum resource");
                    log.info("system properties contains postgres mapped port pre-check: " + System.getProperties().containsKey("postgres.container.port"));
                    log.info("system properties contains keycloak mapped port pre-check: " + System.getProperties().containsKey("keycloak.container.port"));
                    containerArgs = super.start();
                    if (containerArgs == null)
                        containerArgs = new HashMap<>();
                    log.info("system properties contains postgres mapped port post-check: " + System.getProperties().containsKey("postgres.container.port"));
                    log.info("system properties contains keycloak mapped port post-check: " + System.getProperties().containsKey("keycloak.container.port"));

                    containerArgs.putAll(startHorreumContainer(containerArgs));
                    horreumStarted = true;
                }
                return containerArgs;
            } catch (RuntimeException e){
                if (horreumStarted) {
                    log.info("Stopping Horreum resource");
                    stopHorreumContainer();
                }
                throw new RuntimeException("Could not start Jenkins services", e);
            }

        }
    }

    @Override
    public void stop() {
        synchronized (JenkinsItResource.class) {
            try {
                log.info("Stopping Horreum IT resources");
                super.stop();
                horreumStarted = false;
            } catch (RuntimeException e) {
                if (horreumResource != null) {
                    stopHorreumContainer();
                }
                throw e;
            }
        }
    }
}
