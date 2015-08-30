package de.jochor.lib.servicebuilder;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

		String packagePathName = packageName.replace(".", "/");
		Enumeration<URL> packageURLs = classLoader.getResources(packagePathName);

		// TODO works for folders. to be on the save side, add a test.
		while (packageURLs.hasMoreElements()) {
			URL packageURL = packageURLs.nextElement();
			if (packageURL.getProtocol().equals("jar")) {
				scanArchive(packageURL, packageName, packagePathName, names);
			} else {
				scanFolders(packageURL, packageName, names);
			}
		}

		ArrayList<Class<? extends T>> implementations = new ArrayList<>();
		for (String name : names) {
			Class<?> potentialImplementation = classLoader.loadClass(name);
			if (!potentialImplementation.isInterface() && parentType.isAssignableFrom(potentialImplementation)) {
				Class<? extends T> implementation = potentialImplementation.asSubclass(parentType);
				implementations.add(implementation);
			}
		}

		return implementations;
	}

	public static void scanFolders(URL packageURL, String packageName, ArrayList<String> names) throws URISyntaxException {
		URI uri = new URI(packageURL.toString());
		File folder = new File(uri.getPath());
		scanFolder(folder, packageName, names);
	}

	public static void scanFolder(File folder, String packageName, ArrayList<String> names) {
		File[] contenuti = folder.listFiles();
		String entryName;
		for (File actual : contenuti) {
			if (actual.isFile()) {
				entryName = actual.getName();
				int lastDot = entryName.lastIndexOf('.');
				entryName = entryName.substring(0, lastDot);
				String fqClassName = packageName + "." + entryName;
				names.add(fqClassName);
			} else if (actual.isDirectory()) {
				scanFolder(actual, packageName + "." + actual.getName(), names);
			}
		}
	}

	public static void scanArchive(URL packageURL, String packageName, String packagePathName, ArrayList<String> names)
			throws UnsupportedEncodingException, IOException {
		String jarFileName;
		Enumeration<JarEntry> jarEntries;
		String entryName;
		String packagePathNamePlusSlash = packagePathName + "/";

		// build jar file name, then loop through zipped entries
		jarFileName = URLDecoder.decode(packageURL.getFile(), StandardCharsets.UTF_8.name());
		jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
		try (JarFile jf = new JarFile(jarFileName)) {
			jarEntries = jf.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (jarEntry.isDirectory()) {
					continue;
				}

				entryName = jarEntry.getName();
				if (entryName.startsWith(packagePathNamePlusSlash) && entryName.endsWith(".class") && !entryName.contains("$")) {
					entryName = entryName.substring(0, entryName.lastIndexOf('.'));
					entryName = entryName.replace("/", ".");
					names.add(entryName);
				}
			}
		}
	}

}
