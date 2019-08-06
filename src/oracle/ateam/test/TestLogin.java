package oracle.ateam.test;

import oracle.ateam.config.SAMLConfig;
import oracle.ateam.login.LoginImplementation;

public class TestLogin {

	public static void main(String[] args) {
		//LoginImplementation login = (LoginImplementation) new SimpleLogin();
		LoginImplementation login = null;
		try{
			String loginClassName = SAMLConfig.getSAMLConfiguration().getProperty("loginImplementation");
			Class<? extends LoginImplementation> loginClass = Class.forName(loginClassName).asSubclass(LoginImplementation.class);
			login = loginClass.getDeclaredConstructor().newInstance();
		}
		catch(Exception e) {
			System.out.println("Did not work " + e.getMessage());
		}
		if(login!=null) {
			String screen = login.paintLoginScreen();
			System.out.println("Login screen is: " + screen);
		}
		
	}
	public void testValidateUser() {
		/*response.getWriter().append("<html><body onload=\"document.getElementById('samlForm').submit();\">Served at: ").append(request.getContextPath());
		String username = "kiran.thakkar@oracle.com";
		//String relayState = (String) request.getSession().getAttribute("relayState");
		//String relayState = "https://idcs-c53f260e3d2c4d2a8b3f87caf0529066.identity.oraclecloud.com/ui/v1/myconsole";
		String relayState = "id-bIqLrO2wIJD6k3NWBLuAbiiR1bgwyanjUuO9QI-S";
		String requestID = "id-Gmq5-jqQBCtRxags6SSrfWWm7PRuegBtPBbYnJFC";
		String providerID = SAMLConfig.getSAMLConfiguration().getProperty("ProviderID");
		//String requestID = (String)request.getSession().getAttribute("requestID");
		String samlAssertion = SAMLLibrary.getSAMLLibrary().getSAMLAssertion(username, requestID,providerID);
		String encodedAssertion = SAMLLibrary.getSAMLLibrary().getEncodedAssertion(samlAssertion);
		//response.getWriter().append("<br>SAML Assertion is: " + encodedAssertion + "</body></html>");
		
		response.getWriter().append("<form name=\"samlForm\" id=\"samlForm\" action=\"" + SAMLConfig.getSAMLConfiguration().getProperty("AssertionConsumerURL") + "\" method=\"POST\">");
		response.getWriter().append("<input name=\"SAMLResponse\" type=\"hidden\" value=\"" + encodedAssertion + "\">");
		response.getWriter().append("<input type=\"hidden\" name=\"RelayState\" value=\"" + relayState + "\">");
		response.getWriter().append("</form></body></html>");*/
	}
}
