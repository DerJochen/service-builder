package de.jochor.lib.servicebuilder;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.Computer;
import org.junit.runners.Suite;

import de.jochor.test.sub.TestResource;

public class ClasspathUtilTest {

	@Test
	public void testFolder() throws Throwable {
		String packageName = "de.jochor.test.sub";
		Class<Closeable> parentType = Closeable.class;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<? extends Closeable> expected = TestResource.class;

		checkScanner(packageName, parentType, classLoader, expected);
	}

	@Test
	public void testFolderWithTree() throws Throwable {
		String packageName = "de.jochor.test";
		Class<Closeable> parentType = Closeable.class;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<? extends Closeable> expected = TestResource.class;

		checkScanner(packageName, parentType, classLoader, expected);
	}

	@Test
	public void testJar() throws Throwable {
		String packageName = "org.junit.experimental";
		Class<Computer> parentType = Computer.class;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<? extends Computer> expected = ParallelComputer.class;

		checkScanner(packageName, parentType, classLoader, expected);
	}

	@Test
	public void testJarWithTree() throws Throwable {
		String packageName = "org.junit.experimental.runners";
		Class<Suite> parentType = Suite.class;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<? extends Suite> expected = Enclosed.class;

		checkScanner(packageName, parentType, classLoader, expected);
	}

	public <T> void checkScanner(String packageName, Class<T> parentType, ClassLoader classLoader, Class<? extends T> expected)
			throws IOException, URISyntaxException, ClassNotFoundException {
		ArrayList<Class<? extends T>> implementations = ClasspathUtil.findImplementations(packageName, parentType, classLoader);

		Assert.assertNotNull(implementations);
		Assert.assertEquals(1, implementations.size());
		Assert.assertEquals(expected, implementations.get(0));
	}

}