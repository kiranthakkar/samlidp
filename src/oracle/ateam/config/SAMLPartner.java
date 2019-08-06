package oracle.ateam.config;

public class SAMLPartner {

	private String assertionConsumerURL;
	private String samlReceipient;
	private String samlAudience;
	private String relayState;
	private String nameIDFormat;
	
	public SAMLPartner(String assertionConsumerURL, String samlRecipient, String samlAudience, String relayState, String nameIDFormat) {
		this.setAssertionConsumerURL(assertionConsumerURL);
		this.setSAMLRecipient(samlRecipient);
		this.setSAMLAudience(samlAudience);
		this.setRelayState(relayState);
		this.setNameIDFormat(nameIDFormat);
	}
	
	public SAMLPartner() {
		super();
	}
	
	public String getAssertionConsumerURL() {
		return assertionConsumerURL;
	}
	public void setAssertionConsumerURL(String assertionConsumerURL) {
		this.assertionConsumerURL = assertionConsumerURL;
	}

	public String getSAMLRecipient() {
		return samlReceipient;
	}
	public void setSAMLRecipient(String samlRecipient) {
		this.samlReceipient = samlRecipient;
	}

	public String getSAMLAudience() {
		return samlAudience;
	}
	public void setSAMLAudience(String samlAudience) {
		this.samlAudience = samlAudience;
	}

	public String getRelayState() {
		return relayState;
	}
	public void setRelayState(String relayState) {
		this.relayState = relayState;
	}

	public String getNameIDFormat() {
		return nameIDFormat;
	}
	public void setNameIDFormat(String nameIDFormat) {
		this.nameIDFormat = nameIDFormat;
	}
}
