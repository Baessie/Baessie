package org.baessie.simulator.jdbc.expectation;

@SuppressWarnings("serial")
public class ExpectationCommunicationException extends RuntimeException {

	public ExpectationCommunicationException(final Throwable e) {
		super("Failed to communicate (read or write) expectations.", e);
	}

}
