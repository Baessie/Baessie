package org.baessie.simulator.jdbc.driver;

@SuppressWarnings("serial")
public class CannotRegisterWithDriverManagerException extends RuntimeException {

	public CannotRegisterWithDriverManagerException(final Throwable cause) {
		super("Failed to register with DriverManager", cause);
	}

}
