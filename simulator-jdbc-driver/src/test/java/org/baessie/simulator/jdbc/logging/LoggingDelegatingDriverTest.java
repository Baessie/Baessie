package org.baessie.simulator.jdbc.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.baessie.simulator.jdbc.driver.SimulatorDriver;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LoggingDelegatingDriverTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		DriverManager.deregisterDriver(new SimulatorDriver());
	}

	@Before
	public void setUp() throws Exception {
		Class.forName("org.baessie.simulator.jdbc.logging.LoggingDelegatingDriver");
		Class.forName("org.baessie.simulator.jdbc.logging.DriverMock");
	}

	@Test
	public void connectionIsCreated() throws Exception {
		Properties properties = new Properties();
		properties.put("username", "user");
		properties.put("password", "passwd");
		properties.put("wrappedDriverClass", "org.baessie.simulator.jdbc.logging.DriverMock");

		Connection connection = DriverManager.getConnection("log:jdbc:mock:@host:dbname", properties);
		assertNotNull("connection", connection);

		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(String.class);
		classes.add(String.class);
		PreparedStatementMock.addResultSet("SELECT 'A', 'B' FROM DUAL", new ResultSetMock(Arrays.asList("A", "B"), classes, new ResultSetRowMock("A", "B")));

		PreparedStatement preparedStatement = connection.prepareStatement("SELECT 'A', 'B' FROM DUAL");
		assertTrue("execute", preparedStatement.execute());
		ResultSet resultSet = preparedStatement.getResultSet();
		assertTrue("next", resultSet.next());
		assertEquals("value", "A", resultSet.getString(1));
	}

}
