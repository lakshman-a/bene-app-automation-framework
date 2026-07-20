package qa.beneapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager - Singleton that loads environment-specific properties.
 *
 * Usage:
 *   ConfigManager.get("app.base.url")
 *
 * Environment is determined by:
 *   1. System property: -Denv=qa
 *   2. Maven property passed via surefire
 *   3. Default: "qa"
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static Properties properties;

    private ConfigManager() {
    }

    public static synchronized String get(String key) {
        if (properties == null) {
            loadProperties();
        }
        // System properties override file properties (useful for Jenkins)
        String systemProp = System.getProperty(key);
        if (systemProp != null) {
            return systemProp;
        }
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    // ---- Typed convenience accessors -------------------------------------
    public static int getInt(String key, int defaultValue) {
        String v = get(key);
        return v != null ? Integer.parseInt(v.trim()) : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        return v != null ? Boolean.parseBoolean(v.trim()) : defaultValue;
    }

    /** Active environment (dev | qa | uat). */
    public static String env() {
        return System.getProperty("env", "qa");
    }

    /** UI base URL, e.g. http://localhost:4200 */
    public static String uiBaseUrl() {
        return get("app.base.url");
    }

    /** API base URL, e.g. http://localhost:8080 */
    public static String apiBaseUrl() {
        return get("api.base.url");
    }

    /** Static bearer token the bene-app API requires on every request. */
    public static String apiToken() {
        return get("api.token");
    }

    private static void loadProperties() {
        properties = new Properties();
        String env = System.getProperty("env", "qa");
        String fileName = "config-" + env + ".properties";

        log.info("Loading configuration for environment: {}", env);
        log.info("Config file: {}", fileName);

        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                log.warn("Config file '{}' not found, falling back to config-qa.properties", fileName);
                try (InputStream fallback = ConfigManager.class.getClassLoader()
                        .getResourceAsStream("config-qa.properties")) {
                    if (fallback != null) {
                        properties.load(fallback);
                    }
                }
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            log.error("Failed to load configuration: {}", e.getMessage());
            throw new RuntimeException("Could not load config file: " + fileName, e);
        }

        log.info("Configuration loaded successfully. Base URL: {}", properties.getProperty("app.base.url"));
    }
}
