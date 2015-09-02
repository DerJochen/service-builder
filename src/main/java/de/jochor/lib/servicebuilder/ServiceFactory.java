package de.jochor.lib.servicebuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;

/**
 *
 * @author Jochen Hormes
 * @start 2015-08-27
 *
 */
public class ServiceFactory {

	protected static <S> S create(String serviceBinderName) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		LinkedHashSet<URL> possibleBinders = findPossibleBinders(serviceBinderName, classLoader);

		reportMultipleBinders(possibleBinders);
		try {
			String fqClassName = toFqClassName(serviceBinderName);

			Class<?> serviceBinderClass = classLoader.loadClass(fqClassName);
			Method createMethod = serviceBinderClass.getMethod("create");

			@SuppressWarnings("unchecked")
			S service = (S) createMethod.invoke(null);

			return service;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException("Unable to load " + serviceBinderName + " implementation", e);
		}
	}

	protected static LinkedHashSet<URL> findPossibleBinders(String serviceBinderName, ClassLoader classLoader) {
		try {
			Enumeration<URL> resources = classLoader.getResources(serviceBinderName);

			LinkedHashSet<URL> resourceSet = new LinkedHashSet<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				resourceSet.add(resource);
			}

			return resourceSet;
		} catch (IOException e) {
			throw new RuntimeException("Unable to load " + serviceBinderName + " implementation", e);
		}
	}

	private static String toFqClassName(String serviceBinderName) {
		String fqClassName = serviceBinderName.substring(0, serviceBinderName.lastIndexOf('.'));
		fqClassName = fqClassName.replace('/', '.');
		return fqClassName;
	}

	private static void reportMultipleBinders(LinkedHashSet<URL> resourceSet)
	{
		if (resourceSet.size() <= 1)
		{
			return;
		}

		System.out.println("Multiple static binders found:");
		Iterator<URL> iter = resourceSet.iterator();
		while (iter.hasNext())
		{
			URL url = iter.next();
			System.out.println(url.toString());
		}
	}

}
