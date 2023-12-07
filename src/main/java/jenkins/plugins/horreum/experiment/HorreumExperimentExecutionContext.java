package jenkins.plugins.horreum.experiment;

import hudson.EnvVars;
import hudson.model.TaskListener;
import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.services.ExperimentService;
import io.hyperfoil.tools.horreum.api.services.RunService;
import jakarta.ws.rs.WebApplicationException;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumGlobalConfig;

import java.io.PrintStream;
import java.util.List;

public class HorreumExperimentExecutionContext extends BaseExecutionContext<String> {
    private static final long serialVersionUID = 1l;
    
    private final HorreumExperimentConfig config;

    static HorreumExperimentExecutionContext from(HorreumExperimentConfig config,
                                             EnvVars envVars, TaskListener listener){
        String url = envVars != null ? envVars.expand(HorreumGlobalConfig.get().getBaseUrl()) : HorreumGlobalConfig.get().getBaseUrl();
        TaskListener taskListener = config.getQuiet() ? TaskListener.NULL : listener;

        HorreumExperimentExecutionContext context = new HorreumExperimentExecutionContext( url, config, taskListener.getLogger());
        return context;
    }

    private HorreumExperimentExecutionContext (String url, HorreumExperimentConfig config,
                                              PrintStream logger){
        super(url, config.getCredentials(), logger);
        this.config = config;
    }

    @Override
    protected String invoke(HorreumClient client){
        client.runService.waitForDatasets(Integer.parseInt(config.getId()));
        RunService.RunsSummary summary = client.runService.listBySchema(config.getUri(), config.getLimit(), config.getPage(), config.getSort(), config.getDirection());
        if (summary == null){
            throw new WebApplicationException("Cannot find a summary for the uploaded run");
        }
        int datasetId = summary.runs.get(0).datasets[0];
        List<ExperimentService.ExperimentResult> experimentResults = client.experimentService.runExperiments(datasetId);
        String profile = config.getProfile();
        return Boolean.valueOf( experimentResults.stream().filter(r -> r.profile.name.equalsIgnoreCase(profile)).noneMatch(r -> r.results.values().stream().anyMatch(cr -> cr.overall.equals(ExperimentService.BetterOrWorse.WORSE)))).toString();
    }
}
