package org.baessie.simulator.jdbc.expectation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ExpectationManagerTest {
	private transient static final String TRIGGERQUERY = "FNORD glorp MUMPH";
	private final ExpectedResult expectedResult = createExpectedResult();
	private final transient Expectation expectation = new Expectation(TRIGGERQUERY, expectedResult);

	private ExpectedResult createExpectedResult() {
		final Row header = new Row("anka", "pingvin", "bärs");
		final List<Row> rows = new ArrayList<Row>();
		rows.add(new Row("dödlig", "15", "många"));
		rows.add(new Row("grundlig", "1", "en"));
		final ExpectedResult expectedResult = new ExpectedResult(header, rows);
		return expectedResult;
	}

	@Test(expected = IllegalArgumentException.class)
	public void getCallCountForExceptionWithNull() {
		ExpectationManager.resetExpectations();
		ExpectationManager.getCallCountFor(null);
	}

	@Test(expected = IllegalStateException.class)
	public void getCallCountForExceptionWithNonRegisteredQuery() {
		ExpectationManager.getCallCountFor("BAH");
	}

	@Test(expected = IllegalStateException.class)
	public void getCallCountForZeroWithRegisteredQueryThatHasNotBeenCalled() {
		ExpectationManager.resetExpectations();
		ExpectationManager.addExpectation(expectation);
		assertEquals("should get a callcount of zero", 0, ExpectationManager.getCallCountFor(expectation.getTriggerQuery()));
	}

	@Test
	public void getCallCountForOneWithRegisteredQueryThatHasBeenCalledOnce() {
		ExpectationManager.resetExpectations();
		ExpectationManager.addExpectation(expectation);
		ExpectationManager.getExpectedResultFor(expectation.getTriggerQuery());
		assertEquals("should get a callcount of one", 1, ExpectationManager.getCallCountFor(expectation.getTriggerQuery()));
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForExceptionWithNull() {
		ExpectationManager.resetExpectations();
		assertNull("should not get any result with null as query", ExpectationManager.getExpectedResultFor(null));
	}

	@Test(expected = IllegalStateException.class)
	public void getExpectedResultForExceptionWithNonRegisteredQuery() {
		ExpectationManager.resetExpectations();
		assertNull("should not get any result with nothing to match against", ExpectationManager.getExpectedResultFor("BAH"));
	}

	@Test
	public void getExpectedResultForMatchingQuery() {
		ExpectationManager.resetExpectations();
		ExpectationManager.addExpectation(expectation);
		ExpectedResult expectedResultFor = ExpectationManager.getExpectedResultFor(expectation.getTriggerQuery());
		assertEquals("callcount should have increased by one", expectation.getExpectedResult().getCallCount() + 1, expectedResultFor.getCallCount());
		ExpectedResult result = expectation.getExpectedResult();
		// Can't do equals since AtomicInteger does not handle equal
		assertEquals("the inserted and expected result should have matching header", result.getHeader(), expectedResultFor.getHeader());
		assertEquals("the inserted and expected result should have matching rows", result.getRows(), expectedResultFor.getRows());
	}

}
