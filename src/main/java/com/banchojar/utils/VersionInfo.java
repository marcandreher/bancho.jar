package com.banchojar.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to access application version information.
 */
public class VersionInfo {
    
    private static final Properties properties = new Properties();
    // Fixed spelling of the properties file name
    private static final String VERSION_PROPERTIES_FILE = "version.properties";
    
    private static String version = "unknown";
    private static String name = "unknown";
    private static String buildTimestamp = "unknown";
    private static String defaultAvatar = "unknown";
    
    static {
        loadVersionProperties();
    }
    
    private static void loadVersionProperties() {
        try (InputStream inputStream = VersionInfo.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                version = properties.getProperty("application.version", "unknown");
                name = properties.getProperty("application.name", "unknown");
                buildTimestamp = properties.getProperty("application.build.timestamp", "unknown");
                defaultAvatar = properties.getProperty("banchojar.default.avatar", "unknown");
            } else {
                System.err.println("Version properties file not found");
            }
        } catch (IOException e) {
            System.err.println("Error loading version properties: " + e.getMessage());
        }
    }
    
    /**
     * Gets the application version.
     * 
     * @return The application version from Maven's project.version
     */
    public static String getVersion() {
        return version;
    }
    
    /**
     * Gets the default avatar URL.
     * 
     * @return The default avatar URL
     */
    public static String getDefaultAvatar() {
        return defaultAvatar;
    }

    /**
     * Gets the application name.
     * 
     * @return The application name from Maven's project.name
     */
    public static String getName() {
        return name;
    }
    
    /**
     * Gets the build timestamp.
     * 
     * @return The timestamp when the application was built
     */
    public static String getBuildTimestamp() {
        return buildTimestamp;
    }
    
    /**
     * Gets a formatted version string.
     * 
     * @return A formatted version string containing name, version and build timestamp
     */
    public static String getFormattedVersion() {
        return String.format("%s version %s (built on %s)", name, version, buildTimestamp);
    }
}