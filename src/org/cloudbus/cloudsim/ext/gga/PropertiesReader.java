package org.cloudbus.cloudsim.ext.gga;

import java.io.InputStream;

public class PropertiesReader {
	static final String FILE_NAME = "gga.properties";
	java.util.Properties properties = null;
	private static PropertiesReader properties_object = null;

	private PropertiesReader() {
		init();
	}

	public static PropertiesReader loader() {
		if (properties_object == null)
			properties_object = new PropertiesReader();
		return properties_object;
	}

	void init() {
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(FILE_NAME);
		if (is == null) {
			System.err.println("Cannot load " + FILE_NAME);
			return;
		}
		properties = new java.util.Properties();
		try {
			properties.load(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getString(String name) {
		return properties.getProperty(name);
	}

	public boolean getBoolean(String name) {
		return (properties.getProperty(name).equals("false")) ? false : true;
	}

	public int getInt(String name) {
		return (Integer.parseInt(properties.getProperty(name)));
	}
	
	public double getDouble(String name) {
		return (Double.parseDouble(properties.getProperty(name)));
	}

}
