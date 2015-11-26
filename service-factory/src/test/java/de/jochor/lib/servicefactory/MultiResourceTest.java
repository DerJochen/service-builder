package de.jochor.lib.servicefactory;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.jochor.test.TestMultiService;
import de.jochor.test2.TestMultiService2;

/**
 * Test for the behavior of the {@link ServiceFactory} when finding the same static binder in multiple sources.
 *
 * <p>
 * <b>Started:</b> 2015-11-12
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class MultiResourceTest {

	private static final String BINDER_NAME = "de/jochor/test/TestMultiServiceBinder.class";

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
		Assert.assertEquals(2, possibleBinders.size());

		Iterator<URI> iter = possibleBinders.iterator();
		URI uri = iter.next();
		Assert.assertNotNull(uri);
		Assert.assertEquals(BINDER_FILE.toURI(), uri);

		uri = iter.next();
		Assert.assertNotNull(uri);
		Assert.assertTrue(uri.toString().endsWith(BINDER_NAME));
		Assert.assertNotEquals(BINDER_FILE.toURI(), uri);
	}

	@Test
	public void testCreate_default() throws Throwable {
		Object service = ServiceFactory.create(BINDER_NAME);
		Assert.assertNotNull(service);
		Assert.assertTrue(service instanceof TestMultiService);
	}

	@Test
	public void testCreate_service1() throws Throwable {
		System.setProperty("jochor.servicefactory.de.jochor.test.TestMultiServiceBinder", "de.jochor.test.TestMultiService");

		Object service = ServiceFactory.create(BINDER_NAME);
		Assert.assertNotNull(service);
		Assert.assertTrue(service instanceof TestMultiService);
	}

	@Test
	public void testCreate_service2() throws Throwable {
		System.setProperty("jochor.servicefactory.de.jochor.test.TestMultiServiceBinder", "de.jochor.test2.TestMultiService2");

		Object service = ServiceFactory.create(BINDER_NAME);
		Assert.assertNotNull(service);
		Assert.assertTrue(service instanceof TestMultiService2);
	}

	@Test
	public void testCreate_serviceNotFound() throws Throwable {
		System.setProperty("jochor.servicefactory.de.jochor.test.TestMultiServiceBinder", "de.jochor.test2.TestMultiService3");

		Object service = ServiceFactory.create(BINDER_NAME);
		Assert.assertNotNull(service);
		Assert.assertTrue(service instanceof TestMultiService);
	}

}