/*
 * No copyrights. However it is not thoroughly tested. Use it at your own risk!
 * 
 * Developer: Kiran Thakkar
 * Version: 1.0
 * 
 */

package oracle.ateam.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SAMLConfig {
	private static SAMLConfig samlConfig = null;
	private Properties samlProperties;
	private HashMap<String, SAMLPartner> samlPartners;
	private static Logger log = Logger.getLogger(SAMLConfig.class.getName());
	public static SAMLConfig getSAMLConfiguration(){
		if(samlConfig==null){
			samlConfig = new SAMLConfig();
		}
		return samlConfig;
	}
		
	public SAMLPartner getSAMLPartner(String providerID) {
		if(samlPartners!=null) {
			return samlPartners.get(providerID);
		}
		return null;
	}
		
	private SAMLConfig(){
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try{
			InputStream loggingStream = classLoader.getResourceAsStream("logging.properties");
			LogManager.getLogManager().readConfiguration(loggingStream);
			loggingStream.close();
		}
		catch(IOException ioe) {
			log.log(Level.SEVERE,"Logging config file could not be read {0}",ioe.getMessage());
		}
			
		samlProperties = new Properties();
		try{
			InputStream configStream = classLoader.getResourceAsStream("config.properties");
			samlProperties.load(configStream);
			samlPartners = new HashMap<>();
			loadPartnersConfig(samlProperties.getProperty("PartnerConfigFile"));
		}
		catch(IOException ioe){
			log.log(Level.SEVERE,"SAML config file could not be read {0}",ioe.getMessage());
		}
		log.info("SAMLConfig instance is initialized successfully");
	}
		
	private void loadPartnersConfig(String partnersFileName) {
		try{
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream partnersStream = classLoader.getResourceAsStream(partnersFileName);
				
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
			dbFactory.setNamespaceAware (true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(partnersStream);
			NodeList pList = doc.getElementsByTagName("partner");
				
			for(int i=0;i<pList.getLength();i++) {
				Node pNode = pList.item(i);
				if(pNode.getNodeType() == Node.ELEMENT_NODE) {
					Element pElement = (Element)pNode;
					String providerID = pElement.getAttribute("providerID");
					String assertionConsumerURL = pElement.getElementsByTagName("AssertionConsumerURL").item(0).getTextContent();
					String samlRecipient = pElement.getElementsByTagName("SAMLRecipient").item(0).getTextContent();
					String samlAudience = pElement.getElementsByTagName("SAMLAudience").item(0).getTextContent();
					String relayState = pElement.getElementsByTagName("RelayState").item(0).getTextContent();
					String nameIDFormat = pElement.getElementsByTagName("NameIDFormat").item(0).getTextContent();

					log.log(Level.INFO,"Found Provider with ProviderID is: {0}",providerID);
					log.log(Level.FINE,"AssertionConsumerURL for the provider is {0}",assertionConsumerURL);
					log.log(Level.FINE,"SAMLRecipient for the provider is {0}",samlRecipient);
					log.log(Level.FINE,"SAMLAudience for the provider is {0}",samlAudience);
					log.log(Level.FINE,"RelayState for the provider is {0}",relayState);
					log.log(Level.FINE,"NameIDFormat for the provider is {0}",nameIDFormat);

					SAMLPartner partner = new SAMLPartner(assertionConsumerURL,samlRecipient,samlAudience,relayState,nameIDFormat);
					samlPartners.put(pElement.getAttribute("providerID"), partner);
				}
			}
		}
		catch(Exception e) {
			log.severe("Unable to read or parse partners configuration file.");
		}
	}
		
	public boolean validatePartner(String providerID) {
		if(samlPartners!=null) {
			return samlPartners.containsKey(providerID);
		}
		return false;
	}
		
	public String getProperty(String property){
		if(samlPartners!=null) {
			return samlProperties.getProperty(property);
		}
		return null;
	}
}
