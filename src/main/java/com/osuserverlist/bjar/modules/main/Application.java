package com.osuserverlist.bjar.modules.main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.App;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    public static String HEADER = "";

    static {
        try (InputStream in = App.class.getResourceAsStream("/header.txt")) {
            HEADER = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to read header.txt", e);
        }
    }

    public static class BuildInfo {

        public static final String VERSION;
        public static final String BUILD_TIME;
        public static final String JAVA_VERSION;
        public static final String GRADLE_VERSION;

        static {
            String buildPropertiesResource = "/build.properties";
            try (var inputStream = BuildInfo.class.getResourceAsStream(buildPropertiesResource)) {
                if (inputStream == null) {
                    throw new RuntimeException("Build properties file not found: " + buildPropertiesResource);
                }
                var properties = new Properties();
                properties.load(inputStream);
                VERSION = properties.getProperty("version", "unknown");
                BUILD_TIME = properties.getProperty("buildTime", "unknown");
                JAVA_VERSION = properties.getProperty("javaVersion", "unknown");
                GRADLE_VERSION = properties.getProperty("gradleVersion", "unknown");

            } catch (Exception e) {
                throw new RuntimeException("Failed to load build properties", e);
            }
        }

    }

}
