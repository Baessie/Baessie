package org.baessie.simulator.util;

public class BackReferenceValue {

	private final String id;
	private final String value;

	public BackReferenceValue(final String id, final String value) {
		super();
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

}
