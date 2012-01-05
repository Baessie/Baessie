package org.baessie.simulator.jdbc.expectation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Expectation implements Serializable {
	private static final int TRIGGER_QUERY_INDEX = 1;
	private static final int HEADER_ROW_INDEX = 2;
	private static final int FIRST_DATA_ROW_INDEX = 3;
	private static final long serialVersionUID = 10;
	private final String triggerQuery;
	private final ExpectedResult expectedResult;

	public Expectation(final String triggerQuery, final ExpectedResult expectedResult) {
		this.triggerQuery = triggerQuery;
		this.expectedResult = expectedResult;
	}

	public String getTriggerQuery() {
		return triggerQuery;
	}

	public ExpectedResult getExpectedResult() {
		return expectedResult;
	}

	/**
	 * Returns whether the given query is an expectation ("setup") query.
	 * 
	 * @param query
	 *            the query to test.
	 * @return true if the given query is an expectation query, otherwise false.
	 */
	public static boolean isExpectationQuery(final String query) {
		final boolean result = query.startsWith("SETUP:");
		Logger.log("Expectation.isExpectationQuery(%s) = %s", query, result);
		return result;
	}

	/**
	 * Returns an expectation parsed from the given expectation query. To be
	 * valid input for this method, the query must produce "true" when given to
	 * the {@link #isExpectationQuery(String)} method.
	 * 
	 * @param query
	 *            the query to parse.
	 * @return an expectation representing the given expectation query.
	 */
	public static Expectation fromExpectationQuery(final String query) {
		final String[] parts = query.split("\n");
		final String triggerQuery = parts[TRIGGER_QUERY_INDEX];
		final String headerString;
		if (parts.length > HEADER_ROW_INDEX) {
			headerString = parts[HEADER_ROW_INDEX];
		} else {
			headerString = "";
		}
		final List<Row> rows = new ArrayList<Row>();
		final Row header = new Row(splitValueString(headerString));
		if (parts.length > FIRST_DATA_ROW_INDEX) {
			for (int rowIndex = FIRST_DATA_ROW_INDEX; rowIndex < parts.length; rowIndex += 1) {
				final String rowString = parts[rowIndex];
				final Row row = new Row(splitValueString(rowString));
				rows.add(row);
			}
		}
		final Expectation result = new Expectation(triggerQuery, new ExpectedResult(header, rows));
		Logger.log("Expectation.fromExpectationQuery(%s) = %s", query, result);
		return result;
	}

	private static String[] splitValueString(final String rowString) {
		String row = rowString.replaceAll("^'|'$", "");
		String[] strings = row.split("','");
		return strings;
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
