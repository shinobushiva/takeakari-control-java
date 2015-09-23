package jp.ac.sojou.izumi.chikaken.xbee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	private Properties conf;

	public Configuration(File file) {
		conf = new Properties();
		try {
			conf.load(new FileInputStream(file));
		} catch (IOException e) {
			System.err.println("Cannot open " + file.getAbsolutePath() + ".");
			e.printStackTrace();
		}
	}

	public String getProperty(String key) {
		if (conf.containsKey(key))
			return conf.getProperty(key);
		else {
			System.err.println("Key not found: " + key);
			return null;
		}
	}

	public void addProperty(String key, String value) {
		if (conf.containsKey(key))
			System.err.println("Key already exists: " + key);
		else {
			conf.setProperty(key, value);
		}
	}

	public void store(File file, String comments) {
		try {
			conf.store(new FileOutputStream(file), comments);
		} catch (IOException e) {
			System.err.println("Cannot open " + file.getAbsolutePath() + ".");
			e.printStackTrace();
		}
	}

	public void storeToXML(File file, String comments) {
		try {
			conf.storeToXML(new FileOutputStream(file), comments);
		} catch (IOException e) {
			System.err.println("Cannot open " + file.getAbsolutePath() + ".");
			e.printStackTrace();
		}
	}
}
