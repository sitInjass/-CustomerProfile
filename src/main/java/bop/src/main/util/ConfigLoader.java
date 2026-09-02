package bop.src.main.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class ConfigLoader {

    private static Properties properties = new Properties();

    static {
        try {

            Properties envProps = new Properties();
            InputStream envInput = ConfigLoader.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            if (envInput == null) {
                throw new RuntimeException("config.properties not found");
            }

            envProps.load(envInput);
            String env = envProps.getProperty("app.env", "dev");

            // 2. Load environment-specific file
            String fileName = "config-" + env + ".properties";
            InputStream configInput = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName);

            if (configInput == null) {
                throw new RuntimeException(fileName + " not found");
            }

            properties.load(configInput);

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load configuration");
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
