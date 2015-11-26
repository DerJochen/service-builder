package de.jochor.test;

import de.jochor.test2.TestMultiService2;

/**
 *
 * <p>
 * <b>Started:</b> 2015-11-12
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class TestMultiServiceBinder {

	public static TestMultiService2 create() {
		return new TestMultiService2();
	}

	public static String getImplName() {
		return TestMultiService2.class.getName();
	}

}
