package dev.gether.getutils;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the creation and initialization of configuration objects.
 */
public class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    /**
     * Creates and initializes a new instance of a configuration class.
     *
     * <p>Usage example:</p>
     * <pre>
     * {@code
     * MyConfig config = ConfigManager.create(MyConfig.class, cfg -> {
     *     cfg.setFile(new File(getDataFolder(), "config.yml"));
     *     cfg.load();
     * });
     * }
     * </pre>
     *
     * @param <T> The type of the configuration class, must extend GetConfig
     * @param clazz The Class object of the configuration class
     * @param configConsumer A Consumer that performs initialization on the created instance
     * @return An initialized instance of the configuration class, or null if creation failed
     * @throws IllegalArgumentException if clazz is null
     */
    public static <T extends GetConfig> T create(Class<T> clazz, Consumer<T> configConsumer) {
        if (clazz == null) {
            throw new IllegalArgumentException("Configuration class cannot be null");
        }

        T configInstance = null;
        try {
            configInstance = clazz.getDeclaredConstructor().newInstance();
            configConsumer.accept(configInstance);
            return configInstance;
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to create configuration instance of " + clazz.getName(), e);
            cleanup(configInstance);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred while creating configuration instance of " + clazz.getName(), e);
            cleanup(configInstance);
            return null;
        }
    }

    private static <T extends GetConfig> void cleanup(T configInstance) {
        if (configInstance != null) {
            try {
                Field fileField = GetConfig.class.getDeclaredField("file");
                Field urlField = GetConfig.class.getDeclaredField("url");
                Field contentField = GetConfig.class.getDeclaredField("content");

                fileField.setAccessible(true);
                urlField.setAccessible(true);
                contentField.setAccessible(true);

                fileField.set(configInstance, null);
                urlField.set(configInstance, null);
                contentField.set(configInstance, null);

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to cleanup config instance", e);
            }
        }
    }
}