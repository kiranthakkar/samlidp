/*
 * No copyrights. However it is not thoroughly tested. Use it at your own risk!
 * 
 * Developer: Kiran Thakkar
 * Version: 1.0
 * 
 */
package oracle.ateam.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.ateam.config.SAMLConfig;
import oracle.ateam.idp.SAMLLibrary;
import oracle.ateam.login.LoginImplementation;

@WebServlet("/ValidateUser")
public class ValidateUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(ValidateUser.class.getName());
	private static SAMLConfig samlConfig = SAMLConfig.getSAMLConfiguration();
	private static final String RELAYSTATE = "RelayState";
       
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			response.getWriter().append("<html><body>You cannot call get method on Validate User Servlet</body></html>");
		}
		catch(IOException e) {
			log.log(Level.SEVERE,"Cannot get an instance of response writer. The error is {0}",e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.fine("Received post request on validateUser servlet");
		String relayState = (String) request.getSession().getAttribute(RELAYSTATE);
		String requestID = (String)request.getSession().getAttribute("RequestID");
		String providerID = (String)request.getSession().getAttribute("ProviderID");
		LoginImplementation login = null;
		try{
			Class<? extends LoginImplementation> loginClass = Class.forName(samlConfig.getProperty("loginImplementation")).asSubclass(LoginImplementation.class);
			login = loginClass.getDeclaredConstructor().newInstance();
			String username = login.validateCredentials(request);
			StringBuilder responseMessage = new StringBuilder();
			responseMessage.append("<html><head><title>SAML Identity Provider</title></head>");
			if(username!=null) {
				responseMessage.append("<body onload=\"document.getElementById('samlForm').submit();\">");
				log.fine("Calling getSAMLAssertion now");
				String samlAssertion = SAMLLibrary.getSAMLLibrary().getSAMLAssertion(username, requestID, providerID);
				log.log(Level.FINE,"The assertion is encoded. The Value is: {0}",samlAssertion);
				responseMessage.append("<form name=\"samlForm\" id=\"samlForm\" action=\"" + samlConfig.getSAMLPartner(providerID).getAssertionConsumerURL() + "\" method=\"POST\">");
				responseMessage.append("<input name=\"SAMLResponse\" type=\"hidden\" value=\"" + samlAssertion + "\">");
				responseMessage.append("<input type=\"hidden\" name=\"RelayState\" value=\"" + relayState + "\">");
				responseMessage.append("</form>");
			}
			else {
				log.info("Invalid user credentials");
				responseMessage.append("<body>Invalid user credentials");
			}
			responseMessage.append("</body></html>");
			response.getWriter().append(responseMessage.toString());
		}
		catch(IOException e) {
			log.log(Level.SEVERE,"Cannot get an instance of response writer. The error is {0}",e.getMessage());
			forwardToErrorPage(request, response);
		}
		catch(Exception e) {
			log.log(Level.SEVERE,"Login Implementation not found. The error is {0}",e.getMessage());
			forwardToErrorPage(request, response);
		}
	}
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response) {
		try{
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
		catch(Exception e) {
			log.log(Level.SEVERE,"Cannot forward the request to error page. The error is {0}",e.getMessage());
		}
	}
}
