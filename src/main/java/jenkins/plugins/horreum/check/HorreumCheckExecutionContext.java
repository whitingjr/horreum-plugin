package jenkins.plugins.horreum.check;

import hudson.EnvVars;
import hudson.model.TaskListener;
import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.alerting.Variable;
import io.hyperfoil.tools.horreum.api.data.Test;
import io.hyperfoil.tools.horreum.entity.alerting.Variable;
import io.hyperfoil.tools.horreum.entity.json.Test;
import jakarta.ws.rs.WebApplicationException;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumGlobalConfig;

import javax.ws.rs.WebApplicationException;
import java.io.PrintStream;
import java.util.List;

public class HorreumCheckExecutionContext extends BaseExecutionContext<String> {

    private static final long serialVersionUID = 1l; //TODO
    private final HorreumCheckConfig config;

    static HorreumCheckExecutionContext from(HorreumCheckConfig config,
                                             EnvVars envVars, TaskListener listener){
        String url = envVars != null ? envVars.expand(HorreumGlobalConfig.get().getBaseUrl()) : HorreumGlobalConfig.get().getBaseUrl();
        TaskListener taskListener = config.getQuiet() ? TaskListener.NULL : listener;

        HorreumCheckExecutionContext context = new HorreumCheckExecutionContext( url, config, taskListener.getLogger());
        return context;
    }

    private HorreumCheckExecutionContext( String url, HorreumCheckConfig config,
                                          PrintStream logger) {
        super(url, config.getCredentials(), logger);
        this.config = config;
    }

    @Override
    protected String invoke(HorreumClient client){
        Test test = client.testService.getByNameOrId(config.getTest());
        if (test == null){
            throw new WebApplicationException("Cannot find Test!");
        }
        List<Variable> variableList = client.alertingService.variables(test.id);
        if (variableList.isEmpty())
            return Boolean.FALSE.toString();

        String profile = config.getProfile().trim();
        return Boolean.valueOf( variableList.stream().anyMatch(v -> v.group.equalsIgnoreCase(profile)) ).toString();
    }
}
