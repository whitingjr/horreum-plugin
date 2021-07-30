package jenkins.plugins.horreum_upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
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
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.XStream2;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import jenkins.plugins.horreum_upload.auth.KeycloakAuthentication;
import jenkins.plugins.horreum_upload.util.HttpRequestNameValuePair;

@Extension
public class HorreumUploadGlobalConfig extends GlobalConfiguration {

	private String baseUrl;

    private static KeycloakAuthentication KEYCLOAK_AUTHENTICATION = new KeycloakAuthentication();

    private static final XStream2 XSTREAM2 = new XStream2();

    public HorreumUploadGlobalConfig() {
        load();
    }

    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void xStreamCompatibility() {
        XSTREAM2.addCompatibilityAlias("jenkins.plugins.horreum_upload.HttpRequest$DescriptorImpl", HorreumUploadGlobalConfig.class);
        XSTREAM2.addCompatibilityAlias("jenkins.plugins.horreum_upload.util.NameValuePair", HttpRequestNameValuePair.class);
    }

    @Override
    protected XmlFile getConfigFile() {
        File rootDir = Jenkins.getInstance().getRootDir();
        File xmlFile = new File(rootDir, "jenkins.plugins.horreum_upload.HorreumUpload.xml");
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

	public ListBoxModel doFillClientSecretIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
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
						StringCredentialsImpl.class)
				.includeCurrentValue(credentialsId);
	}


    public static HorreumUploadGlobalConfig get() {
        return GlobalConfiguration.all().get(HorreumUploadGlobalConfig.class);
    }

    public KeycloakAuthentication getOauthAuthentication() {
        return KEYCLOAK_AUTHENTICATION;
    }

	@DataBoundSetter
    public void setOauthAuthentication(
            KeycloakAuthentication oauthAuthentication) {
	        KEYCLOAK_AUTHENTICATION = oauthAuthentication;
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
    	return this.KEYCLOAK_AUTHENTICATION.getKeyName();
	}

	public void setKeycloakBaseUrl(
			String baseUrl) {
		this.KEYCLOAK_AUTHENTICATION.setKeycloakBaseUrl(baseUrl);
	}

	public String getKeycloakBaseUrl(){
		return this.KEYCLOAK_AUTHENTICATION.getKeycloakBaseUrl();
	}

	public String getKeycloakRealm(){
		return this.KEYCLOAK_AUTHENTICATION.getKeycloakRealm();
	}

	public void setKeycloakRealm(String realm){
		this.KEYCLOAK_AUTHENTICATION.setKeycloakRealm(realm);
	}

	public String getClientId(){
    	return this.KEYCLOAK_AUTHENTICATION.getClientId();
	}
	public void setClientId( String clientId){
    	this.KEYCLOAK_AUTHENTICATION.setClientId(clientId);
	}

	public String getCredentialsId(){
    	return this.KEYCLOAK_AUTHENTICATION.getHorreumCredentialsID();
	}
	public void setCredentialsId(String id){
		this.KEYCLOAK_AUTHENTICATION.setHorreumCredentialsID(id);
	}
	public String getClientSecretId(){
    	return this.KEYCLOAK_AUTHENTICATION.getHorreumClientSecretID();
	}
	public void setClientSecretId(String id){
		this.KEYCLOAK_AUTHENTICATION.setHorreumClientSecretID(id);
	}


	public static KeycloakAuthentication getKeycloakAuthentication(){
    	return KEYCLOAK_AUTHENTICATION;
	}
}
