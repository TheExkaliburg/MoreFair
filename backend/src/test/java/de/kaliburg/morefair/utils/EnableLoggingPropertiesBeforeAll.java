package de.kaliburg.morefair.utils;

import de.kaliburg.morefair.utils.EnableLoggingPropertiesBeforeAll.LoadPropertiesBeforeAll;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(LoadPropertiesBeforeAll.class)
public @interface EnableLoggingPropertiesBeforeAll {

  @Slf4j
  class LoadPropertiesBeforeAll implements BeforeAllCallback {

    private static final String LOGGING_LEVEL_PREFIX = "logging.level";
    private Properties properties;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
      ClassPathResource resource = new ClassPathResource("application.properties");
      properties = PropertiesLoaderUtils.loadProperties(resource);
      setLoggingLevels();
    }

    private void setLoggingLevels() {
      LoggingSystem loggingSystem = LoggingSystem.get(getClass().getClassLoader());
      for (String propertyName : properties.stringPropertyNames()) {
        if (propertyName.startsWith(LOGGING_LEVEL_PREFIX)) {
          String loggerName = propertyName.substring(LOGGING_LEVEL_PREFIX.length() + 1);
          String logLevel = properties.getProperty(propertyName);
          loggingSystem.setLogLevel(loggerName, LogLevel.valueOf(logLevel.toUpperCase()));
        }
      }
    }
  }
}
