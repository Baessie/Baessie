package org.baessie.simulator.jdbc.expectation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@SuppressWarnings("serial")
public class Row extends ArrayList<String> implements Serializable {

	public Row(final String... columns) {
		super(list(columns));
	}

	public Row(final List<String> columns) {
		super(columns);
	}

	/**
	 * Returns the String value at the column with the given index (1-based).
	 * 
	 * @param columnIndex
	 *            the column to get the value for.
	 * @return the String value at the column with the given index.
	 */
	public String getStringAt(final int columnIndex) {
		Logger.log("getStringAt(%s)", columnIndex);
		Logger.log("<Columns: %s>", this);
		return get(columnIndex - 1);
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

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
