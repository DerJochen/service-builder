package de.jochor.lib.servicebuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author Jochen Hormes
 * @start 2015-08-27
 *
 */
public class ClasspathUtil {

	/**
	 * Found at
	 * http://stackoverflow.com/questions/1456930/how-do-i-read-all-classes-from-a-java-package-in-the-classpath/7461653
	 * #7461653 and adapted to better fit my problem. Thanks!
	 *
	 * @param packageName
	 *            Package to start the search in
	 * @param toLookFor
	 *            Class or interface the searched classes must be extending or implementing
	 * @param classLoader
	 *            ClassLoader to use
	 * @return All classes below the package inheriting from the given class
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ClassNotFoundException
	 */
	public static <T> ArrayList<Class<? extends T>> findImplementations(String packageName, Class<T> parentType, ClassLoader classLoader)
			throws IOException, URISyntaxException, ClassNotFoundException {
		ArrayList<String> names = new ArrayList<String>();

		packageName = packageName.replace(".", "/");
		URL packageURL = classLoader.getResource(packageName);

		// TODO filter by 'parentType'
		// TODO add tree search or recursion to descend in child packages
		if (packageURL.getProtocol().equals("jar")) {
			String jarFileName;
			Enumeration<JarEntry> jarEntries;
			String entryName;

			// build jar file name, then loop through zipped entries
			jarFileName = URLDecoder.decode(packageURL.getFile(), StandardCharsets.UTF_8.name());
			jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
			try (JarFile jf = new JarFile(jarFileName)) {
				jarEntries = jf.entries();
				while (jarEntries.hasMoreElements()) {
					entryName = jarEntries.nextElement().getName();
					if (entryName.startsWith(packageName) && entryName.length() > packageName.length() + 5) {
						entryName = entryName.substring(packageName.length(), entryName.lastIndexOf('.'));
						names.add(entryName);
					}
				}
			}
		} else {
			URI uri = new URI(packageURL.toString());
			File folder = new File(uri.getPath());
			// won't work with path which contains blank (%20)
			// File folder = new File(packageURL.getFile());
			File[] contenuti = folder.listFiles();
			String entryName;
			for (File actual : contenuti) {
				entryName = actual.getName();
				int lastDot = entryName.lastIndexOf('.');
				entryName = entryName.substring(0, lastDot);
				names.add(entryName);
			}
		}

		ArrayList<Class<? extends T>> implementations = new ArrayList<>();
		for (String name : names) {
			Class<?> potentialImplementation = classLoader.loadClass(name);
			if (parentType.isAssignableFrom(potentialImplementation)) {
				Class<? extends T> implementation = potentialImplementation.asSubclass(parentType);
				implementations.add(implementation);
			}
		}

		return implementations;
	}

}
