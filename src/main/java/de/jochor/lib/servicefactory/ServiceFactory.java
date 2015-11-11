package de.jochor.lib.servicefactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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

	public static final String PROPERTIES_BASE = "jochor.servicefactory.";

	public static final String SILENT_MODE = PROPERTIES_BASE + "silence";

	private static boolean silence;

	static {
		String silenceString = System.getProperty(SILENT_MODE);
		silence = Boolean.valueOf(silenceString).booleanValue();
	}

	protected static <S> S create(String serviceBinderName) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		LinkedHashSet<URL> possibleBinders = findPossibleBinders(serviceBinderName, classLoader);

		String fqClassName = toFqClassName(serviceBinderName);

		String implName = null;
		if (possibleBinders.size() > 1) {
			String propertyName = PROPERTIES_BASE + fqClassName;
			implName = System.getProperty(propertyName);
		}

		try {
			Class<?> serviceBinderClass;
			if (implName == null) {
				reportMultipleBinders(possibleBinders);
				serviceBinderClass = loadFirstBinder(fqClassName, classLoader);
			} else {
				serviceBinderClass = loadSpecificBinder(fqClassName,serviceBinderName, possibleBinders, implName);
				if (serviceBinderClass == null) {
					serviceBinderClass = loadFirstBinder(fqClassName, classLoader);
				}
			}

			Method createMethod = serviceBinderClass.getMethod("create");

			@SuppressWarnings("unchecked")
			S service = (S) createMethod.invoke(null);

			reportActuallyUsedBinder(possibleBinders);

			return service;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | MalformedURLException e) {
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

	protected static Class<?> loadFirstBinder(String fqClassName, ClassLoader classLoader) throws ClassNotFoundException {
		Class<?> serviceBinderClass = classLoader.loadClass(fqClassName);
		return serviceBinderClass;
	}

	private static Class<?> loadSpecificBinder(String fqClassName, String serviceBinderName, LinkedHashSet<URL> possibleBinders, String implName) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
		int charsToCut = serviceBinderName.length();
		Iterator<URL> iter = possibleBinders.iterator();
		while (iter.hasNext()) {
			URL binderURL = iter.next();
			
			String urlString = binderURL.toString();
			URL rootURL = new URL(urlString.substring(0,urlString.length() - charsToCut));
			ClassLoader classLoader = new MyClassLoader(rootURL);
			Class<?> binderClass = loadFirstBinder(fqClassName, classLoader);

			Method getImplNameMethod = binderClass.getDeclaredMethod("getImplName");
			String actualImplName = (String) getImplNameMethod.invoke(null);
			if (implName.equals(actualImplName)) {
				return binderClass;
			}
		}

		return null;
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
