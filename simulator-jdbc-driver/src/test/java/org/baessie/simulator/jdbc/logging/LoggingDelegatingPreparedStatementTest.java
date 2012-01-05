package org.baessie.simulator.jdbc.logging;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class LoggingDelegatingPreparedStatementTest {
	private final LoggerMock logger = new LoggerMock();
	private PreparedStatement wrappedSelectStatement;
	private PreparedStatement wrappedParameterizedSelectStatement;
	private PreparedStatement wrappedParameterizedSelectStatement2;
	private PreparedStatement wrappedUpdateStatement;
	private PreparedStatement wrappedInsertStatement;
	private LoggingDelegatingPreparedStatement selectStatement;
	private LoggingDelegatingPreparedStatement parameterizedSelectStatement;
	private LoggingDelegatingPreparedStatement parameterizedSelectStatement2;
	private LoggingDelegatingPreparedStatement updateStatement;
	private LoggingDelegatingPreparedStatement insertStatement;

	@Before
	public void setUp() throws Exception {
		DriverManager.registerDriver(new DriverMock());
		Connection connection = DriverManager.getConnection("jdbc:mock:thin:@host:1521:dbname", "user", "passwd");
		String select = "SELECT 1 FROM DUAL";
		String parameterizedSelect = "SELECT VALUE FROM TEMP WHERE ID = ?";
		String parameterizedSelect2 = "SELECT VALUE FROM TEMP WHERE CREATED = ?";
		String update = "UPDATE TEMP SET VALUE = 'BEPA' WHERE ID = 123";
		String insert = "INSERT INTO TEMP (ID, VALUE) VALUES (?, ?)";
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(String.class);
		Object[] values = new Object[] { null };
		PreparedStatementMock.addResultSet(update, new ResultSetMock(Arrays.asList("DUMMY"), classes, new ResultSetRowMock(values), new ResultSetRowMock(values)));
		this.wrappedSelectStatement = connection.prepareStatement(select);
		this.wrappedParameterizedSelectStatement = connection.prepareStatement(parameterizedSelect);
		this.wrappedParameterizedSelectStatement2 = connection.prepareStatement(parameterizedSelect2);
		this.wrappedUpdateStatement = connection.prepareStatement(update);
		this.wrappedInsertStatement = connection.prepareStatement(insert);
		this.selectStatement = new LoggingDelegatingPreparedStatement(wrappedSelectStatement, select, logger);
		this.parameterizedSelectStatement = new LoggingDelegatingPreparedStatement(wrappedParameterizedSelectStatement, parameterizedSelect, logger);
		this.parameterizedSelectStatement2 = new LoggingDelegatingPreparedStatement(wrappedParameterizedSelectStatement2, parameterizedSelect2, logger);
		this.updateStatement = new LoggingDelegatingPreparedStatement(wrappedUpdateStatement, update, logger);
		this.insertStatement = new LoggingDelegatingPreparedStatement(wrappedInsertStatement, insert, logger);
		logger.reset();
	}

	@Test
	public void getResultSetReturnALoggingDelegating() throws Exception {
		selectStatement.execute("SELECT 'A' FROM DUAL");
		ResultSet resultSet = selectStatement.getResultSet();
		assertTrue("resultset", resultSet.getClass().isAssignableFrom(LoggingDelegatingResultSet.class));
	}

	@Test
	public void executeWithProvidedSqlLogsQueryAndResult() throws Exception {
		selectStatement.execute("SELECT 'A' FROM DUAL");
		selectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT 'A' FROM DUAL", "TRACE"));
	}

	@Test
	public void executeLogsQueryAndResult() throws Exception {
		selectStatement.execute();
		selectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT 1 FROM DUAL", "TRACE"));
	}

	@Test
	public void executeBatchLogsQueryAndResult() throws Exception {
		selectStatement.executeBatch();
		selectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT 1 FROM DUAL", "TRACE"));
	}

	@Test
	public void executeQueryWithProvidedSqlLogsQueryAndResult() throws Exception {
		selectStatement.executeQuery("SELECT 'A' FROM DUAL");
		selectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT 'A' FROM DUAL", "TRACE"));
	}

	@Test
	public void executeQueryLogsQueryAndResult() throws Exception {
		selectStatement.executeQuery();
		selectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT 1 FROM DUAL", "TRACE"));
	}

	@Test
	public void executeUpdateWithProvidedSqlLogsQueryAndResult() throws Exception {
		updateStatement.executeUpdate("UPDATE TEMP SET VALUE = 'BEPA' WHERE ID = 123");

		assertTrue("log message", logger.wasLogged("SETUP:\nUPDATE TEMP SET VALUE = 'BEPA' WHERE ID = 123\n'DUMMY'\n'<null>'\n'<null>'", "TRACE"));
	}

	@Test(expected = SQLException.class)
	public void executeUpdateWithProvidedSqlAndColumnIndexesThrowsSQLException() throws Exception {
		updateStatement.executeUpdate("UPDATE TEMP SET VALUE = 'APA' WHERE ID = 123", new int[] { 1 });
	}

	@Test(expected = SQLException.class)
	public void executeUpdateWithProvidedSqlAndColumnNamesThrowsSQLException() throws Exception {
		updateStatement.executeUpdate("UPDATE TEMP SET VALUE = 'APA' WHERE ID = 123", new String[] { "VALUE" });
	}

	@Test(expected = SQLException.class)
	public void executeUpdateWithProvidedSqlAndAutogeneratedKeysThrowsSQLException() throws Exception {
		updateStatement.executeUpdate("UPDATE TEMP SET VALUE = 'APA' WHERE ID = 123", Statement.NO_GENERATED_KEYS);
	}

	@Test(expected = SQLException.class)
	public void executeWithProvidedSqlAndAutogeneratedKeysThrowsSQLException() throws Exception {
		updateStatement.execute("UPDATE TEMP SET VALUE = 'APA' WHERE ID = 123", Statement.NO_GENERATED_KEYS);
	}

	@Test(expected = SQLException.class)
	public void executeWithProvidedSqlAndColumnIndexesThrowsSQLException() throws Exception {
		updateStatement.execute("UPDATE TEMP SET VALUE = 'APA' WHERE ID = 123", new int[] { 1 });
	}

	@Test(expected = SQLException.class)
	public void executeWithProvidedSqlAndColumnNamesThrowsSQLException() throws Exception {
		updateStatement.execute("UPDATE TEMP SET VALUE = 'APA' WHERE ID = 123", new String[] { "VALUE" });
	}

	@Test
	public void executeUpdateLogsQueryAndResult() throws Exception {
		updateStatement.executeUpdate();

		assertTrue("log message", logger.wasLogged("SETUP:\nUPDATE TEMP SET VALUE = 'BEPA' WHERE ID = 123\n'DUMMY'\n'<null>'\n'<null>'", "TRACE"));
	}

	@Test
	public void stringParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setString(1, "123");
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '123'", "TRACE"));
	}

	@Test
	public void nullParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setNull(1, Types.VARCHAR);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = ''", "TRACE"));
	}

	@Test
	public void nullParametersAreHandledCorrectlyForSpecifiedType() throws Exception {
		parameterizedSelectStatement.setNull(1, Types.VARCHAR, "java.lang.String");
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = ''", "TRACE"));
	}

	@Test
	public void setStringWithNullValueSetsAnEmptyString() throws Exception {
		insertStatement.setString(1, null);
		insertStatement.setObject(2, null);
		insertStatement.execute();

		assertTrue("log message", logger.wasLogged("SETUP:\nINSERT INTO TEMP (ID, VALUE) VALUES ('', '')", "TRACE"));
	}

	@Test
	public void setObjectWithNullValueSetsAnEmptyString() throws Exception {
		parameterizedSelectStatement.setObject(1, (String) null);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = ''", "TRACE"));
	}

	@Test
	public void booleanParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setBoolean(1, true);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = 'true'", "TRACE"));
	}

	@Test
	public void byteParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setByte(1, (byte) 65);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '65'", "TRACE"));
	}

	@Test
	public void shortParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setShort(1, (short) 67);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '67'", "TRACE"));
	}

	@Test
	public void intParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setInt(1, 69);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '69'", "TRACE"));
	}

	@Test
	public void longParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setLong(1, 73l);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '73'", "TRACE"));
	}

	@Test
	public void floatParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setFloat(1, 74.3f);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '74.3'", "TRACE"));
	}

	@Test
	public void doubleParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setDouble(1, 74.7d);
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '74.7'", "TRACE"));
	}

	@Test
	public void bigDecimalParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement.setBigDecimal(1, BigDecimal.valueOf(127l));
		parameterizedSelectStatement.execute();
		parameterizedSelectStatement.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE ID = '127'", "TRACE"));
	}

	@Test
	public void dateParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement2.setDate(1, new Date(0));
		parameterizedSelectStatement2.execute();
		parameterizedSelectStatement2.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE CREATED = '0'", "TRACE"));
	}

	@Test
	public void nullDateParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement2.setDate(1, null);
		parameterizedSelectStatement2.execute();
		parameterizedSelectStatement2.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE CREATED = '<null>'", "TRACE"));
	}

	@Test
	public void timeParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement2.setTime(1, new Time(0));
		parameterizedSelectStatement2.execute();
		parameterizedSelectStatement2.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE CREATED = '0'", "TRACE"));
	}

	@Test
	public void nullTimeParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement2.setTime(1, null);
		parameterizedSelectStatement2.execute();
		parameterizedSelectStatement2.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE CREATED = '<null>'", "TRACE"));
	}

	@Test
	public void timestampParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement2.setTimestamp(1, new Timestamp(0));
		parameterizedSelectStatement2.execute();
		parameterizedSelectStatement2.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE CREATED = '0'", "TRACE"));
	}

	@Test
	public void nullTimestampParametersAreHandledCorrectly() throws Exception {
		parameterizedSelectStatement2.setTimestamp(1, null);
		parameterizedSelectStatement2.execute();
		parameterizedSelectStatement2.getResultSet();

		assertTrue("log message", logger.wasLogged("SETUP:\nSELECT VALUE FROM TEMP WHERE CREATED = '<null>'", "TRACE"));
	}

}
