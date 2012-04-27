package timc.common;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TIMConfigurator {
	
	private static final Logger logger =
			LoggerFactory.getLogger(TIMConfigurator.class);
	private static final Properties properties;
	
	static {
		properties = new Properties();
		try {
			properties.load(new FileInputStream("config/client.properties"));
		} catch (Exception e) {
			logger.error("Unable to load client's properties file: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

}
