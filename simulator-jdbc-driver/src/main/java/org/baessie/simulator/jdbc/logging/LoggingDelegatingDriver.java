package org.baessie.simulator.jdbc.logging;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingDelegatingDriver implements Driver {

	static {
		try {
			DriverManager.registerDriver(new LoggingDelegatingDriver());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		if (acceptsURL(url)) {
			String wrappedDriverClass = (String) info.get("wrappedDriverClass");
			registerWrappedDriver(wrappedDriverClass);

			Logger logger = LoggerFactory.getLogger(this.getClass());
			for (Object key : info.keySet()) {
				logger.trace(key + " : " + info.getProperty((String) key));
			}
			Connection wrappedConnection = DriverManager.getConnection(url.replaceFirst("log:", ""), info);
			return new LoggingDelegatingConnection(wrappedConnection, logger);
		}
		return null;
	}

	private void registerWrappedDriver(String wrappedDriver) throws SQLException {
		try {
			Class<?> wrappedDriverClass = Class.forName(wrappedDriver);
			DriverManager.registerDriver((Driver) wrappedDriverClass.newInstance());
		} catch (ClassNotFoundException e) {
			throw new SQLException("Unable to load driver class", e);
		} catch (InstantiationException e) {
			throw new SQLException("Unable to load driver class", e);
		} catch (IllegalAccessException e) {
			throw new SQLException("Unable to load driver class", e);
		}
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("log:");
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		String trimmedUrl = url.replaceFirst("log:", "");
		DriverPropertyInfo[] propertyInfo = DriverManager.getDriver(trimmedUrl).getPropertyInfo(trimmedUrl, info);
		return propertyInfo;
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public boolean jdbcCompliant() {
		return true;
	}

}
