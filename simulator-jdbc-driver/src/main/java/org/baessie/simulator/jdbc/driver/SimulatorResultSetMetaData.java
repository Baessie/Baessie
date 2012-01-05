package org.baessie.simulator.jdbc.driver;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.baessie.simulator.jdbc.expectation.ExpectedResult;
import org.baessie.simulator.jdbc.expectation.Logger;
import org.baessie.simulator.jdbc.expectation.Row;

public class SimulatorResultSetMetaData implements ResultSetMetaData {
	private final ExpectedResult expectedResult;

	public SimulatorResultSetMetaData(final ExpectedResult expectedResult) {
		this.expectedResult = expectedResult;
	}

	@Override
	public int getColumnCount() throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getColumnCount()");
		final List<Row> rows = expectedResult.getRows();
		if (rows.size() == 0) {
			return 0;
		}
		return rows.get(0).size();
	}

	@Override
	public String getColumnLabel(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getColumnLabel()");
		return expectedResult.getColumnLabelForColumnIndex(column);
	}

	@Override
	public String getColumnName(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getColumnName()");
		return getColumnLabel(column);
	}

	@Override
	public String getTableName(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getTableName()");
		return null;
	}

	@Override
	public int getColumnType(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getColumnType()");
		return 0;
	}

	// --- Unimplemented methods

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.unwrap()");
		return null;
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isWrapperFor()");
		return false;
	}

	@Override
	public boolean isAutoIncrement(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isAutoIncrement()");
		return false;
	}

	@Override
	public boolean isCaseSensitive(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isCaseSensitive()");
		return false;
	}

	@Override
	public boolean isSearchable(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isSearchable()");
		return false;
	}

	@Override
	public boolean isCurrency(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isCurrency()");
		return false;
	}

	@Override
	public int isNullable(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isNullable()");
		return 0;
	}

	@Override
	public boolean isSigned(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isSigned()");
		return false;
	}

	@Override
	public int getColumnDisplaySize(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getColumnDisplaySize()");
		return 0;
	}

	@Override
	public String getSchemaName(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getSchemaName()");
		return null;
	}

	@Override
	public int getPrecision(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getPrecision()");
		return 0;
	}

	@Override
	public int getScale(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getScale()");
		return 0;
	}

	@Override
	public String getCatalogName(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getCatalogName()");
		return null;
	}

	@Override
	public String getColumnTypeName(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getColumnTypeName()");
		return null;
	}

	@Override
	public boolean isReadOnly(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isReadOnly()");
		return false;
	}

	@Override
	public boolean isWritable(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isWritable()");
		return false;
	}

	@Override
	public boolean isDefinitelyWritable(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.isDefinitelyWritable()");
		return false;
	}

	@Override
	public String getColumnClassName(final int column) throws SQLException {
		Logger.log("SimulatorResultSetMetaData.getColumnClassName()");
		return null;
	}

}
