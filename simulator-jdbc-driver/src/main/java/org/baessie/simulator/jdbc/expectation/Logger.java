package org.baessie.simulator.jdbc.expectation;

public class Logger {

	/**
	 * Logs a given formatted message inserting the given objects into the
	 * format string.
	 * 
	 * @param message
	 *            a string adhering to the
	 *            {@link String#format(String, Object...)} style.
	 * @param objects
	 *            objects referred to from the message.
	 */
	public static void log(final String message, final Object... objects) {
		final String formattedMessage = String.format(message, objects);
		System.out.println("SIMULATOR: " + formattedMessage);
	}

}
