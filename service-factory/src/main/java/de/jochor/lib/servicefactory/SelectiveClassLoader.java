package de.jochor.lib.servicefactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import lombok.Getter;

/**
 * {@link ClassLoader} that primarily tries to load a class from a specific URL. Only if this fails, it uses the normal
 * {@link ClassLoader} hierarchy.
 *
 * <p>
 * <b>Started:</b> 2015-11-11
 * </p>
 *
 * @author jochen.hormes
 *
 */
public class SelectiveClassLoader extends ClassLoader {

	@Getter
	private final URL baseURL;

	private final HashSet<String> selected = new HashSet<>();

	/**
	 * Creates a {@link SelectiveClassLoader} for a base {@link URL} and exactly one class to be loaded from there.
	 * Every other class will be loaded via the normal {@link ClassLoader}.
	 *
	 * @param baseURL
	 *            Alternative class source
	 * @param selected
	 *            Class to be loaded from there
	 */
	public SelectiveClassLoader(URL baseURL, String selected) {
		this.baseURL = baseURL;
		this.selected.add(selected);
	}

	/**
	 * Creates a {@link SelectiveClassLoader} for a base {@link URL} and some classes to be loaded from there. Every
	 * other class will be loaded via the normal {@link ClassLoader}.
	 *
	 * @param baseURL
	 *            Alternative class source
	 * @param selected
	 *            Classes to be loaded from there
	 */
	public SelectiveClassLoader(URL baseURL, String... selected) {
		this.baseURL = baseURL;
		this.selected.addAll(Arrays.asList(selected));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> loadedClass = null;

		if (selected.contains(name)) {
			loadedClass = tryUnderBaseURL(name);
		}
		if (loadedClass == null) {
			loadedClass = super.loadClass(name);
		}

		return loadedClass;
	}

	private Class<?> tryUnderBaseURL(String name) {
		String resName = name.replace('.', '/').concat(".class");

		try (InputStream resourceStream = new URL(baseURL, resName).openStream()) {
			int off = 0;
			int len = 1024;
			byte[] b = new byte[0];
			boolean allRead = false;
			while (!allRead) {
				b = Arrays.copyOf(b, len);
				int toRead = len - off;
				int read = resourceStream.read(b, off, toRead);
				allRead = read < toRead;
				off += read;
				len *= 2;
			}

			Class<?> loadedClass = defineClass(name, b, 0, off);
			resolveClass(loadedClass);

			return loadedClass;
		} catch (IOException e) {
			// Cannot load the class from the baseURL, just let the normal ClassLoader handle it.
			return null;
		}
	}

}
