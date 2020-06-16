package gui;

import java.net.URL;

public class URLGetter {
	
	private URLGetter() {}
	

	public static URL getResource(String filename) {
		URL url = ClassLoader.getSystemResource(filename);
		if (url == null) { 
			try {
				url = new URL("file", "localhost", filename);
			} catch (Exception urlException) {} // ignore
		}
		return url;
	}
}
