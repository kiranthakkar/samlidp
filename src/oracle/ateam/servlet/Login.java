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
import oracle.ateam.login.LoginImplementation;

@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Login.class.getName());
    private static SAMLConfig samlConfig = SAMLConfig.getSAMLConfiguration();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginImplementation login = null;
		try{
			log.log(Level.FINE,"Login Implementation configured is: {0}",samlConfig.getProperty("loginImplementation"));
			Class<? extends LoginImplementation> loginClass = Class.forName(samlConfig.getProperty("loginImplementation")).asSubclass(LoginImplementation.class);
			login = loginClass.getDeclaredConstructor().newInstance();
			if(login!=null) {
				String htmlResponse = login.paintLoginScreen();
				response.getWriter().append(htmlResponse);
			}
		}
		catch(IOException e) {
			log.log(Level.SEVERE,"Cannot get an instance of response writer. The error is {0}",e.getMessage());
			forwardToErrorPage(request, response);
		}
		catch(Exception e) {
			log.log(Level.SEVERE,"Login Implementation class not found. The error is {0}",e.getMessage());
			forwardToErrorPage(request, response);
		}
	}

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.fine("Got post request on Login servlet. Calling get method");
		try{
			doGet(request, response);
		}
		catch(Exception e) {
			log.log(Level.SEVERE,"Post request to Login servlet failed. The error is {0}",e.getMessage());
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
