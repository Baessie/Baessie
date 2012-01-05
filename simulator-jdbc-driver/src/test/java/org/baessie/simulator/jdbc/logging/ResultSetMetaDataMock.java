package org.baessie.simulator.jdbc.logging;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class ResultSetMetaDataMock implements ResultSetMetaData {
	private final List<String> columnNames;
	private final List<Class<?>> classes;

	public ResultSetMetaDataMock(List<String> columnNames, List<Class<?>> classes) {
		this.columnNames = columnNames;
		this.classes = classes;
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		return classes.get(column - 1).getName();
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columnNames.size();
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		return columnNames.get(column - 1);
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public int getScale(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public String getTableName(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public int isNullable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		throw new UnsupportedOperationException("Not yet implemented!!!");
	}

}
