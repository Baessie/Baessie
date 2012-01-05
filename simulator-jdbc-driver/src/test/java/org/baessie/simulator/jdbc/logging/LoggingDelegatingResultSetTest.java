package org.baessie.simulator.jdbc.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class LoggingDelegatingResultSetTest {
	private final LoggerMock logger = new LoggerMock();
	private LoggingDelegatingResultSet resultSet;

	@Before
	public void setUp() throws Exception {
		DriverManager.registerDriver(new DriverMock());
		Connection connection = DriverManager.getConnection("jdbc:mock:thin:@host:1521:dbname", "user", "passwd");
		String select = "SELECT ID, VALUE, CREATED, DUMMY FROM TEMP WHERE ID = 123";

		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(Long.class);
		classes.add(String.class);
		classes.add(Timestamp.class);
		classes.add(String.class);
		PreparedStatementMock.addResultSet("SELECT ID, VALUE, CREATED, DUMMY FROM TEMP WHERE ID = 123", new ResultSetMock(Arrays.asList("ID", "VALUE", "CREATED", "DUMMY"), classes,
				new ResultSetRowMock(Long.valueOf(123), "String value", new Timestamp(0), null)));

		PreparedStatement selectStatement = connection.prepareStatement(select);
		selectStatement.execute();
		this.resultSet = new LoggingDelegatingResultSet(selectStatement.getResultSet(), logger);
		logger.reset();
	}

	@Test
	public void firstCallToNextLogsColumnNames() throws Exception {
		assertTrue("next", this.resultSet.next());
		assertTrue("log message", logger.wasLogged("'ID','VALUE','CREATED','DUMMY'", "TRACE"));
	}

	@Test
	public void nextLogsNextRowOfResults() throws Exception {
		assertTrue("next", this.resultSet.next());
		assertTrue("log message", logger.wasLogged("'java.lang.Long:123','java.lang.String:String value','java.sql.Timestamp:0','java.lang.String:'", "TRACE"));
	}

	@Test
	public void itIsStillPossibleToGetRowValuesAfterNextHasLoggedResults() throws Exception {
		assertTrue("next", this.resultSet.next());
		assertEquals("string value", "String value", resultSet.getString("VALUE"));
	}

	@Test
	public void nullValuesAreReturnedAsAnEmptyStringForGetString() throws Exception {
		this.resultSet.next();
		assertEquals("", resultSet.getString(4));
	}

}
