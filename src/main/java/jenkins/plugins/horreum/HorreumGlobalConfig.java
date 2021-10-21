package jenkins.plugins.horreum;

import java.io.File;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.Extension;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import hudson.util.XStream2;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import jenkins.plugins.horreum.auth.KeycloakAuthentication;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

@Extension
public class HorreumGlobalConfig extends GlobalConfiguration {

	private String baseUrl;

    private static final KeycloakAuthentication KEYCLOAK_AUTHENTICATION = new KeycloakAuthentication();

    private static final XStream2 XSTREAM2 = new XStream2();

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

	public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
		StandardListBoxModel result = new StandardListBoxModel();
		if (item == null) {
			if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
				return result.includeCurrentValue(credentialsId);
			}
		} else {
			if (!item.hasPermission(Item.EXTENDED_READ)
					&& !item.hasPermission(CredentialsProvider.USE_ITEM)) {
				return result.includeCurrentValue(credentialsId);
			}
		}
		return result
				.includeEmptyValue()
				.includeAs(ACL.SYSTEM, Jenkins.get(),
						UsernamePasswordCredentialsImpl.class)
				.includeCurrentValue(credentialsId);
	}

   public static HorreumGlobalConfig get() {
		return GlobalConfiguration.all().get(HorreumGlobalConfig.class);
   }

	public KeycloakAuthentication getAuthentication() {
    	return KEYCLOAK_AUTHENTICATION;
   }

   public String getBaseUrl(){
    	return this.baseUrl;
	}

	public void setBaseUrl(
			String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getKeyName(){
    	return KEYCLOAK_AUTHENTICATION.getKeyName();
	}

	public void setKeycloakBaseUrl(
			String baseUrl) {
		KEYCLOAK_AUTHENTICATION.setKeycloakBaseUrl(baseUrl);
	}

	public String getKeycloakBaseUrl(){
		return KEYCLOAK_AUTHENTICATION.getKeycloakBaseUrl();
	}

	public String getKeycloakRealm(){
		return KEYCLOAK_AUTHENTICATION.getKeycloakRealm();
	}

	public void setKeycloakRealm(String realm){
		KEYCLOAK_AUTHENTICATION.setKeycloakRealm(realm);
	}

	public String getClientId(){
    	return KEYCLOAK_AUTHENTICATION.getClientId();
	}
	public void setClientId( String clientId){
    	KEYCLOAK_AUTHENTICATION.setClientId(clientId);
	}

	public String getCredentialsId(){
    	return KEYCLOAK_AUTHENTICATION.getCredentialsID();
	}
	public void setCredentialsId(String id){
		KEYCLOAK_AUTHENTICATION.setCredentialsID(id);
	}

	public static KeycloakAuthentication getKeycloakAuthentication(){
    	return KEYCLOAK_AUTHENTICATION;
	}
}
