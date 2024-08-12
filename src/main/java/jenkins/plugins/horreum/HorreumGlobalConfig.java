package jenkins.plugins.horreum;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.XStream2;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import jenkins.plugins.horreum.auth.KeycloakAuthentication;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

@Extension
public class HorreumGlobalConfig extends GlobalConfiguration {

    private static final XStream2 XSTREAM2 = new XStream2();

	 private String baseUrl;
	 private List<Long> retries = LongStream.of(5, 10, 30, 60, 120).boxed().collect(Collectors.toList());
	 private final KeycloakAuthentication keycloak = new KeycloakAuthentication();

    public HorreumGlobalConfig() {
        load();
    }

    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void xStreamCompatibility() {
        XSTREAM2.addCompatibilityAlias("jenkins.plugins.horreum.HttpRequest$DescriptorImpl", HorreumGlobalConfig.class);
        XSTREAM2.addCompatibilityAlias("jenkins.plugins.horreum.util.NameValuePair", HttpRequestNameValuePair.class);
    }

    @Override
    protected XmlFile getConfigFile() {
        File rootDir = Jenkins.getInstance().getRootDir();
        File xmlFile = new File(rootDir, "jenkins.plugins.horreum.HorreumUpload.xml");
        return new XmlFile(XSTREAM2, xmlFile);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json)
    throws FormException
    {
        req.bindJSON(this, json);
        save();
        return true;
    }

   public static HorreumGlobalConfig get() {
		return GlobalConfiguration.all().get(HorreumGlobalConfig.class);
   }

	public KeycloakAuthentication getAuthentication() {
    	return keycloak;
   }

   public String getBaseUrl(){
    	return this.baseUrl;
	}

	public void setBaseUrl(
			String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getKeyName(){
    	return keycloak.getKeyName();
	}

	public void setKeycloakBaseUrl(
			String baseUrl) {
		keycloak.setBaseUrl(baseUrl);
	}

	public String getKeycloakBaseUrl(){
		return keycloak.getBaseUrl();
	}

	public String getKeycloakRealm(){
		return keycloak.getRealm();
	}

	public void setKeycloakRealm(String realm){
		keycloak.setRealm(realm);
	}

	public String getClientId(){
    	return keycloak.getClientId();
	}
	public void setClientId( String clientId){
    	keycloak.setClientId(clientId);
	}

	public static KeycloakAuthentication getKeycloakAuthentication(){
		HorreumGlobalConfig globalConfig = GlobalConfiguration.all().get(HorreumGlobalConfig.class);
		return globalConfig == null ? null : globalConfig.keycloak;
	}

	public List<Long> retries() {
		return this.retries;
	}

	public String getRetries() {
    	return this.retries.stream().map(String::valueOf).collect(Collectors.joining(", "));
	}

	public void setRetries(String retries) {
    	List<Long> list = new ArrayList<>();
    	for (String part : retries.split(",")) {
    		part = part.trim();
    		if (part.endsWith("s")) {
    			part = part.substring(0, part.length() - 1).trim();
			}
			try {
				long delay = Long.parseLong(part);
				if (delay <= 0) {
					throw new IllegalArgumentException("Illegal delay value: " + delay);
				}
				list.add(delay);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Cannot parse '" + retries + "' into list of delays.");
			}
		}
    	this.retries = Collections.unmodifiableList(list);
	}
}
