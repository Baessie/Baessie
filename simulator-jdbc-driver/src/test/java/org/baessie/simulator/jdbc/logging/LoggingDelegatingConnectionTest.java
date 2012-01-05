package org.baessie.simulator.jdbc.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Before;
import org.junit.Test;

public class LoggingDelegatingConnectionTest {
	private Connection wrappedConnection;
	private LoggingDelegatingConnection connection;

	@Before
	public void setUp() throws Exception {
		DriverManager.registerDriver(new DriverMock());
		wrappedConnection = DriverManager.getConnection("jdbc:mock:thin:@host:1521:dbname", "user", "passwd");
		connection = new LoggingDelegatingConnection(wrappedConnection, null);
	}

	@Test
	public void isWrapperForReturnTrueForMockConnection() throws Exception {
		assertTrue("is wrapper", connection.isWrapperFor(ConnectionMock.class));
	}

	@Test
	public void isWrapperForReturnFalseForOtherConnection() throws Exception {
		assertFalse("is wrapper", connection.isWrapperFor(LoggingDelegatingConnection.class));
	}

	@Test
	public void unWrapReturnTheWrappedConnection() throws Exception {
		assertSame("wrapped connection", wrappedConnection, connection.unwrap(ConnectionMock.class));
	}

}
