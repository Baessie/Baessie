package org.baessie.simulator.jdbc.expectation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ExpectationManager {
	private static final String FILENAME = "/tmp/expectations.serialized";

	private static ExpectationContainer loadExpectations() {
		Logger.log("ExpectationManager.loadExpectations()");
		try {
			final ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));
			final ExpectationContainer expectations = (ExpectationContainer) in.readObject();
			in.close();
			Logger.log("<Expectations found: %s>", expectations);
			return expectations;
		} catch (final IOException e) {
			Logger.log("<Exception: %s>", e);
			Logger.log("<No expectations found, returning empty container>");
			return new ExpectationContainer();
		} catch (final ClassNotFoundException e) {
			Logger.log("<Exception: %s>", e);
			throw new ExpectationCommunicationException(e);
		}
	}

	private static void saveExpectations(final ExpectationContainer expectations) {
		Logger.log("ExpectationManager.saveExpectations()");
		Logger.log("<Exception to save: %s>", expectations);
		try {
			final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILENAME));
			out.writeObject(expectations);
			out.close();
		} catch (final IOException e) {
			Logger.log("<Exception: %s>", e);
			throw new ExpectationCommunicationException(e);
		}
	}

	/**
	 * Clears all expectations.
	 */
	public static void resetExpectations() {
		Logger.log("ExpectationManager.resetExpectations()");
		new File(FILENAME).delete();
	}

	/**
	 * Adds the given expectation in a way that it is visible to all driver
	 * instances.
	 * 
	 * @param expectation
	 *            the expectation to add.
	 */
	public static void addExpectation(final Expectation expectation) {
		Logger.log("ExpectationManager.addExpectation(%s)", expectation);
		final ExpectationContainer expectations = loadExpectations();
		expectations.addExpectation(expectation);
		saveExpectations(expectations);
	}

	/**
	 * Gets the expected result for the given query.
	 * 
	 * @param query
	 *            the query to find a result for.
	 * @return the expected result for the given query, or null if no such
	 *         expectation exists.
	 */
	public static ExpectedResult getExpectedResultFor(final String query) {
		Logger.log("ExpectationManager.getExpectedResultFor(%s)", query);
		final ExpectationContainer expectations = loadExpectations();
		Logger.log("<Expectations: %s>", expectations);
		final ExpectedResult result = expectations.getNextExpectedResult(query);
		result.increaseCallCount();
		saveExpectations(expectations);
		Logger.log("<Expected result: %s>", result);
		return result;
	}

	/**
	 * Retrieves the current call count for the given query.
	 * 
	 * @param query
	 *            the query to find the call count for.
	 * @return the call count of the given query.
	 */
	public static int getCallCountFor(final String query) {
		Logger.log("ExpectationManager.getExpectedResultFor(%s)", query);
		if (query == null) {
			throw new IllegalArgumentException("query must not be null");
		}
		final ExpectationContainer expectations = loadExpectations();
		Logger.log("<Expectations: %s>", expectations);
		final ExpectedResult result = expectations.getNextCalledExpectedResult(query);
		saveExpectations(expectations);
		Logger.log("<Callcount: %s>", result.getCallCount());
		return result.getCallCount();
	}

}
