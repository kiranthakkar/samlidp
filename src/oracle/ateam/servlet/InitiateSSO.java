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

import org.opensaml.saml2.core.AuthnRequest;

import oracle.ateam.config.SAMLConfig;
import oracle.ateam.idp.SAMLLibrary;

@WebServlet("/InitiateSSO")
public class InitiateSSO extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(InitiateSSO.class.getName());
	private static SAMLConfig samlConfig = SAMLConfig.getSAMLConfiguration();
	private static final String RELAYSTATE = "RelayState";
    
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String authnRequest = request.getParameter("SAMLRequest");
		AuthnRequest authRequest = SAMLLibrary.getSAMLLibrary().parseSAMLRequest(authnRequest);
		String providerID = authRequest.getIssuer().getValue();
		if(!samlConfig.validatePartner(providerID)) {
			log.severe("Authentication request received from invalid providerID");
			try{
				response.getWriter().append("ProviderID is not valid");
			}
			catch(IOException e) {
				log.log(Level.SEVERE,"Cannot get an instance of response writer. The error is {0}",e.getMessage());
			}
			try{
				request.getRequestDispatcher("error.jsp").forward(request, response);
			}
			catch(Exception e) {
				log.log(Level.SEVERE,"Request forward to error page failed in InitiateSSO. The error is {0}",e.getMessage());
			}
		}
		else {
			log.fine("Its a valid authentication request with requestID: " + authRequest.getID());
			String relayState = request.getParameter(RELAYSTATE);
			if(relayState==null) {
				relayState = samlConfig.getSAMLPartner(providerID).getRelayState();
				log.warning("Authentication request does not have RelayStateRelay");
			}
			log.log(Level.FINE,"RelayState for the request is: {0}",relayState);
			
			request.getSession().setAttribute("RequestID", authRequest.getID());
			request.getSession().setAttribute(RELAYSTATE, relayState);
			request.getSession().setAttribute("ProviderID", providerID);
			try{
				request.getRequestDispatcher("/Login").forward(request, response);
			}
			catch(Exception e) {
				log.log(Level.SEVERE,"Cannot forward the request to the Login Servlet. The error is {0}",e.getMessage());
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			doGet(request,response);
		}
		catch(Exception e) {
			log.log(Level.SEVERE,"Post request to InitiateSSO servlet failed. The error is {0}",e.getMessage());
		}
	}
}
