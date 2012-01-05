package org.baessie.simulator.jdbc.driver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.baessie.simulator.jdbc.expectation.Logger;

public class SimulatorDriver implements Driver {

	static {
		Logger.log("<Registering with DriverManager>");
		try {
			DriverManager.registerDriver(new SimulatorDriver());
		} catch (final SQLException e) {
			throw new CannotRegisterWithDriverManagerException(e);
		}
	}

	@Override
	public Connection connect(final String url, final Properties info) throws SQLException {
		Logger.log("SimulatorDriver.connect(%s, %s)", url, info);
		if (acceptsURL(url)) {
			return new SimulatorConnection();
		}
		return null;
	}

	// --- Unimplemented methods below

	@Override
	public boolean acceptsURL(final String url) throws SQLException {
		Logger.log("SimulatorDriver.acceptsURL(%s)", url);
		return !url.contains("jdbc:mock:");
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
		Logger.log("SimulatorDriver.getPropertyInfo(%s, %s)", url, info);
		return null;
	}

	@Override
	public int getMajorVersion() {
		Logger.log("SimulatorDriver.getMajorVersion()");
		return 1;
	}

	@Override
	public int getMinorVersion() {
		Logger.log("SimulatorDriver.getMinorVersion()");
		return 0;
	}

	@Override
	public boolean jdbcCompliant() {
		Logger.log("SimulatorDriver.jdbcCompliant()");
		return false;
	}

}
