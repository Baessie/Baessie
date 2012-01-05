package org.baessie.simulator.jdbc.driver;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.baessie.simulator.jdbc.expectation.Expectation;
import org.baessie.simulator.jdbc.expectation.ExpectationManager;
import org.baessie.simulator.jdbc.expectation.ExpectedResult;
import org.baessie.simulator.jdbc.expectation.Logger;
import org.baessie.simulator.jdbc.expectation.Row;

public class SimulatorConnection implements Connection {

	@Override
	public PreparedStatement prepareStatement(final String sql) throws SQLException {
		Logger.log("SimulatorConnection.prepareStatement(%s)", sql);
		if (Expectation.isExpectationQuery(sql)) {
			addExpectation(sql);
			return new SimulatorPreparedStatement(createStatusOkResult());
		} else if (isResetQuery(sql)) {
			ExpectationManager.resetExpectations();
			return new SimulatorPreparedStatement(createStatusOkResult());
		} else if (isVerifyQuery(sql)) {
			int callCount = getCallCount(sql);
			return new SimulatorPreparedStatement(createCallCountResult(callCount));
		} else {
			return new SimulatorPreparedStatement(sql);
		}
	}

	private ExpectedResult createCallCountResult(int callCount) {
		return new ExpectedResult(new Row("CallCount"), list(new Row(String.valueOf(callCount))));
	}

	private int getCallCount(String sql) {
		final String[] parts = sql.split("\n");
		return ExpectationManager.getCallCountFor(parts[1]);
	}

	private boolean isVerifyQuery(String sql) {
		return sql.startsWith("VERIFY:");
	}

	private boolean isResetQuery(String sql) {
		return sql.startsWith("RESET");
	}

	private void addExpectation(final String sql) {
		final Expectation expectation = Expectation.fromExpectationQuery(sql);
		ExpectationManager.addExpectation(expectation);
	}

	private ExpectedResult createStatusOkResult() {
		return new ExpectedResult(new Row("Status"), list(new Row("OK")));
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		Logger.log("SimulatorConnection.getMetaData()");
		return new SimulatorDatabaseMetaData();
	}

	/**
	 * Returns a list of the given arguments.
	 * 
	 * @param <T>
	 *            type of the list.
	 * @param items
	 *            the items to make a list from.
	 * @return a list of the given arguments.
	 */
	public static <T> List<T> list(final T... items) {
		return Arrays.asList(items);
	}

	// --- Unimplemented methods below

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		Logger.log("SimulatorConnection.unwrap(%s)", iface);
		return null;
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		Logger.log("SimulatorConnection.isWrapperFor(%s)", iface);
		return false;
	}

	@Override
	public Statement createStatement() throws SQLException {
		Logger.log("SimulatorConnection.createStatement()");
		return null;
	}

	@Override
	public CallableStatement prepareCall(final String sql) throws SQLException {
		Logger.log("SimulatorConnection.prepareCall(%s)", sql);
		return null;
	}

	@Override
	public String nativeSQL(final String sql) throws SQLException {
		Logger.log("SimulatorConnection.nativeSQL(%s)", sql);
		return null;
	}

	@Override
	public void setAutoCommit(final boolean autoCommit) throws SQLException {
		Logger.log("SimulatorConnection.setAutoCommit(%s)", autoCommit);

	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		Logger.log("SimulatorConnection.getAutoCommit()");
		return false;
	}

	@Override
	public void commit() throws SQLException {
		Logger.log("SimulatorConnection.commit()");

	}

	@Override
	public void rollback() throws SQLException {
		Logger.log("SimulatorConnection.rollback()");

	}

	@Override
	public void close() throws SQLException {
		Logger.log("SimulatorConnection.close()");

	}

	@Override
	public boolean isClosed() throws SQLException {
		Logger.log("SimulatorConnection.isClosed()");
		return false;
	}

	@Override
	public void setReadOnly(final boolean readOnly) throws SQLException {
		Logger.log("SimulatorConnection.setReadOnly(%s)", readOnly);

	}

	@Override
	public boolean isReadOnly() throws SQLException {
		Logger.log("SimulatorConnection.isReadOnly()");
		return false;
	}

	@Override
	public void setCatalog(final String catalog) throws SQLException {
		Logger.log("SimulatorConnection.setCatalog(%s)", catalog);

	}

	@Override
	public String getCatalog() throws SQLException {
		Logger.log("SimulatorConnection.getCatalog()");
		return null;
	}

	@Override
	public void setTransactionIsolation(final int level) throws SQLException {
		Logger.log("SimulatorConnection.setTransactionIsolation(%s)", level);

	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		Logger.log("SimulatorConnection.getTransactionIsolation()");
		return 0;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		Logger.log("SimulatorConnection.getWarnings()");
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		Logger.log("SimulatorConnection.clearWarnings()");

	}

	@Override
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
		Logger.log("SimulatorConnection.createStatement(%s, %s)", resultSetType, resultSetConcurrency);
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
		Logger.log("SimulatorConnection.prepareStatement(%s, %s, %s)", sql, resultSetType, resultSetConcurrency);
		return null;
	}

	@Override
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
		Logger.log("SimulatorConnection.prepareCall(%s, %s, %s)", sql, resultSetType, resultSetConcurrency);
		return null;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		Logger.log("SimulatorConnection.getTypeMap()");
		return null;
	}

	@Override
	public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
		Logger.log("SimulatorConnection.setTypeMap(%s)", map);

	}

	@Override
	public void setHoldability(final int holdability) throws SQLException {
		Logger.log("SimulatorConnection.setHoldability(%s)", holdability);

	}

	@Override
	public int getHoldability() throws SQLException {
		Logger.log("SimulatorConnection.getHoldability()");
		return 0;
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		Logger.log("SimulatorConnection.setSavepoint()");
		return null;
	}

	@Override
	public Savepoint setSavepoint(final String name) throws SQLException {
		Logger.log("SimulatorConnection.setSavepoint(%s)", name);
		return null;
	}

	@Override
	public void rollback(final Savepoint savepoint) throws SQLException {
		Logger.log("SimulatorConnection.rollback(%s)", savepoint);

	}

	@Override
	public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
		Logger.log("SimulatorConnection.releaseSavepoint(%s)", savepoint);

	}

	@Override
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		Logger.log("SimulatorConnection.createStatement(%s, %s, %s)", resultSetType, resultSetConcurrency, resultSetHoldability);
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		Logger.log("SimulatorConnection.prepareStatement(%s, %s, %s)", resultSetType, resultSetConcurrency, resultSetHoldability);
		return null;
	}

	@Override
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		Logger.log("SimulatorConnection.prepareCall(%s, %s, %s)", resultSetType, resultSetConcurrency, resultSetHoldability);
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
		Logger.log("SimulatorConnection.prepareStatement(%s)", sql, autoGeneratedKeys);
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
		Logger.log("SimulatorConnection.prepareStatement(%s, %s)", sql, columnIndexes);
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
		Logger.log("SimulatorConnection.prepareStatement(%s, %s)", sql, columnNames);
		return null;
	}

	@Override
	public Clob createClob() throws SQLException {
		Logger.log("SimulatorConnection.createClob()");
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		Logger.log("SimulatorConnection.createBlob()");
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		Logger.log("SimulatorConnection.createNClob()");
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		Logger.log("SimulatorConnection.createSQLXML()");
		return null;
	}

	@Override
	public boolean isValid(final int timeout) throws SQLException {
		Logger.log("SimulatorConnection.isValid(%s)", timeout);
		return false;
	}

	@Override
	public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
		Logger.log("SimulatorConnection.setClientInfo(%s, %s)", name, value);

	}

	@Override
	public void setClientInfo(final Properties properties) throws SQLClientInfoException {
		Logger.log("SimulatorConnection.setClientInfo(%s)", properties);

	}

	@Override
	public String getClientInfo(final String name) throws SQLException {
		Logger.log("SimulatorConnection.getClientInfo(%s)", name);
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		Logger.log("SimulatorConnection.getClientInfo()");
		return null;
	}

	@Override
	public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
		Logger.log("SimulatorConnection.createArrayOf(%s, %s)", typeName, elements);
		return null;
	}

	@Override
	public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
		Logger.log("SimulatorConnection.createStruct(%s, %s)", typeName, attributes);
		return null;
	}

}
