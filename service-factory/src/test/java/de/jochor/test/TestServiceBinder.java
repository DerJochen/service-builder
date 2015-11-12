package de.jochor.test;

public class TestServiceBinder {

	public static TestService create() {
		return new TestService();
	}
	
	public static String getImplName() {
		return TestService.class.getName();
	}

}
