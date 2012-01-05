package org.baessie.simulator.jdbc.logging;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;

public class LoggingDelegatingPreparedStatement implements PreparedStatement {
	private static final String SETUP = "SETUP:\n";
	private final PreparedStatement wrappedStatement;
	private final String sql;
	private final Logger logger;
	private final List<String> logMessages = new ArrayList<String>();
	private final List<String> parameterValues = new ArrayList<String>();

	public LoggingDelegatingPreparedStatement(final PreparedStatement wrappedStatement, String sql, final Logger logger) {
		this.wrappedStatement = wrappedStatement;
		this.sql = sql;
		this.logger = logger;
	}

	private void addParameterValue(int parameterIndex, String value) {
		while (parameterValues.size() <= parameterIndex) {
			parameterValues.add(null);
		}
		this.parameterValues.set(parameterIndex, "'" + value + "'");
	}

	private void addParameterValue(int parameterIndex, java.util.Date date) {
		if (date != null) {
			addParameterValue(parameterIndex, String.valueOf(date.getTime()));
		} else {
			addParameterValue(parameterIndex, "<null>");
		}
	}

	private String updateQueryWithParameterValues(String query) {
		String result = query;
		int pos = 1;
		while (result.contains("?")) {
			if (pos > parameterValues.size()) {
				result = result.replaceFirst("\\?", "<null>");
			} else {
				result = result.replaceFirst("\\?", parameterValues.get(pos++));
			}
		}
		return result;
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return new LoggingDelegatingResultSet(wrappedStatement.executeQuery(sql), logger);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return wrappedStatement.unwrap(iface);
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		logMessages.add(SETUP + updateQueryWithParameterValues(sql));
		outputLoggedMessages();
		return new LoggingDelegatingResultSet(wrappedStatement.executeQuery(), logger);
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		StringBuilder logMessage = new StringBuilder(SETUP + sql);
		int rowcount = wrappedStatement.executeUpdate(sql);
		logMessage.append("\n'DUMMY'");
		for (int i = 0; i < rowcount; i++) {
			logMessage.append("\n'<null>'");
		}
		logger.trace(logMessage.toString());
		return rowcount;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return wrappedStatement.isWrapperFor(iface);
	}

	@Override
	public int executeUpdate() throws SQLException {
		StringBuilder logMessage = new StringBuilder(SETUP + updateQueryWithParameterValues(sql));
		int rowcount = wrappedStatement.executeUpdate();
		logMessage.append("\n'DUMMY'");
		for (int i = 0; i < rowcount; i++) {
			logMessage.append("\n'<null>'");
		}
		logger.trace(logMessage.toString());
		return rowcount;
	}

	@Override
	public void close() throws SQLException {
		wrappedStatement.close();
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		addParameterValue(parameterIndex, "");
		wrappedStatement.setNull(parameterIndex, sqlType);
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return wrappedStatement.getMaxFieldSize();
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setBoolean(parameterIndex, x);
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		wrappedStatement.setMaxFieldSize(max);
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setByte(parameterIndex, x);
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setShort(parameterIndex, x);
	}

	@Override
	public int getMaxRows() throws SQLException {
		return wrappedStatement.getMaxRows();
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setInt(parameterIndex, x);
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		wrappedStatement.setMaxRows(max);
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setLong(parameterIndex, x);
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		wrappedStatement.setEscapeProcessing(enable);
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setFloat(parameterIndex, x);
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return wrappedStatement.getQueryTimeout();
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setDouble(parameterIndex, x);
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		wrappedStatement.setQueryTimeout(seconds);
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		addParameterValue(parameterIndex, String.valueOf(x));
		wrappedStatement.setBigDecimal(parameterIndex, x);
	}

	@Override
	public void cancel() throws SQLException {
		wrappedStatement.cancel();
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		addParameterValue(parameterIndex, emptyStringIfNull(x));
		wrappedStatement.setString(parameterIndex, x);
	}

	private String emptyStringIfNull(Object x) {
		if (x == null) {
			return "";
		}
		return String.valueOf(x);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return wrappedStatement.getWarnings();
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		wrappedStatement.setBytes(parameterIndex, x);
	}

	@Override
	public void clearWarnings() throws SQLException {
		wrappedStatement.clearWarnings();
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		addParameterValue(parameterIndex, x);
		wrappedStatement.setDate(parameterIndex, x);
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		wrappedStatement.setCursorName(name);
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		addParameterValue(parameterIndex, x);
		wrappedStatement.setTime(parameterIndex, x);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		addParameterValue(parameterIndex, x);
		wrappedStatement.setTimestamp(parameterIndex, x);
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return wrappedStatement.execute(sql);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		wrappedStatement.setAsciiStream(parameterIndex, x, length);
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		outputLoggedMessages();
		return new LoggingDelegatingResultSet(wrappedStatement.getResultSet(), logger);
	}

	private void outputLoggedMessages() {
		for (String message : logMessages) {
			logger.trace(message);
		}
		logMessages.clear();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		wrappedStatement.setUnicodeStream(parameterIndex, x, length);
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return wrappedStatement.getUpdateCount();
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		return wrappedStatement.getMoreResults();
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		wrappedStatement.setBinaryStream(parameterIndex, x, length);
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		wrappedStatement.setFetchDirection(direction);
	}

	@Override
	public void clearParameters() throws SQLException {
		parameterValues.clear();
		wrappedStatement.clearParameters();
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return wrappedStatement.getFetchDirection();
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		if (x instanceof java.util.Date) {
			addParameterValue(parameterIndex, (java.util.Date) x);
		} else {
			addParameterValue(parameterIndex, String.valueOf(x));
		}
		wrappedStatement.setObject(parameterIndex, x, targetSqlType);
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		wrappedStatement.setFetchSize(rows);
	}

	@Override
	public int getFetchSize() throws SQLException {
		return wrappedStatement.getFetchSize();
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		if (x instanceof java.util.Date) {
			addParameterValue(parameterIndex, (java.util.Date) x);
		} else {
			addParameterValue(parameterIndex, emptyStringIfNull(x));
		}
		wrappedStatement.setObject(parameterIndex, x);
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return wrappedStatement.getResultSetConcurrency();
	}

	@Override
	public int getResultSetType() throws SQLException {
		return wrappedStatement.getResultSetType();
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		wrappedStatement.addBatch(sql);
	}

	@Override
	public void clearBatch() throws SQLException {
		wrappedStatement.clearBatch();
	}

	@Override
	public boolean execute() throws SQLException {
		logMessages.add(SETUP + updateQueryWithParameterValues(sql));
		outputLoggedMessages();
		return wrappedStatement.execute();
	}

	@Override
	public int[] executeBatch() throws SQLException {
		logMessages.add(SETUP + updateQueryWithParameterValues(sql));
		outputLoggedMessages();
		return wrappedStatement.executeBatch();
	}

	@Override
	public void addBatch() throws SQLException {
		wrappedStatement.addBatch();
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		wrappedStatement.setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		wrappedStatement.setRef(parameterIndex, x);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return wrappedStatement.getConnection();
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		wrappedStatement.setBlob(parameterIndex, x);
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		wrappedStatement.setClob(parameterIndex, x);
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		return wrappedStatement.getMoreResults(current);
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		wrappedStatement.setArray(parameterIndex, x);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return wrappedStatement.getMetaData();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return wrappedStatement.getGeneratedKeys();
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		addParameterValue(parameterIndex, x);
		wrappedStatement.setDate(parameterIndex, x, cal);
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return wrappedStatement.executeUpdate(sql, autoGeneratedKeys);
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		addParameterValue(parameterIndex, x);
		wrappedStatement.setTime(parameterIndex, x, cal);
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return wrappedStatement.executeUpdate(sql, columnIndexes);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		addParameterValue(parameterIndex, x);
		wrappedStatement.setTimestamp(parameterIndex, x, cal);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		addParameterValue(parameterIndex, "");
		wrappedStatement.setNull(parameterIndex, sqlType, typeName);
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return wrappedStatement.executeUpdate(sql, columnNames);
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return wrappedStatement.execute(sql, autoGeneratedKeys);
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		wrappedStatement.setURL(parameterIndex, x);
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return wrappedStatement.getParameterMetaData();
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		wrappedStatement.setRowId(parameterIndex, x);
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return wrappedStatement.execute(sql, columnIndexes);
	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {
		wrappedStatement.setNString(parameterIndex, value);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		wrappedStatement.setNCharacterStream(parameterIndex, value, length);
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		logMessages.add(SETUP + sql);
		outputLoggedMessages();
		return wrappedStatement.execute(sql, columnNames);
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		wrappedStatement.setNClob(parameterIndex, value);
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		wrappedStatement.setClob(parameterIndex, reader, length);
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return wrappedStatement.getResultSetHoldability();
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		wrappedStatement.setBlob(parameterIndex, inputStream, length);
	}

	@Override
	public boolean isClosed() throws SQLException {
		return wrappedStatement.isClosed();
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		wrappedStatement.setPoolable(poolable);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		wrappedStatement.setNClob(parameterIndex, reader, length);
	}

	@Override
	public boolean isPoolable() throws SQLException {
		return wrappedStatement.isPoolable();
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		wrappedStatement.setSQLXML(parameterIndex, xmlObject);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		if (x instanceof java.util.Date) {
			addParameterValue(parameterIndex, (java.util.Date) x);
		} else {
			addParameterValue(parameterIndex, String.valueOf(x));
		}
		wrappedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		wrappedStatement.setAsciiStream(parameterIndex, x, length);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		wrappedStatement.setBinaryStream(parameterIndex, x, length);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		wrappedStatement.setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		wrappedStatement.setAsciiStream(parameterIndex, x);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		wrappedStatement.setBinaryStream(parameterIndex, x);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		wrappedStatement.setCharacterStream(parameterIndex, reader);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		wrappedStatement.setNCharacterStream(parameterIndex, value);
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		wrappedStatement.setClob(parameterIndex, reader);
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		wrappedStatement.setBlob(parameterIndex, inputStream);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		wrappedStatement.setNClob(parameterIndex, reader);
	}

}
