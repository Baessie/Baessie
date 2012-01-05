package org.baessie.simulator.jdbc.driver;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class UnexpectedQueryException extends SQLException {

	public UnexpectedQueryException(final String query) {
		super(query);
	}

}
