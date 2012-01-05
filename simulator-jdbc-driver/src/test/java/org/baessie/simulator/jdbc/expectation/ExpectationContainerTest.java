package org.baessie.simulator.jdbc.expectation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ExpectationContainerTest {
	private transient final ExpectationContainer emptyContainer = new ExpectationContainer();
	private transient final ExpectationContainer filledContainer = new ExpectationContainer();
	private transient static final String TRIGGERQUERY = "FNORD glorp MUMPH";
	private transient Expectation expectation;
	private transient Expectation wildcardExpectation;

	@Before
	public void setUp() {
		final Row header = new Row("anka", "pingvin", "bärs");
		final List<Row> rows = new ArrayList<Row>();
		rows.add(new Row("dödlig", "15", "många"));
		rows.add(new Row("grundlig", "1", "en"));
		final ExpectedResult expectedResult = new ExpectedResult(header, rows);
		expectation = new Expectation(TRIGGERQUERY, expectedResult);
		wildcardExpectation = new Expectation(TRIGGERQUERY.replace("glorp", "*"), expectedResult);
		assertFalse("wildcard expectation and regular expectation should NOT be equal!", expectation.equals(wildcardExpectation));
		filledContainer.addExpectation(expectation);
		filledContainer.addExpectation(wildcardExpectation);
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForWithNullInEmptyNoMatch() {
		assertNull("null input should give no match", emptyContainer.getNextExpectedResult(null));
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForWithNullInFilledNoMatch() {
		assertNull("null input should give no match", filledContainer.getNextExpectedResult(null));
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForWithEmptyStringInEmptyNoMatch() {
		assertNull("empty input should give no match", emptyContainer.getNextExpectedResult(""));
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForWithEmptyStringInFilledNoMatch() {
		assertNull("empty input should give no match", filledContainer.getNextExpectedResult(""));
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForWithNonMatchingStringInEmptyNoMatch() {
		assertNull("non-matching input should give no match", emptyContainer.getNextExpectedResult("nalle puh"));
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForWithNonMatchingStringInFilledNoMatch() {
		assertNull("non-matching input should give no match", filledContainer.getNextExpectedResult("nalle puh"));
	}

	@Test
	public void getExpectedResultForWithMatchingStringInFilledMatch() {
		assertEquals("matching input should give match", expectation.getExpectedResult(), filledContainer.getNextExpectedResult(expectation.getTriggerQuery()));
	}

	@Test
	public void doesStringMatchWithNullAndNull() {
		assertTrue("null should equal null", filledContainer.doesStringMatch(null, null));
	}

	@Test
	public void doesStringMatchSameStringWithItself() {
		final String str = "anka";
		assertTrue(String.format("%s should equal %s", str, str), filledContainer.doesStringMatch(str, str));
	}

	@Test
	public void doesStringMatchAnotherStringWithItself() {
		final String str = "anka";
		final String instance = "anka2";
		assertFalse(String.format("%s should not equal %s", str, instance), filledContainer.doesStringMatch(str, instance));
	}

	@Test
	public void doesStringMatchStringWithNull() {
		final String str = "anka";
		assertFalse(String.format("%s should not equal %s", str, null), filledContainer.doesStringMatch(str, null));
	}

	@Test
	public void doesStringMatchNullWithString() {
		final String str = "anka";
		assertFalse(String.format("%s should not equal %s", null, str), filledContainer.doesStringMatch(null, str));
	}

	@Test
	public void doesStringMatchWildcardStringWithMatchingString() {
		final String str = "anka*";
		final String instance = "anka2";
		assertTrue(String.format("%s should equal %s", str, instance), filledContainer.doesStringMatch(str, instance));
	}

	@Test
	public void doesStringMatchAdvancedWildcardStringWithMatchingString() {
		final String str = "anka*banan*gurka";
		final String instance = "ankafnorpbanangurka";
		assertTrue(String.format("%s should equal %s", str, instance), filledContainer.doesStringMatch(str, instance));
	}

	@Test
	public void doesStringMatchStringWithWildcardString() {
		final String str = "anka2";
		final String instance = "anka*";
		assertFalse(String.format("%s should not equal %s", str, instance), filledContainer.doesStringMatch(str, instance));
	}

	@Test
	public void doesStringMatchRegexpStringWithString() {
		final String str = "anka[1-2]*e";
		final String instance = "anka[1-2]beige";
		assertTrue(String.format("%s should equal %s", str, instance), filledContainer.doesStringMatch(str, instance));
	}

}
