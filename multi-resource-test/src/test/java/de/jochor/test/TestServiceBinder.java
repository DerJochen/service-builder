package de.jochor.test;

import de.jochor.test2.TestService2;

/**
 *
 * <p>
 * <b>Started:</b> 2015-11-12
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class TestServiceBinder {

	public static TestService2 create() {
		return new TestService2();
	}

	public static String getImplName() {
		return TestService2.class.getName();
	}

}
