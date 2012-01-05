package org.baessie.simulator.jdbc.driver;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.baessie.simulator.jdbc.expectation.ExpectationManager;
import org.junit.Test;

public class IntegrationTest {

	@Test
	public void testItAll() throws Exception {
		ExpectationManager.resetExpectations();

		final String triggerQuery = "TRIGGER";
		final String expectationQuery = "SETUP:\n" + triggerQuery + "\n'name','age'\n'java.lang.String:Adam','java.lang.String:32'\n'java.lang.String:Karlsson, Bertil','java.lang.String:70'";

		Class.forName("org.baessie.simulator.jdbc.driver.SimulatorDriver");
		final PreparedStatement setupStatement = createStatement(expectationQuery);
		setupStatement.execute();
		final ResultSet setupResultSet = setupStatement.getResultSet();

		assertTrue(setupResultSet.next());
		assertEquals("OK", setupResultSet.getString("Status"));
		assertFalse(setupResultSet.next());

		final PreparedStatement triggerStatement = createStatement(triggerQuery);
		triggerStatement.execute();
		final ResultSet resultSet = triggerStatement.getResultSet();
		assertEquals(2, triggerStatement.getFetchSize());
		assertEquals(2, resultSet.getFetchSize());

		final ResultSetMetaData metaData = resultSet.getMetaData();
		assertEquals(null, metaData.getTableName(1));
		assertEquals(2, metaData.getColumnCount());
		assertEquals("name", metaData.getColumnLabel(1));
		assertEquals("age", metaData.getColumnLabel(2));

		assertTrue(resultSet.next());
		assertEquals("Adam", resultSet.getString("name"));
		assertEquals("32", resultSet.getString("age"));
		assertTrue(resultSet.next());
		assertEquals("Karlsson, Bertil", resultSet.getString(1));
		assertEquals("70", resultSet.getString(2));
		assertFalse(resultSet.next());

		try {
			createStatement("OTHER QUERY");
		} catch (final UnexpectedQueryException expected) {
			// Expected
		}

		createStatement("RESET");

		try {
			createStatement(triggerQuery);
		} catch (final UnexpectedQueryException expected) {
			// Expected
		}
	}

	@Test
	public void positionalParametersAreHandledCorrectly() throws Exception {
		ExpectationManager.resetExpectations();

		final String triggerQuery = "SELECT name, age WHERE d = ?";
		final String expectationQuery = "SETUP:\n" + "SELECT name, age WHERE d = 'yadda'" + "\n'name','age'\n'java.lang.String:Adam','java.lang.String:32'";

		Class.forName("org.baessie.simulator.jdbc.driver.SimulatorDriver");
		final PreparedStatement setupStatement = createStatement(expectationQuery);
		setupStatement.execute();

		final PreparedStatement triggerStatement = createStatement(triggerQuery);
		triggerStatement.setString(1, "yadda");
		triggerStatement.execute();
		final ResultSet resultSet = triggerStatement.getResultSet();
		assertEquals(1, triggerStatement.getFetchSize());
		assertEquals(1, resultSet.getFetchSize());
	}

	@Test
	public void wildcardPositionalParametersAreHandledCorrectly() throws Exception {
		ExpectationManager.resetExpectations();

		final String triggerQuery = "SELECT name, age WHERE d = ?";
		final String expectationQuery = "SETUP:\n" + "SELECT name, age WHERE d = '*'" + "\n'name','age'\n'java.lang.String:Adam','java.lang.String:32'";

		Class.forName("org.baessie.simulator.jdbc.driver.SimulatorDriver");
		final PreparedStatement setupStatement = createStatement(expectationQuery);
		setupStatement.execute();

		final PreparedStatement triggerStatement = createStatement(triggerQuery);
		triggerStatement.setString(1, "bingo");
		triggerStatement.execute();
		final ResultSet resultSet = triggerStatement.getResultSet();
		assertEquals(1, triggerStatement.getFetchSize());
		assertEquals(1, resultSet.getFetchSize());
	}

	@Test
	public void callCountIsHandledCorrectly() throws Exception {
		ExpectationManager.resetExpectations();

		final String triggerQuery = "SELECT name, age WHERE d = b";
		final String expectationQuery = "SETUP:\n" + "SELECT name, age WHERE d = b" + "\n'name','age'";

		Class.forName("org.baessie.simulator.jdbc.driver.SimulatorDriver");
		final PreparedStatement setupStatement = createStatement(expectationQuery);
		setupStatement.execute();

		final PreparedStatement triggerStatement = createStatement(triggerQuery);
		triggerStatement.execute();

		PreparedStatement verifyStatement = createStatement("VERIFY:\n" + triggerQuery);
		verifyStatement.execute();
		ResultSet resultSet = verifyStatement.getResultSet();
		assertTrue(resultSet.next());
		assertEquals(1, resultSet.getInt(1));
	}

	@Test
	public void emptyResultSetupDoesNotThrowException() throws Exception {
		ExpectationManager.resetExpectations();

		final String triggerQuery = "TRIGGER";
		final String expectationQuery = "SETUP:\n" + triggerQuery;

		Class.forName("org.baessie.simulator.jdbc.driver.SimulatorDriver");
		final PreparedStatement setupStatement = createStatement(expectationQuery);
		setupStatement.execute();
	}

	private PreparedStatement createStatement(final String expectationQuery) throws SQLException {
		final PreparedStatement setupStatement = DriverManager.getDriver("test").connect("", null).prepareStatement(expectationQuery);
		return setupStatement;
	}

}
