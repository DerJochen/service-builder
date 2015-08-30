package de.jochor.lib.servicebuilder;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.Computer;
import org.junit.runners.Suite;

public class ClasspathUtilTest {

	@Test
	public void testJar() throws Throwable {
		String packageName = "org.junit.experimental";
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		ArrayList<Class<? extends Computer>> implementations = ClasspathUtil.findImplementations(packageName, Computer.class, classLoader);

		Assert.assertNotNull(implementations);
		Assert.assertEquals(1, implementations.size());
		Assert.assertEquals(ParallelComputer.class, implementations.get(0));
	}

	@Test
	public void testJarWithTree() throws Throwable {
		String packageName = "org.junit.experimental.runners";
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		ArrayList<Class<? extends Suite>> implementations = ClasspathUtil.findImplementations(packageName, Suite.class, classLoader);

		Assert.assertNotNull(implementations);
		Assert.assertEquals(1, implementations.size());
		Assert.assertEquals(Enclosed.class, implementations.get(0));
	}

}