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
                    containerArgs = super.start();
                    if (containerArgs == null)
                        containerArgs = new HashMap<>();
                    containerArgs.putAll(startAMQPContainer(containerArgs));
                    containerArgs.putAll(startHorreumContainer(containerArgs));
                    horreumStarted = true;
                }
                return containerArgs;
            } catch (RuntimeException e){
                if (horreumStarted) {
                    log.info("Stopping Horreum resource");
                    stopHorreumContainer();
                    stopAMQPContainer();
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
                stopContainers();
                horreumStarted = false;
            } catch (RuntimeException e) {
                if (amqpResource != null) {
                    stopAMQPContainer();
                }
                if (horreumResource != null) {
                    stopHorreumContainer();
                }
                throw e;
            }
        }
    }
}
