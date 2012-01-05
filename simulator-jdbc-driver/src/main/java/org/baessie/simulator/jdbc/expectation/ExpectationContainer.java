package org.baessie.simulator.jdbc.expectation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@SuppressWarnings("serial")
public class ExpectationContainer implements Serializable {
	private final List<Expectation> nextExpectations = new ArrayList<Expectation>();
	private final List<Expectation> calledExpectations = new ArrayList<Expectation>();

	/**
	 * Adds an expectation to the container.
	 * 
	 * @param expectation
	 *            the expectation to add.
	 */
	public void addExpectation(final Expectation expectation) {
		nextExpectations.add(expectation);
	}

	/**
	 * Returns next expected result from an expectation in the container.
	 * 
	 * @param triggerQuery
	 *            the query to get an expected result for.
	 * @return the expected result for the given query, or null if no such
	 *         expectation exists.
	 */
	public ExpectedResult getNextExpectedResult(final String triggerQuery) {
		ExpectedResult result = null;
		for (Expectation nextExpectation : nextExpectations) {
			if (doesStringMatch(nextExpectation.getTriggerQuery(), triggerQuery)) {
				result = nextExpectation.getExpectedResult();
				nextExpectations.remove(nextExpectation);
				calledExpectations.add(nextExpectation);
				return result;
			}
		}
		throw new IllegalStateException("Next expected result does not match query");
	}

	/**
	 * Returns next called expected result from an expectation in the container.
	 * 
	 * @param triggerQuery
	 *            the query to get an expected result for.
	 * @return the expected result for the given query, or null if no such
	 *         expectation exists.
	 */
	public ExpectedResult getNextCalledExpectedResult(final String triggerQuery) {
		ExpectedResult result = null;
		for (Expectation calledExpectation : calledExpectations) {
			if (doesStringMatch(calledExpectation.getTriggerQuery(), triggerQuery)) {
				result = calledExpectation.getExpectedResult();
				nextExpectations.remove(calledExpectation);
				return result;
			}
		}
		throw new IllegalStateException("Next called result does not match query");
	}

	/**
	 * Checks if the instance matches the defined string, allowing for special
	 * cases in the defined string such as wildcards.
	 * 
	 * @param defined
	 *            the defined string, may contain wildcards
	 * @param instance
	 *            the instance string, may not contain wildcards
	 * @return <code>true</code> if the instance matches the defined string,
	 *         otherwise <code>false</code>
	 */
	public boolean doesStringMatch(final String defined, final String instance) {
		Boolean match = null;
		if (defined == null && instance == null) {
			match = Boolean.TRUE;
		} else if (defined != null && instance == null) {
			match = Boolean.FALSE;
		} else if (defined == null && instance != null) {
			match = Boolean.FALSE;
		} else if (defined.equals(instance)) {
			match = Boolean.TRUE;
		}
		if (match == null) {
			match = Boolean.valueOf(containsWildcardDefinitions(defined) && doesWildCardMatch(defined, instance));
		}
		return match.booleanValue();

	}

	private boolean doesWildCardMatch(final String defined, final String actual) {
		final String regExpString = Pattern.quote(defined).replaceAll("\\*", "\\\\E.*\\\\Q");
		return Pattern.matches(regExpString, actual);
	}

	private boolean containsWildcardDefinitions(final String defined) {
		return defined.contains("*");
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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

}
