package oracle.ateam.test;

import oracle.ateam.config.SAMLConfig;

public class TestConfig {

	public static void main(String args[]) {
		SAMLConfig samlConfig = SAMLConfig.getSAMLConfiguration();
		String loginImplementation = samlConfig.getProperty("loginImplementation");
		System.out.println("Login is done by: " + loginImplementation);
	}
}
