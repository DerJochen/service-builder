package de.jochor.lib.servicefactory;

import java.net.URL;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.jochor.test.TestService;

/**
 * Test for the {@link SelectiveClassLoader}.
 *
 * <p>
 * <b>Started:</b> 2015-11-26
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public class SelectiveClassLoaderTest {

	private SelectiveClassLoader classLoader;

	private URL baseURL1;

	private String fqClassName1 = "de.jochor.test.TestService";

	private String fqClassName2 = "de.jochor.lib.servicefactory.ServiceFactory";

	@Before
	public void setUp() throws Exception {
		baseURL1 = Paths.get("target/test-classes").toAbsolutePath().toUri().toURL();
		classLoader = new SelectiveClassLoader(baseURL1, fqClassName1, fqClassName2);
	}

	@Test
	public void testLoadClass_fromBaseURL() throws Exception {
		Class<?> loadedClass = classLoader.loadClass(fqClassName1);
		Assert.assertEquals(TestService.class.getName(), loadedClass.getName());
		Assert.assertNotSame(TestService.class, loadedClass);
	}

	@Test
	public void testLoadClass_notFromBaseURL() throws Exception {
		Class<?> loadedClass = classLoader.loadClass(fqClassName2);
		Assert.assertEquals(ServiceFactory.class.getName(), loadedClass.getName());
		Assert.assertSame(ServiceFactory.class, loadedClass);
	}

	@Test
	public void testLoadClass_biggerClass() throws Exception {
		URL baseURL2 = Paths.get("target/classes").toAbsolutePath().toUri().toURL();
		SelectiveClassLoader classLoader = new SelectiveClassLoader(baseURL2, fqClassName2);
		Class<?> loadedClass = classLoader.loadClass(fqClassName2);
		Assert.assertEquals(ServiceFactory.class.getName(), loadedClass.getName());
		Assert.assertNotSame(ServiceFactory.class, loadedClass);
	}

	@Test(expected = ClassNotFoundException.class)
	public void testLoadClass_notThere() throws Exception {
		URL baseURL2 = Paths.get("target/classes").toAbsolutePath().toUri().toURL();
		SelectiveClassLoader classLoader = new SelectiveClassLoader(baseURL2, "NotThere");

		Class<?> loadedClass = classLoader.loadClass("NotThere");

		Assert.assertEquals(ServiceFactory.class.getName(), loadedClass.getName());
		Assert.assertNotSame(ServiceFactory.class, loadedClass);
	}

	// A bit useless, but it covers more code paths in the generated try-with-resource code
	@Test(expected = ClassFormatError.class)
	public void testLoadClass_notAClass() throws ClassFormatError, ClassNotFoundException {
		SelectiveClassLoader classLoader = new SelectiveClassLoader(baseURL1, "NotAClass");
		classLoader.loadClass("NotAClass");
	}

	@Test
	public void testGetBaseURL() {
		Assert.assertEquals(baseURL1, classLoader.getBaseURL());
	}

}
