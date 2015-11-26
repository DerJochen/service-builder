package de.jochor.test;

public class TestMultiServiceBinder {

	public static TestMultiService create() {
		return new TestMultiService();
	}

	public static String getImplName() {
		return TestMultiService.class.getName();
	}

}
