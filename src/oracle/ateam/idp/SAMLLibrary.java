/*
 * No copyrights. However it is not thoroughly tested. Use it at your own risk!
 * 
 * Developer: Kiran Thakkar
 * Version: 1.0
 * 
 */
package oracle.ateam.idp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.impl.RandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import oracle.ateam.config.SAMLConfig;

public class SAMLLibrary extends SAML{

	private static SAMLLibrary samlLibrary = null;
	private static SAMLConfig samlConfig = SAMLConfig.getSAMLConfiguration();
	
	private static String KEYSTOREPASSWORD = samlConfig.getProperty("keystorepassword");
	private static String CERTALIASNAME = samlConfig.getProperty("certificatealias");
	private static String KEYSTOREFILENAME = samlConfig.getProperty("keystorefile");
	
	private static Logger log = Logger.getLogger(SAMLLibrary.class.getName());
	
	private KeyStore keystore;
	private PrivateKeyEntry pkEntry;
	private PrivateKey pk;
    private X509Certificate cert;
    private BasicX509Credential credential;
    private Credential signingCredential; 
    private Signature signature;
    javax.xml.crypto.dsig.XMLSignature xmlSignature;
 
	public static SAMLLibrary getSAMLLibrary(){
		if(samlLibrary==null){
			samlLibrary = new SAMLLibrary();
		}
		return samlLibrary;
	}
	
	private SAMLLibrary(){
		char[] keystorePassword = SAMLLibrary.KEYSTOREPASSWORD.toCharArray();
		try{
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream stream = classLoader.getResourceAsStream(SAMLLibrary.KEYSTOREFILENAME);
			keystore.load(stream,keystorePassword);
			stream.close();
			
	    	pkEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry(SAMLLibrary.CERTALIASNAME,new KeyStore.PasswordProtection(keystorePassword));
			if(pkEntry!=null) {
				pk = pkEntry.getPrivateKey();
				cert = (X509Certificate)pkEntry.getCertificate();
			    credential = new BasicX509Credential();
			    credential.setEntityCertificate(cert);
			    credential.setPrivateKey(pk);
			    signingCredential = credential;
			    initializeSigning();
			    log.info("SAMLLibrary instance is created successfully");
			}
		}
		catch(Exception e){
			log.log(Level.SEVERE,"Cannot create SAMLLibrary instance. The error message is {0}",e.getMessage());
		}
	}
	
	public void initializeSigning(){
	    signature = null;
		SignatureBuilder signatureBuilder  = (SignatureBuilder)Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME);
	    signature = signatureBuilder.buildObject();
	    signature.setSigningCredential(signingCredential);
	    
	    signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
	    if(samlConfig.getProperty("signingalgo").equals("sha1")) {
	    	signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
	    }
	    else {
	    	signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
	    }

	    Credential peerCredential = SecurityHelper.getSimpleCredential(cert, null);
	    X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
	    x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
	    KeyInfoGeneratorManager keyInfoGeneratorManager = new KeyInfoGeneratorManager();
	    keyInfoGeneratorManager.registerFactory(x509KeyInfoGeneratorFactory);
	    KeyInfoGeneratorFactory keyInfoGeneratorFactory = keyInfoGeneratorManager.getFactory(peerCredential);
	    KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
	    try{
	    	KeyInfo serviceKeyInfo = keyInfoGenerator.generate(credential);
	    	signature.setKeyInfo(serviceKeyInfo);
	    }
	    catch (org.opensaml.xml.security.SecurityException e) {
			log.severe("Security Exception while initializing signing");
	    }
	}
	
	public String encodeAssertion(String assertion){
		String encodedAssertion = null;
		byte[] assertionBytes = assertion.getBytes(StandardCharsets.UTF_8);
		encodedAssertion = Base64.encodeBytes(assertionBytes,Base64.DONT_BREAK_LINES);
		return encodedAssertion;
	}
	
	public String getSAMLAssertion(String username,String samlRequestID,String providerID) {
		String assertion = generateSAMLAssertion(username, samlRequestID, providerID);
		String encodedAssertion = encodeAssertion(assertion);
		return encodedAssertion;
	}
	
	public AuthnRequest parseSAMLRequest(String encodedSAMLRequest){
		encodedSAMLRequest = encodedSAMLRequest.replaceAll("\\r\\n","");
		log.log(Level.FINE,"SAML Request is: {0}",encodedSAMLRequest);
		byte[] samlRequest = Base64.decode(encodedSAMLRequest);
		String decodedRequest = new String(samlRequest);
		InputStream samlStream = null;
		if(decodedRequest.contains("AuthnRequest")) {
			log.log(Level.FINE,"SAMLRequest is not deflated. Decoded SAMLRequest is {0}",decodedRequest);
			samlStream = new ByteArrayInputStream(samlRequest);
		}
		else {
			log.fine("SAMLRequest is inflated");
			ByteArrayInputStream bytesIn = new ByteArrayInputStream(samlRequest);
			samlStream = new InflaterInputStream(bytesIn, new Inflater(true));
		}
		
		AuthnRequest authnRequest = null;
		try{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setExpandEntityReferences(false);
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
			documentBuilderFactory.setNamespaceAware (true);
		    DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		    Document document = docBuilder.parse(samlStream);   
		    Element element = document.getDocumentElement();
		    UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		    Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
		    XMLObject requestXmlObj = unmarshaller.unmarshall(element);
		    authnRequest = (AuthnRequest) requestXmlObj;
		    log.fine("Authentication request ID is: " + authnRequest.getID());
		}
		catch(Exception e){
			log.info("Authentication request cannot be read or parsed. " + e.getMessage());
		}
		return authnRequest;
	}
	
	public String getURLEncodedAssertion(String assertion){
		String encodedAssertion = encodeAssertion(assertion);
		String urlEncodedAssertion = null;
		try{
		    urlEncodedAssertion = URLEncoder.encode(encodedAssertion,"UTF-8");
		}
		catch(UnsupportedEncodingException e){
			log.log(Level.SEVERE,"Assertion encoding failed. The error is {0}",e.getMessage());
		}
		return urlEncodedAssertion;		
	}
	
	public String generateSAMLAssertion(String username,String samlRequestID,String providerID){
		log.fine("Started building SAML assertion");
        DateTime now = new DateTime();
        Issuer issuer = create (Issuer.DEFAULT_ELEMENT_NAME);
        String samlIssuer = samlConfig.getProperty("SAMLIssuer");
        issuer.setValue (samlIssuer);
        issuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        
        NameID nameID = createNameID(username,samlConfig.getSAMLPartner(providerID).getNameIDFormat());
        Subject subject = createSubject(nameID,samlConfig.getSAMLPartner(providerID).getSAMLRecipient(),samlRequestID);
        Conditions conditions = createAssertionConditions(samlConfig.getSAMLPartner(providerID).getSAMLAudience());
        AuthnStatement authnStatement = createAuthnStatement();
        
        AttributeStatement attributeStatement = create (AttributeStatement.DEFAULT_ELEMENT_NAME);
        addAttribute(attributeStatement, "https://auth.oraclecloud.com/saml/claims/groupName", "DummyIDP");
        
        Assertion assertion = create (Assertion.DEFAULT_ELEMENT_NAME);
        RandomIdentifierGenerator idGenerator = new RandomIdentifierGenerator();
        String assertionId = "id-" + Base64.encodeBytes(idGenerator.generateIdentifier().getBytes());
        assertion.setID (assertionId);
        assertion.setIssueInstant (now);
        assertion.setSchemaLocation("http://www.w3.org/2001/XMLSchema-instance");
        assertion.setIssuer (issuer);
        assertion.setSubject (subject);
        assertion.getStatements ().add (authnStatement);
        assertion.setSignature(signature);
        assertion.setConditions(conditions);
        assertion.getStatements().add(attributeStatement);
        
        StatusCode samlStatusCode = create(StatusCode.DEFAULT_ELEMENT_NAME);
        samlStatusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
        Status samlStatus = create(Status.DEFAULT_ELEMENT_NAME);
        samlStatus.setStatusCode(samlStatusCode);
        Issuer responseIssuer = create(Issuer.DEFAULT_ELEMENT_NAME);
        responseIssuer.setValue(samlIssuer);
        responseIssuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        
        Response samlResponse = create(Response.DEFAULT_ELEMENT_NAME);
        samlResponse.setIssueInstant(now);
        samlResponse.setStatus(samlStatus);
        samlResponse.setIssuer(responseIssuer);
        samlResponse.getAssertions().add(assertion);
        if(samlRequestID!=null){
        	samlResponse.setInResponseTo(samlRequestID);
        }
        
        AssertionMarshaller marshaller = new AssertionMarshaller();
        try{
        	marshaller.marshall(assertion);
        	Signer.signObject(signature);
        }
        catch(MarshallingException me) {
        	log.log(Level.SEVERE,"Failed during Marshalling assertion. The error is {0}",me.getMessage());
        	return null;
        }
        catch(SignatureException se){
        	log.log(Level.SEVERE,"Failed during signing assertion. The error is {0}",se.getMessage());
        	return null;
        }
        
        Document samlDocument = null;
        samlDocument = asDOMDocument(samlResponse);
        if(samlDocument==null) {
        	log.severe("Cannot convert XML SAML assertion to DOM object");
        	return null;
        }
        String returningAssertion = null;
        returningAssertion = convertToString(samlDocument);
        log.log(Level.FINE,"SAML Response is: {0}",returningAssertion);
        return returningAssertion;
	}
	
	private String convertToString(Document samlDocument){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream ();
        String assertion = null;
        try{
        	TransformerFactory factory = TransformerFactory.newInstance();
        	factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        	Transformer transformer = factory.newTransformer();
            transformer.transform (new DOMSource(samlDocument), new StreamResult(buffer));
            byte[] rawResult = buffer.toByteArray ();
            buffer.close ();
            assertion = new String(rawResult);
        }
        catch (Exception e){
            log.log(Level.SEVERE,"Cannot convert SAML assertion Document to String. The error is {0} ",e.getMessage());
        }
        return assertion;
	}
}
