package de.jochor.lib.servicefactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public  class MyClassLoader extends URLClassLoader {
	
	private URL baseURL;

	public MyClassLoader(URL baseURL) {
		super(new URL[0]);
		
		this.baseURL = baseURL;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> loadedClass = tryUnderBaseURL(name);
		
		if (loadedClass==null){
			 loadedClass = super.loadClass(name);
		}

		return loadedClass;
	}

	private Class<?> tryUnderBaseURL(String name) {
		String resName = name.replace('.', '/').concat(".class");
		try (InputStream resourceStream = baseURL.toURI().resolve(resName).toURL().openStream()) {
			if (resourceStream != null) {
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
			}

			return null;
		} catch (Throwable e) {
			return null;
		}
	}

}
