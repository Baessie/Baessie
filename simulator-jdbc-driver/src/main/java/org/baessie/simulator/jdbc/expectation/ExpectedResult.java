package org.baessie.simulator.jdbc.expectation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@SuppressWarnings("serial")
public class ExpectedResult implements Serializable {
	private final Row header;
	private final List<Row> rows;
	private final AtomicInteger callCount = new AtomicInteger();

	public ExpectedResult(final Row header, final List<Row> rows) {
		this.header = header;
		this.rows = rows;
	}

	public List<Row> getRows() {
		return Collections.unmodifiableList(rows);
	}

	public List<Row> getHeader() {
		return rows;
	}

	/**
	 * Returns the index (1-based) of the column with the given label.
	 * 
	 * @param columnLabel
	 *            the label to find a column for.
	 * @return the index of the column with the given label.
	 */
	public int getColumnIndexForColumnLabel(final String columnLabel) {
		int index = 0;
		for (final String column : header) {
			index += 1;
			if (column.equalsIgnoreCase(columnLabel)) {
				break;
			}
		}
		return index;
	}

	/**
	 * Returns the label for the column with the given index (1-based).
	 * 
	 * @param column
	 *            index of the column to get the label for.
	 * @return the label for the column with the given index.
	 */
	public String getColumnLabelForColumnIndex(final int column) {
		return header.get(column - 1);
	}

	public void increaseCallCount() {
		this.callCount.incrementAndGet();
	}

	public int getCallCount() {
		return this.callCount.get();
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
