package org.baessie.simulator.jdbc.driver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.baessie.simulator.jdbc.expectation.ExpectedResult;
import org.baessie.simulator.jdbc.expectation.Row;
import org.junit.Before;
import org.junit.Test;

public class SimulatorResultSetTest {
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private final ExpectedResult expectedResult = new ExpectedResult(new Row("string", "boolean", "null_value", "byte", "short", "int", "long", "float", "double", "bigdecimal", "date", "time",
			"timestamp", "unhandledtype"), Row.list(new Row("java.lang.String:abc", "java.lang.Boolean:true", null, "java.lang.Byte:111", "java.lang.Short:222", "java.lang.Integer:3333",
			"java.lang.Long:1234567", "java.lang.Float:1.3", "java.lang.Double:0.2", "java.math.BigDecimal:11.23", "java.sql.Date:1301678923456", "java.sql.Time:1301678923456",
			"java.sql.Timestamp:1301678923456", "java.lang.Class:java.lang.String", "blutti")));
	private final SimulatorResultSet resultSet = new SimulatorResultSet(expectedResult);

	@Before
	public void setUp() throws Exception {
		resultSet.next();
	}

	@Test
	public void getStringByIndexIsHandled() throws Exception {
		assertEquals("abc", resultSet.getString(1));
	}

	@Test
	public void getBooleanByIndexIsHandled() throws Exception {
		assertTrue(resultSet.getBoolean(2));
	}

	@Test
	public void getBooleanByNameIsHandled() throws Exception {
		assertTrue(resultSet.getBoolean("boolean"));
	}

	@Test
	public void getBooleanByIndexReturnFalseForNullValue() throws Exception {
		assertFalse(resultSet.getBoolean(3));
	}

	@Test
	public void getByteByIndexIsHandled() throws Exception {
		assertEquals(111, resultSet.getByte(4));
	}

	@Test
	public void getByteByNameIsHandled() throws Exception {
		assertEquals(111, resultSet.getByte("byte"));
	}

	@Test
	public void getByteByIndexReturnZeroForNullValue() throws Exception {
		assertEquals(0, resultSet.getByte(3));
	}

	@Test
	public void getShortByIndexIsHandled() throws Exception {
		assertEquals(222, resultSet.getShort(5));
	}

	@Test
	public void getShortByNameIsHandled() throws Exception {
		assertEquals(222, resultSet.getShort("short"));
	}

	@Test
	public void getShortByIndexReturnZeroForNullValue() throws Exception {
		assertEquals(0, resultSet.getShort(3));
	}

	@Test
	public void getIntByIndexIsHandled() throws Exception {
		assertEquals(3333, resultSet.getInt(6));
	}

	@Test
	public void getIntByNameIsHandled() throws Exception {
		assertEquals(3333, resultSet.getInt("int"));
	}

	@Test
	public void getIntByIndexReturnZeroForNullValue() throws Exception {
		assertEquals(0, resultSet.getInt(3));
	}

	@Test
	public void getLongByIndexIsHandled() throws Exception {
		assertEquals(1234567, resultSet.getLong(7));
	}

	@Test
	public void getLongByNameIsHandled() throws Exception {
		assertEquals(1234567, resultSet.getLong("long"));
	}

	@Test
	public void getLongByIndexReturnZeroForNullValue() throws Exception {
		assertEquals(0, resultSet.getLong(3));
	}

	@Test
	public void getFloatByIndexIsHandled() throws Exception {
		assertEquals(1.3, resultSet.getFloat(8), 0.0000001);
	}

	@Test
	public void getFloatByNameIsHandled() throws Exception {
		assertEquals(1.3, resultSet.getFloat("float"), 0.0000001);
	}

	@Test
	public void getFloatByIndexReturnZeroForNullValue() throws Exception {
		assertEquals(0, resultSet.getFloat(3), 0.0);
	}

	@Test
	public void getDoubleByIndexIsHandled() throws Exception {
		assertEquals(0.2, resultSet.getDouble(9), 0.0);
	}

	@Test
	public void getDoubleByNameIsHandled() throws Exception {
		assertEquals(0.2, resultSet.getDouble("double"), 0.0);
	}

	@Test
	public void getDoubleByIndexReturnZeroForNullValue() throws Exception {
		assertEquals(0, resultSet.getDouble(3), 0.0);
	}

	@Test
	public void getBigDecimalByIndexIsHandled() throws Exception {
		assertEquals(new BigDecimal("11.23"), resultSet.getBigDecimal(10));
	}

	@Test
	public void getBigDecimalByNameIsHandled() throws Exception {
		assertEquals(new BigDecimal("11.23"), resultSet.getBigDecimal("bigdecimal"));
	}

	@Test
	public void getBigDecimalByIndexReturnNullForNullValue() throws Exception {
		assertNull(resultSet.getBigDecimal(3));
	}

	@Test
	public void getBigDecimalWithScaleByIndexIsHandled() throws Exception {
		assertEquals(new BigDecimal("11.2"), resultSet.getBigDecimal(10, 1));
	}

	@Test
	public void getBigDecimalWithScaleByNameIsHandled() throws Exception {
		assertEquals(new BigDecimal("11.2"), resultSet.getBigDecimal("bigdecimal", 1));
	}

	@Test
	public void getBigDecimalWithScaleByIndexReturnNullForNullValue() throws Exception {
		assertNull(resultSet.getBigDecimal(3, 1));
	}

	@Test
	public void getDateByIndexIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getDate(11)));
	}

	@Test
	public void getDateWithCalendarByIndexIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getDate(11, Calendar.getInstance())));
	}

	@Test
	public void getDateByNameIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getDate("date")));
	}

	@Test
	public void getDateWithCalendarByNameIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getDate("date", Calendar.getInstance())));
	}

	@Test
	public void getDateByIndexReturnNullForNullValue() throws Exception {
		assertNull(resultSet.getDate(3));
	}

	@Test
	public void getTimeByIndexIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTime(12)));
	}

	@Test
	public void getTimeWithCalendarByIndexIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTime(12, Calendar.getInstance())));
	}

	@Test
	public void getTimeByNameIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTime("time")));
	}

	@Test
	public void getTimeWithCalendarByNameIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTime("time", Calendar.getInstance())));
	}

	@Test
	public void getTimeByIndexReturnNullForNullValue() throws Exception {
		assertNull(resultSet.getTime(3));
	}

	@Test
	public void getTimestampByIndexIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTimestamp(13)));
	}

	@Test
	public void getTimestampWithCalendarByIndexIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTimestamp(13, Calendar.getInstance())));
	}

	@Test
	public void getTimestampByNameIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTimestamp("time")));
	}

	@Test
	public void getTimestampWithCalendarByNameIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getTimestamp("time", Calendar.getInstance())));
	}

	@Test
	public void getTimeStampByIndexReturnNullForNullValue() throws Exception {
		assertNull(resultSet.getTimestamp(3));
	}

	@Test
	public void getObjectByIndexHandlesStringValues() throws Exception {
		assertEquals("abc", resultSet.getObject(1));
	}

	@Test
	public void getObjectByIndexHandlesBooleanValues() throws Exception {
		assertEquals(Boolean.TRUE, resultSet.getObject(2));
	}

	@Test
	public void getObjectByIndexHandlesByteValues() throws Exception {
		assertEquals(Byte.valueOf("111"), resultSet.getObject(4));
	}

	@Test
	public void getObjectByIndexHandlesShortValues() throws Exception {
		assertEquals(Short.valueOf("222"), resultSet.getObject(5));
	}

	@Test
	public void getObjectByIndexHandlesIntValues() throws Exception {
		assertEquals(Integer.valueOf(3333), resultSet.getObject(6));
	}

	@Test
	public void getObjectByIndexHandlesLongValues() throws Exception {
		assertEquals(Long.valueOf(1234567), resultSet.getObject(7));
	}

	@Test
	public void getObjectByIndexHandlesFloatValues() throws Exception {
		assertEquals(Float.valueOf("1.3"), resultSet.getObject(8));
	}

	@Test
	public void getObjectByIndexHandlesDoubleValues() throws Exception {
		assertEquals(Double.valueOf("0.2"), resultSet.getObject(9));
	}

	@Test
	public void getObjectByIndexHandlesBigDecimalValues() throws Exception {
		assertEquals(new BigDecimal("11.23"), resultSet.getObject(10));
	}

	@Test
	public void getObjectByIndexHandlesDateValues() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getObject(11)));
	}

	@Test
	public void getObjectByIndexHandlesTimeValues() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getObject(12)));
	}

	@Test
	public void getObjectByIndexHandlesTimestampValues() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getObject(13)));
	}

	@Test
	public void getObjectByIndexReturnNullForUnhandledTypes() throws Exception {
		assertNull(resultSet.getObject(14));
	}

	@Test
	public void getObjectByIndexReturnProvidedValueIfTypeInformationIsNotPresent() throws Exception {
		assertEquals("blutti", resultSet.getObject(15));
	}

	@Test
	public void getObjectByNameIsHandled() throws Exception {
		assertEquals("2011-04-01 19:28:43.456", dateFormat.format(resultSet.getObject("timestamp")));
	}

}
