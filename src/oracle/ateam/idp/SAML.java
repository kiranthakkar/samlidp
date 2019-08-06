/*
 * No copyrights. However it is not thoroughly tested. Use it at your own risk!
 * 
 * Developer: Kiran Thakkar
 * Version: 1.0
 * 
 */
package oracle.ateam.idp;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.schema.XSAny;
import org.w3c.dom.Document;

public class SAML
{
    private String issuerURL;
    private static Logger log = Logger.getLogger(SAML.class.getName());
    
    public SAML(){
    	new SAML(null);
    }
    
    public SAML (String issuerURL){
        log.info("SAML Constructor is called");
        this.issuerURL = issuerURL;
    }
       
    @SuppressWarnings ("unchecked")
    public <T> T create ( QName qname){
        return (T) ((XMLObjectBuilder<XSAny>)Configuration.getBuilderFactory ()
        		.getBuilder (qname)).buildObject (qname);
    }
    
    public Document asDOMDocument (XMLObject object){
    	Document document = null;
    	try{
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
    		factory.setExpandEntityReferences(false);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
			factory.setNamespaceAware (true);
    		DocumentBuilder dBuilder = factory.newDocumentBuilder();
    		document = dBuilder.newDocument ();
    		Marshaller out = 
    				Configuration.getMarshallerFactory ().getMarshaller (object);
    		out.marshall (object, document);
    	}
    	catch(Exception e) {
    		log.log(Level.SEVERE,"Exception in converting XML to DOM object in asDOMDocument method. The error is {0}",e.getMessage());
    	}
        return document;
    }

    public Issuer spawnIssuer (){
        Issuer result = null;
        if (issuerURL != null) {
            result = create (Issuer.DEFAULT_ELEMENT_NAME);
            result.setValue (issuerURL);
        }
        return result;
    }
    
    public NameID createNameID(String username,String nameIDFormat) {
        NameID nameID = create (NameID.DEFAULT_ELEMENT_NAME);
        nameID.setFormat(nameIDFormat);
        nameID.setValue (username);
        return nameID;
    }
    
    public Subject createSubject(NameID nameID,String samlRecipient,String samlRequestID) {
        Subject subject = create (Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID (nameID);
        System.setProperty("org.apache.xml.security.ignoreLineBreaks","true");
		System.setProperty("org.apache.xml.security.util.XMLUtils.ignoreAddReturnToElement","true");
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        
        SubjectConfirmationBuilder subjectConfirmationBuilder = (SubjectConfirmationBuilder) builderFactory
        	    .getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        SubjectConfirmation subjectConfirmation = subjectConfirmationBuilder.buildObject();
        subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = (SubjectConfirmationDataBuilder) builderFactory
        	    .getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.buildObject();
        String assertionRecipient = samlRecipient;
        subjectConfirmationData.setRecipient(assertionRecipient);
        DateTime now = new DateTime();
        subjectConfirmationData.setNotOnOrAfter(now.plusSeconds(3600));
        if(samlRequestID!=null){
        	subjectConfirmationData.setInResponseTo(samlRequestID);
        }
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        return subject;
    }
    
    public Conditions createAssertionConditions(String samlAudience) {
        Conditions conditions = create 
                (Conditions.DEFAULT_ELEMENT_NAME);
        DateTime now = new DateTime();
        conditions.setNotBefore (now.minusSeconds (15));
        conditions.setNotOnOrAfter (now.plusSeconds (3600));
        System.setProperty("org.apache.xml.security.ignoreLineBreaks","true");
		System.setProperty("org.apache.xml.security.util.XMLUtils.ignoreAddReturnToElement","true");
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        AudienceRestrictionBuilder audienceRestrictionBuilder = (AudienceRestrictionBuilder) builderFactory
        		.getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        AudienceRestriction audienceRestriction  = audienceRestrictionBuilder.buildObject();
        AudienceBuilder audienceBuilder  = (AudienceBuilder) builderFactory
        		.getBuilder(Audience.DEFAULT_ELEMENT_NAME);
        Audience e = audienceBuilder.buildObject();
        e.setAudienceURI(samlAudience);
        audienceRestriction.getAudiences().add(e);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }
    
    public AuthnStatement createAuthnStatement() {
    	AuthnStatement authnStatement = create 
                (AuthnStatement.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef ref = create (AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        ref.setAuthnContextClassRef (AuthnContext.PPT_AUTHN_CTX);
        AuthnContext authnContext = create 
            (AuthnContext.DEFAULT_ELEMENT_NAME);
        authnContext.setAuthnContextClassRef (ref);
        DateTime now = new DateTime();
        authnStatement.setAuthnInstant(now);
        authnStatement.setSessionNotOnOrAfter(now.plusSeconds(3600));
        authnStatement.setAuthnContext (authnContext);
        return authnStatement;
    }
    
    public void addAttribute(AttributeStatement statement, String name, String value){
    	final XMLObjectBuilder<XSAny> objectBuilder = 
    			(XMLObjectBuilder<XSAny>)Configuration.getBuilderFactory ().getBuilder(XSAny.TYPE_NAME);
    	XSAny valueElement = objectBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        valueElement.setTextContent (value);
        Attribute attribute = create 
            (Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName (name);
        attribute.getAttributeValues ().add (valueElement);
        statement.getAttributes ().add (attribute);
    }
}
