package org.baessie.simulator.jdbc.logging;

import java.util.Arrays;
import java.util.List;

public class ResultSetRowMock {
	private final List<Object> values;

	public ResultSetRowMock(final Object... values) {
		this.values = Arrays.asList(values);
	}

	public Object get(int columnIndex) {
		return values.get(columnIndex);
	}

}
