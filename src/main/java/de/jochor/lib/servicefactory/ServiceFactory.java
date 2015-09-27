package de.jochor.lib.servicefactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Base class for service factories. Its job is to find a service binder class that is referenced by name and to call
 * the binders create() method.
 *
 * <p>
 * <b>Started:</b> 2015-08-27
 * </p>
 *
 * @author Jochen Hormes
 *
 */
public abstract class ServiceFactory {

	public static final String SILENT_MODE = "jochor.servicefactory.silence";

	private static boolean silence;

	static {
		String silenceString = System.getProperty(SILENT_MODE);
		silence = Boolean.valueOf(silenceString).booleanValue();
	}

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

			reportActuallyUsedBinder(possibleBinders);

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

	private static void reportMultipleBinders(LinkedHashSet<URL> resourceSet) {
		if (resourceSet.size() <= 1) {
			return;
		}

		syso("Multiple static binders found:");
		Iterator<URL> iter = resourceSet.iterator();
		while (iter.hasNext()) {
			URL url = iter.next();
			syso(url.toString());
		}
	}

	private static void reportActuallyUsedBinder(LinkedHashSet<URL> possibleBinders) {
		URL url = possibleBinders.iterator().next();
		syso("Used static binder: " + url.toString());
	}

	private static void syso(String message) {
		if (silence) {
			return;
		}
		System.out.println(message);
	}

}
