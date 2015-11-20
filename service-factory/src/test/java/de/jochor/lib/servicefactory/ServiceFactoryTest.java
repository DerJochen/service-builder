package de.jochor.lib.servicefactory;

import java.io.File;
import java.net.URI;
import java.util.LinkedHashSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.jochor.test.TestService;

public class ServiceFactoryTest {

	private static final String BINDER_NAME = "de/jochor/test/TestServiceBinder.class";

	private static final File BINDER_FILE = new File("target/test-classes", BINDER_NAME);

	@BeforeClass
	public static void setUpBeforeClass() {
		// Switch off outputs from the service factory
		System.setProperty(ServiceFactory.SILENT_MODE, "true");
	}

	@Test
	public void testFindPossibleBinders() throws Throwable {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		LinkedHashSet<URI> possibleBinders = ServiceFactory.findPossibleBinders(BINDER_NAME, classLoader);
		Assert.assertNotNull(possibleBinders);
		Assert.assertEquals(1, possibleBinders.size());

		URI uri = possibleBinders.iterator().next();
		Assert.assertNotNull(uri);
		Assert.assertEquals(BINDER_FILE.toURI(), uri);
	}

	@Test
	public void testCreate() throws Throwable {
		Object service = ServiceFactory.create(BINDER_NAME);
		Assert.assertNotNull(service);
		Assert.assertTrue(service instanceof TestService);
	}

}