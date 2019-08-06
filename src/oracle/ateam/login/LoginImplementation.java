package oracle.ateam.login;

import javax.servlet.http.HttpServletRequest;

public interface LoginImplementation {
	public String paintLoginScreen();
	public String validateCredentials(HttpServletRequest request);	
}
