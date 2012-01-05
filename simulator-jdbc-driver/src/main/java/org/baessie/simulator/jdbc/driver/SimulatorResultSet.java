package org.baessie.simulator.jdbc.driver;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.baessie.simulator.jdbc.expectation.ExpectedResult;
import org.baessie.simulator.jdbc.expectation.Logger;
import org.baessie.simulator.jdbc.expectation.Row;

public class SimulatorResultSet implements ResultSet {
	private final ExpectedResult expectedResult;
	private int currentRowIndex = -1;

	public SimulatorResultSet(final ExpectedResult expectedResult) {
		this.expectedResult = expectedResult;
	}

	@Override
	public boolean next() throws SQLException {
		Logger.log("SimulatorResultSet.next()");
		currentRowIndex += 1;
		return currentRowIndex < getFetchSize();
	}

	@Override
	public int getRow() throws SQLException {
		Logger.log("SimulatorResultSet.getRow()");
		return currentRowIndex + 1;
	}

	@Override
	public int getFetchSize() throws SQLException {
		Logger.log("SimulatorResultSet.getFetchSize()");
		return expectedResult.getRows().size();
	}

	@Override
	public boolean isClosed() throws SQLException {
		Logger.log("SimulatorResultSet.isClosed()");
		return true;
	}

	private Row currentRow() {
		return expectedResult.getRows().get(currentRowIndex);
	}

	@Override
	public String getString(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getString(columnIndex);
	}

	@Override
	public String getString(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getString()");
		return getTypeStrippedValue(columnIndex);
	}

	@Override
	public boolean getBoolean(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getBoolean()");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return false;
		} else {
			return BooleanUtils.toBoolean(value);
		}
	}

	@Override
	public byte getByte(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getByte()");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return 0;
		} else {
			return Byte.parseByte(value);
		}
	}

	@Override
	public short getShort(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getShort()");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return 0;
		} else {
			return Short.parseShort(value);
		}
	}

	@Override
	public int getInt(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getInt()");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return 0;
		} else {
			return Integer.parseInt(value);
		}
	}

	@Override
	public long getLong(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getLong()");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return 0L;
		} else {
			return Long.parseLong(value);
		}
	}

	@Override
	public float getFloat(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getFloat()");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return 0F;
		} else {
			return Float.parseFloat(value);
		}
	}

	@Override
	public double getDouble(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getDouble()");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return 0D;
		} else {
			return Double.parseDouble(value);
		}
	}

	@Override
	public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
		Logger.log("SimulatorResultSet.getBigDecimal()1");
		String value = getTypeStrippedValue(columnIndex);
		if (isNull(value)) {
			return null;
		} else {
			return new BigDecimal(value).setScale(scale, RoundingMode.HALF_EVEN);
		}
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		Logger.log("SimulatorResultSet.getMetaData()");
		return new SimulatorResultSetMetaData(expectedResult);
	}

	private String getTypeStrippedValue(final int columnIndex) {
		return stripType(currentRow().getStringAt(columnIndex));
	}

	private String stripType(String currentColumnValue) {
		if (isNull(currentColumnValue)) {
			return null;
		}
		return currentColumnValue.replaceFirst("^.*?:", "");
	}

	private void logFunc(final String funcName, final String value) {
		Logger.log(String.format("SimulatorResultSet.%s(%s)", funcName, value));
	}

	@Override
	public Date getDate(final int columnIndex) throws SQLException {
		String columnValue = getTypeStrippedValue(columnIndex);
		logFunc("getDate", columnValue);
		if (isNull(columnValue)) {
			return null;
		} else {
			return new Date(Long.parseLong(columnValue));
		}
	}

	@Override
	public Time getTime(final int columnIndex) throws SQLException {
		String columnValue = getTypeStrippedValue(columnIndex);
		logFunc("getTime", columnValue);
		if (isNull(columnValue)) {
			return null;
		} else {
			return new Time(Long.parseLong(columnValue));
		}
	}

	@Override
	public Timestamp getTimestamp(final int columnIndex) throws SQLException {
		String columnValue = getTypeStrippedValue(columnIndex);
		logFunc("getTimestamp", columnValue);
		if (isNull(columnValue)) {
			return null;
		} else {
			return new Timestamp(Long.parseLong(columnValue));
		}
	}

	@Override
	public boolean getBoolean(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getBoolean(columnIndex);
	}

	@Override
	public byte getByte(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getByte(columnIndex);
	}

	@Override
	public short getShort(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getShort(columnIndex);
	}

	@Override
	public int getInt(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getInt(columnIndex);
	}

	@Override
	public long getLong(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getLong(columnIndex);
	}

	@Override
	public float getFloat(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getFloat(columnIndex);
	}

	@Override
	public double getDouble(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getDouble(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getBigDecimal(columnIndex, scale);
	}

	@Override
	public Date getDate(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getDate(columnIndex);
	}

	@Override
	public Time getTime(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getTime(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getTimestamp(columnIndex);
	}

	@Override
	public Object getObject(final int columnIndex) throws SQLException {
		String columnValue = currentRow().getStringAt(columnIndex);
		String[] parts = columnValue.split(":");
		if (parts.length == 2) {
			if ("java.lang.String".equals(parts[0])) {
				return getString(columnIndex);
			} else if ("java.lang.Boolean".equals(parts[0])) {
				return getBoolean(columnIndex);
			} else if ("java.lang.Byte".equals(parts[0])) {
				return getByte(columnIndex);
			} else if ("java.lang.Short".equals(parts[0])) {
				return getShort(columnIndex);
			} else if ("java.lang.Integer".equals(parts[0])) {
				return getInt(columnIndex);
			} else if ("java.lang.Long".equals(parts[0])) {
				return getLong(columnIndex);
			} else if ("java.lang.Float".equals(parts[0])) {
				return getFloat(columnIndex);
			} else if ("java.lang.Double".equals(parts[0])) {
				return getDouble(columnIndex);
			} else if ("java.math.BigDecimal".equals(parts[0])) {
				return getBigDecimal(columnIndex);
			} else if ("java.sql.Date".equals(parts[0])) {
				return getDate(columnIndex);
			} else if ("java.sql.Time".equals(parts[0])) {
				return getTime(columnIndex);
			} else if ("java.sql.Timestamp".equals(parts[0])) {
				return getTimestamp(columnIndex);
			}
		} else {
			return columnValue;
		}
		return null;
	}

	@Override
	public Object getObject(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getObject(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getBigDecimal()1");
		String columnValue = getTypeStrippedValue(columnIndex);
		if (isNull(columnValue)) {
			return null;
		} else {
			return new BigDecimal(columnValue);
		}
	}

	@Override
	public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
		final int columnIndex = expectedResult.getColumnIndexForColumnLabel(columnLabel);
		return getBigDecimal(columnIndex);
	}

	@Override
	public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
		// TODO: adjust for calendar
		return getDate(columnIndex);
	}

	@Override
	public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
		// TODO: adjust for calendar
		return getDate(columnLabel);
	}

	@Override
	public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
		// TODO: adjust for calendar
		return getTime(columnIndex);
	}

	@Override
	public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
		// TODO: adjust for calendar
		return getTime(columnLabel);
	}

	@Override
	public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
		// TODO: adjust for calendar
		return getTimestamp(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
		// TODO: adjust for calendar
		return getTimestamp(columnLabel);
	}

	private boolean isNull(final String value) {
		return (value == null || value.equals("null"));
	}

	// --- Unimplemented methods below

	@Override
	public byte[] getBytes(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getBytes()2");
		return null;
	}

	@Override
	public InputStream getAsciiStream(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getAsciiStream()1");
		return null;
	}

	@Override
	public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getUnicodeStream()1");
		return null;
	}

	@Override
	public InputStream getBinaryStream(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getBinaryStream()1");
		return null;
	}

	@Override
	public InputStream getAsciiStream(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getAsciiStream()2");
		return null;
	}

	@Override
	public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getUnicodeStream()2");
		return null;
	}

	@Override
	public InputStream getBinaryStream(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getBinaryStream()2");
		return null;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		Logger.log("SimulatorResultSet.getWarnings()");
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		Logger.log("SimulatorResultSet.clearWarnings()");

	}

	@Override
	public String getCursorName() throws SQLException {
		Logger.log("SimulatorResultSet.getCursorName()");
		return null;
	}

	@Override
	public int findColumn(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.findColumn()");
		return 0;
	}

	@Override
	public Reader getCharacterStream(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getCharacterStream()");
		return null;
	}

	@Override
	public Reader getCharacterStream(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getCharacterStream()");
		return null;
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		Logger.log("SimulatorResultSet.isBeforeFirst()");
		return false;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		Logger.log("SimulatorResultSet.isAfterLast()");
		return false;
	}

	@Override
	public boolean isFirst() throws SQLException {
		Logger.log("SimulatorResultSet.isFirst()");
		return false;
	}

	@Override
	public boolean isLast() throws SQLException {
		Logger.log("SimulatorResultSet.isLast()");
		return false;
	}

	@Override
	public void beforeFirst() throws SQLException {
		Logger.log("SimulatorResultSet.beforeFirst()");
	}

	@Override
	public void afterLast() throws SQLException {
		Logger.log("SimulatorResultSet.afterLast()");
	}

	@Override
	public boolean first() throws SQLException {
		Logger.log("SimulatorResultSet.first()");
		return false;
	}

	@Override
	public boolean last() throws SQLException {
		Logger.log("SimulatorResultSet.last()");
		return false;
	}

	@Override
	public boolean absolute(final int row) throws SQLException {
		Logger.log("SimulatorResultSet.absolute()");
		return false;
	}

	@Override
	public boolean relative(final int rows) throws SQLException {
		Logger.log("SimulatorResultSet.relative()");
		return false;
	}

	@Override
	public boolean previous() throws SQLException {
		Logger.log("SimulatorResultSet.previous()");
		return false;
	}

	@Override
	public void setFetchDirection(final int direction) throws SQLException {
		Logger.log("SimulatorResultSet.setFetchDirection()");
	}

	@Override
	public int getFetchDirection() throws SQLException {
		Logger.log("SimulatorResultSet.getFetchDirection()");
		return 0;
	}

	@Override
	public void setFetchSize(final int rows) throws SQLException {
		Logger.log("SimulatorResultSet.setFetchSize()");
	}

	@Override
	public int getType() throws SQLException {
		Logger.log("SimulatorResultSet.getType()");
		return 0;
	}

	@Override
	public int getConcurrency() throws SQLException {
		Logger.log("SimulatorResultSet.getConcurrency()");
		return 0;
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		Logger.log("SimulatorResultSet.rowUpdated()");
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException {
		Logger.log("SimulatorResultSet.rowInserted()");
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		Logger.log("SimulatorResultSet.rowDeleted()");
		return false;
	}

	@Override
	public void updateNull(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.updateNull()");
	}

	@Override
	public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBoolean()");
	}

	@Override
	public void updateByte(final int columnIndex, final byte x) throws SQLException {
		Logger.log("SimulatorResultSet.updateByte()");
	}

	@Override
	public void updateShort(final int columnIndex, final short x) throws SQLException {
		Logger.log("SimulatorResultSet.updateShort()");
	}

	@Override
	public void updateInt(final int columnIndex, final int x) throws SQLException {
		Logger.log("SimulatorResultSet.updateInt()");
	}

	@Override
	public void updateLong(final int columnIndex, final long x) throws SQLException {
		Logger.log("SimulatorResultSet.updateLong()");
	}

	@Override
	public void updateFloat(final int columnIndex, final float x) throws SQLException {
		Logger.log("SimulatorResultSet.updateFloat()");
	}

	@Override
	public void updateDouble(final int columnIndex, final double x) throws SQLException {
		Logger.log("SimulatorResultSet.updateDouble()");
	}

	@Override
	public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBigDecimal()");
	}

	@Override
	public void updateString(final int columnIndex, final String x) throws SQLException {
		Logger.log("SimulatorResultSet.updateString()");
	}

	@Override
	public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBytes()");
	}

	@Override
	public void updateDate(final int columnIndex, final Date x) throws SQLException {
		Logger.log("SimulatorResultSet.updateDate()");
	}

	@Override
	public void updateTime(final int columnIndex, final Time x) throws SQLException {
		Logger.log("SimulatorResultSet.updateTime()");
	}

	@Override
	public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
		Logger.log("SimulatorResultSet.updateTimestamp()");
	}

	@Override
	public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
		Logger.log("SimulatorResultSet.updateAsciiStream()1");
	}

	@Override
	public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
		Logger.log("SimulatorResultSet.updateBinaryStream()1");
	}

	@Override
	public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
		Logger.log("SimulatorResultSet.updateCharacterStream()1");
	}

	@Override
	public void updateObject(final int columnIndex, final Object x, final int scaleOrLength) throws SQLException {
		Logger.log("SimulatorResultSet.updateObject()1");
	}

	@Override
	public void updateObject(final int columnIndex, final Object x) throws SQLException {
		Logger.log("SimulatorResultSet.updateObject()");
	}

	@Override
	public void updateNull(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.updateNull()");
	}

	@Override
	public void updateBoolean(final String columnLabel, final boolean x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBoolean()");
	}

	@Override
	public void updateByte(final String columnLabel, final byte x) throws SQLException {
		Logger.log("SimulatorResultSet.updateByte()");
	}

	@Override
	public void updateShort(final String columnLabel, final short x) throws SQLException {
		Logger.log("SimulatorResultSet.updateShort()");
	}

	@Override
	public void updateInt(final String columnLabel, final int x) throws SQLException {
		Logger.log("SimulatorResultSet.updateInt()");
	}

	@Override
	public void updateLong(final String columnLabel, final long x) throws SQLException {
		Logger.log("SimulatorResultSet.updateLong()");
	}

	@Override
	public void updateFloat(final String columnLabel, final float x) throws SQLException {
		Logger.log("SimulatorResultSet.updateFloat()");
	}

	@Override
	public void updateDouble(final String columnLabel, final double x) throws SQLException {
		Logger.log("SimulatorResultSet.updateDouble()");
	}

	@Override
	public void updateBigDecimal(final String columnLabel, final BigDecimal x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBigDecimal()");
	}

	@Override
	public void updateString(final String columnLabel, final String x) throws SQLException {
		Logger.log("SimulatorResultSet.updateString()");
	}

	@Override
	public void updateBytes(final String columnLabel, final byte[] x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBytes()");
	}

	@Override
	public void updateDate(final String columnLabel, final Date x) throws SQLException {
		Logger.log("SimulatorResultSet.updateDate()");
	}

	@Override
	public void updateTime(final String columnLabel, final Time x) throws SQLException {
		Logger.log("SimulatorResultSet.updateTime()");
	}

	@Override
	public void updateTimestamp(final String columnLabel, final Timestamp x) throws SQLException {
		Logger.log("SimulatorResultSet.updateTimestamp()");
	}

	@Override
	public void updateAsciiStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
		Logger.log("SimulatorResultSet.updateAsciiStream()2");
	}

	@Override
	public void updateBinaryStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
		Logger.log("SimulatorResultSet.updateBinaryStream()2");
	}

	@Override
	public void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException {
		Logger.log("SimulatorResultSet.updateCharacterStream()2");
	}

	@Override
	public void updateObject(final String columnLabel, final Object x, final int scaleOrLength) throws SQLException {
		Logger.log("SimulatorResultSet.updateObject()");
	}

	@Override
	public void updateObject(final String columnLabel, final Object x) throws SQLException {
		Logger.log("SimulatorResultSet.updateObject()");
	}

	@Override
	public void insertRow() throws SQLException {
		Logger.log("SimulatorResultSet.insertRow()");
	}

	@Override
	public void updateRow() throws SQLException {
		Logger.log("SimulatorResultSet.updateRow()");
	}

	@Override
	public void deleteRow() throws SQLException {
		Logger.log("SimulatorResultSet.deleteRow()");
	}

	@Override
	public void refreshRow() throws SQLException {
		Logger.log("SimulatorResultSet.refreshRow()");
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		Logger.log("SimulatorResultSet.cancelRowUpdates()");
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		Logger.log("SimulatorResultSet.moveToInsertRow()");
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		Logger.log("SimulatorResultSet.moveToCurrentRow()");
	}

	@Override
	public Statement getStatement() throws SQLException {
		Logger.log("SimulatorResultSet.getStatement()");
		return null;
	}

	@Override
	public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
		Logger.log("SimulatorResultSet.getObject()");
		return null;
	}

	@Override
	public Ref getRef(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getRef()");
		return null;
	}

	@Override
	public Blob getBlob(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getBlob()");
		return null;
	}

	@Override
	public Clob getClob(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getClob()");
		return null;
	}

	@Override
	public Array getArray(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getArray()");
		return null;
	}

	@Override
	public Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
		Logger.log("SimulatorResultSet.getObject()");
		return null;
	}

	@Override
	public Ref getRef(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getRef()");
		return null;
	}

	@Override
	public Blob getBlob(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getBlob()");
		return null;
	}

	@Override
	public Clob getClob(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getClob()");
		return null;
	}

	@Override
	public Array getArray(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getArray()");
		return null;
	}

	@Override
	public URL getURL(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getURL()");
		return null;
	}

	@Override
	public URL getURL(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getURL()");
		return null;
	}

	@Override
	public void updateRef(final int columnIndex, final Ref x) throws SQLException {
		Logger.log("SimulatorResultSet.updateRef()");
	}

	@Override
	public void updateRef(final String columnLabel, final Ref x) throws SQLException {
		Logger.log("SimulatorResultSet.updateRef()");
	}

	@Override
	public void updateBlob(final int columnIndex, final Blob x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBlob()1");
	}

	@Override
	public void updateBlob(final String columnLabel, final Blob x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBlob()2");
	}

	@Override
	public void updateClob(final int columnIndex, final Clob x) throws SQLException {
		Logger.log("SimulatorResultSet.updateClob()1");
	}

	@Override
	public void updateClob(final String columnLabel, final Clob x) throws SQLException {
		Logger.log("SimulatorResultSet.updateClob()2");
	}

	@Override
	public void updateArray(final int columnIndex, final Array x) throws SQLException {
		Logger.log("SimulatorResultSet.updateArray()1");
	}

	@Override
	public void updateArray(final String columnLabel, final Array x) throws SQLException {
		Logger.log("SimulatorResultSet.updateArray()2");
	}

	@Override
	public RowId getRowId(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getRowId()");
		return null;
	}

	@Override
	public RowId getRowId(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getRowId()");
		return null;
	}

	@Override
	public void updateRowId(final int columnIndex, final RowId x) throws SQLException {
		Logger.log("SimulatorResultSet.updateRowId()1");
	}

	@Override
	public void updateRowId(final String columnLabel, final RowId x) throws SQLException {
		Logger.log("SimulatorResultSet.updateRowId()2");
	}

	@Override
	public int getHoldability() throws SQLException {
		Logger.log("SimulatorResultSet.getHoldability()");
		return 0;
	}

	@Override
	public void updateNString(final int columnIndex, final String nString) throws SQLException {
		Logger.log("SimulatorResultSet.updateNString()1");
	}

	@Override
	public void updateNString(final String columnLabel, final String nString) throws SQLException {
		Logger.log("SimulatorResultSet.updateNString()2");
	}

	@Override
	public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {
		Logger.log("SimulatorResultSet.updateNClob()1");
	}

	@Override
	public void updateNClob(final String columnLabel, final NClob nClob) throws SQLException {
		Logger.log("SimulatorResultSet.updateNClob()2");
	}

	@Override
	public NClob getNClob(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getNClob()1");
		return null;
	}

	@Override
	public NClob getNClob(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getNClob()2");
		return null;
	}

	@Override
	public SQLXML getSQLXML(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getSQLXML()");
		return null;
	}

	@Override
	public SQLXML getSQLXML(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getSQLXML()");
		return null;
	}

	@Override
	public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
		Logger.log("SimulatorResultSet.updateSQLXML()");
	}

	@Override
	public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
		Logger.log("SimulatorResultSet.updateSQLXML()");
	}

	@Override
	public String getNString(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getNString()");
		return null;
	}

	@Override
	public String getNString(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getNString()");
		return null;
	}

	@Override
	public Reader getNCharacterStream(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getNCharacterStream()");
		return null;
	}

	@Override
	public Reader getNCharacterStream(final String columnLabel) throws SQLException {
		Logger.log("SimulatorResultSet.getNCharacterStream()");
		return null;
	}

	@Override
	public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateNCharacterStream()1");
	}

	@Override
	public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateNCharacterStream()2");
	}

	@Override
	public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateAsciiStream()1");
	}

	@Override
	public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateBinaryStream()1");
	}

	@Override
	public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateCharacterStream()1");
	}

	@Override
	public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateAsciiStream()2");
	}

	@Override
	public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateBinaryStream()2");
	}

	@Override
	public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateCharacterStream()2");
	}

	@Override
	public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateBlob()1");
	}

	@Override
	public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateBlob()2");
	}

	@Override
	public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateClob()1");
	}

	@Override
	public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateClob()3");
	}

	@Override
	public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateNClob()1");
	}

	@Override
	public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
		Logger.log("SimulatorResultSet.updateNClob()2");
	}

	@Override
	public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {
		Logger.log("SimulatorResultSet.updateNCharacterStream()1");
	}

	@Override
	public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
		Logger.log("SimulatorResultSet.updateNCharacterStream()2");
	}

	@Override
	public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException {
		Logger.log("SimulatorResultSet.updateAsciiStream()1");
	}

	@Override
	public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBinaryStream()1");
	}

	@Override
	public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {
		Logger.log("SimulatorResultSet.updateCharacterStream()1");
	}

	@Override
	public void updateAsciiStream(final String columnLabel, final InputStream x) throws SQLException {
		Logger.log("SimulatorResultSet.updateAsciiStream()2");
	}

	@Override
	public void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException {
		Logger.log("SimulatorResultSet.updateBinaryStream()2");
	}

	@Override
	public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
		Logger.log("SimulatorResultSet.updateCharacterStream()2");
	}

	@Override
	public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
		Logger.log("SimulatorResultSet.updateBlob()1");
	}

	@Override
	public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
		Logger.log("SimulatorResultSet.updateBlob()2");
	}

	@Override
	public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
		Logger.log("SimulatorResultSet.updateClob()1");
	}

	@Override
	public void updateClob(final String columnLabel, final Reader reader) throws SQLException {
		Logger.log("SimulatorResultSet.updateClob()2");
	}

	@Override
	public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
		Logger.log("SimulatorResultSet.updateNClob()1");
	}

	@Override
	public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
		Logger.log("SimulatorResultSet.updateNClob()2");
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		Logger.log("SimulatorResultSet.unwrap()");
		return null;
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		Logger.log("SimulatorResultSet.isWrapperFor()");
		return false;
	}

	@Override
	public void close() throws SQLException {
		Logger.log("SimulatorResultSet.close()");
	}

	@Override
	public boolean wasNull() throws SQLException {
		Logger.log("SimulatorResultSet.wasNull()");
		return false;
	}

	@Override
	public byte[] getBytes(final int columnIndex) throws SQLException {
		Logger.log("SimulatorResultSet.getBytes()");
		return null;
	}

}
