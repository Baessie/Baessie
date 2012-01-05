package org.baessie.simulator.jdbc.logging;

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
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;

public class LoggingDelegatingConnection implements Connection {
	private final Connection wrappedConnection;
	private final Logger logger;

	public LoggingDelegatingConnection(final Connection wrappedConnection, final Logger logger) {
		this.wrappedConnection = wrappedConnection;
		this.logger = logger;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return (T) wrappedConnection;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isAssignableFrom(wrappedConnection.getClass());
	}

	@Override
	public Statement createStatement() throws SQLException {
		return wrappedConnection.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new LoggingDelegatingPreparedStatement(wrappedConnection.prepareStatement(sql), sql, logger);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return wrappedConnection.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return wrappedConnection.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		wrappedConnection.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return wrappedConnection.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		wrappedConnection.commit();
	}

	@Override
	public void rollback() throws SQLException {
		wrappedConnection.rollback();
	}

	@Override
	public void close() throws SQLException {
		wrappedConnection.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return wrappedConnection.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return wrappedConnection.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		wrappedConnection.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return wrappedConnection.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		wrappedConnection.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return wrappedConnection.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		wrappedConnection.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return wrappedConnection.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return wrappedConnection.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		wrappedConnection.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return wrappedConnection.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return new LoggingDelegatingPreparedStatement(wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency), sql, logger);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return wrappedConnection.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		wrappedConnection.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		wrappedConnection.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return wrappedConnection.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return wrappedConnection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return wrappedConnection.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		wrappedConnection.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		wrappedConnection.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return wrappedConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return wrappedConnection.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return wrappedConnection.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return wrappedConnection.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return wrappedConnection.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return wrappedConnection.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return wrappedConnection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return wrappedConnection.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return wrappedConnection.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		wrappedConnection.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		wrappedConnection.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return wrappedConnection.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return wrappedConnection.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return wrappedConnection.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return wrappedConnection.createStruct(typeName, attributes);
	}

}
