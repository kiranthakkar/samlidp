package oracle.ateam.login;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class SimpleLogin implements LoginImplementation{
	
	private static Logger log = Logger.getLogger(SimpleLogin.class.getName());

	public String paintLoginScreen() {
		StringBuilder htmlResponse = new StringBuilder();
		htmlResponse.append("<html><head><meta http-equiv=\"Content-Type\"");
		htmlResponse.append("content=\"text/html; charset=UTF-8\">");
		htmlResponse.append("<title>SAML Identity Provider</title></head><body>");
		htmlResponse.append("<img src=\"images/ateam.png\" style=\"width: 424px; height: 74px; \"/><br>");
		htmlResponse.append("<h2>SAML Identity Provider Login page.<br> Enter username and click Submit.</h2><br>");
		htmlResponse.append("<form name=\"loginForm\" method=\"post\" action=\"ValidateUser\">");
		htmlResponse.append("Username: <input type=\"text\" name=\"username\"/> <br/>");
		htmlResponse.append("<input type=\"submit\" value=\"Login\" />");
		htmlResponse.append("</form></body></html>");
		return htmlResponse.toString();
	}
	public String validateCredentials(HttpServletRequest request) {
		String username = request.getParameter("username");
		if(username==null) {
			log.severe("Username is null. Cannot authenticate the user");
		}
		return username;
	}
}
