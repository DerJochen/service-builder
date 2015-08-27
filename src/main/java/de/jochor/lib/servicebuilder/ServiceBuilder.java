package de.jochor.lib.servicebuilder;

import java.util.ArrayList;

/**
 *
 * @author Jochen Hormes
 * @start 2015-08-27
 *
 */
public class ServiceBuilder {

	protected static <T> Class<? extends T> findImplementation(Class<T> serviceType, String classNameProperty) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			String className = System.getProperty(classNameProperty);

			Class<? extends T> serviceClass = null;

			if (className != null) {
				serviceClass = classLoader.loadClass(className).asSubclass(serviceType);
			} else {
				Package mainPackage = serviceType.getPackage();
				String packageName = mainPackage.getName();

				ArrayList<Class<? extends T>> implementations = ClasspathUtil.findImplementations(packageName, serviceType, classLoader);

				if (implementations.isEmpty()) {
					throw new RuntimeException("No implementation of " + serviceType.getName() + " found in classpath");
				}

				serviceClass = implementations.get(0);
				if (implementations.size() > 1) {
					System.out.println("Multiple implementations of " + serviceType.getName() + " found, randomly chosen: " + className);
				}
			}

			return serviceClass;
		} catch (Throwable e) {
			throw new RuntimeException("Unable to load " + serviceType.getName() + " implementation", e);
		}
	}

	protected static <S> S create(Class<S> serviceClass) {
		if (serviceClass == null) {
			throw new IllegalStateException("No service class set");
		}
		try {
			S service = serviceClass.newInstance();
			return service;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
