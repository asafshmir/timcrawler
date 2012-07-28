package timc.common;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.common.Utils.OperationMode;

public class TIMConfigurator {
	
	private static final Logger logger =
			LoggerFactory.getLogger(TIMConfigurator.class);
	private static final Properties properties;
	
	// specific configurations
	private static OperationMode opMode;
	private static float halfSeedCompletionRate;
	
	static {
		properties = new Properties();
		try {
			properties.load(TIMConfigurator.class.getResourceAsStream("/client.properties"));
		} catch (Exception e) {
			logger.error("Unable to load client's properties file: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static void initialize()
	{
		// initializing specific configurations
		opMode = OperationMode.get(Integer.parseInt(getProperty("operation_mode")));
		if (opMode == OperationMode.NeverNotifySeeder || opMode == OperationMode.HalfSeedDropNewPieces) {
			halfSeedCompletionRate = Float.parseFloat(getProperty("half_seed_completion_rate"));
			if (halfSeedCompletionRate < 0 || halfSeedCompletionRate > 1) {
				logger.error("Invalid half_seed_completion_rate: {}. Should be between 0 and 1", halfSeedCompletionRate);
			}
		}
	}
	
	public static OperationMode getOpMode() {
		return opMode;
	}
	
	public static float getHalfSeedCompletionRate() {
		return halfSeedCompletionRate;
	}
}
