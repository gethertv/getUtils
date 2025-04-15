package dev.gether.getutils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.gether.getutils.adapter.ServerSerializer;
import dev.gether.getutils.adapter.ServerSerializerFactory;
import dev.gether.getutils.annotation.Comment;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The GetConfig class provides functionality for loading, saving, and managing configuration data.
 * It supports both file-based and URL-based configurations, as well as in-memory content.
 */
@Getter
public class GetConfig {
    private static final Logger logger = LoggerFactory.getLogger(GetConfig.class);

    private final ObjectMapper mapper;
    private File file;
    private URL url;
    private String content;

    @JsonIgnore
    private boolean noAutoSave = false;


    /**
     * Constructs a new GetConfig instance and initializes the ObjectMapper.
     */
    public GetConfig() {
        this.mapper = createObjectMapper();
    }

    /**
     * Creates and configures an ObjectMapper for YAML serialization and deserialization.
     *
     * @return A configured ObjectMapper instance.
     */
    private ObjectMapper createObjectMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();

        SimpleModule module = new SimpleModule();
        ServerSerializer serializer = ServerSerializerFactory.createSerializer();
        serializer.registerSerializers(module);
        serializer.registerDeserializers(module);

        mapper.registerModule(module);
        return mapper;
    }


    /**
     * Sets the file to be used for loading and saving the configuration.
     * If the file doesn't exist, it will be created along with its parent directories.
     *
     * @param file The File object representing the configuration file.
     * @throws RuntimeException if there's an error creating the file.
     */
    public void setFile(File file) {
        this.file = file;
        this.url = null;
        if (!file.exists()) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Files.createFile(file.toPath());
            } catch (IOException e) {
                logger.error("Failed to create file: {}", file.getAbsolutePath(), e);
                throw new RuntimeException("Failed to create file", e);
            }
        }
    }

    /**
     * Sets the URL to be used for loading and saving the configuration.
     *
     * @param urlString The URL string representing the configuration source.
     * @throws RuntimeException if the URL is invalid.
     */
    public void setUrl(String urlString) {
        try {
            this.url = new URL(urlString);
            this.file = null;
        } catch (IOException e) {
            logger.error("Invalid URL: {}", urlString, e);
            throw new RuntimeException("Invalid URL: " + urlString, e);
        }
    }

    /**
     * Saves the current configuration to the specified file, URL, or in-memory content.
     *
     * @throws RuntimeException if there's an error saving the configuration.
     */
    public void save() {
        // Check if auto-save is disabled
        if (noAutoSave) {
            logger.debug("Auto-save is disabled. Skipping save operation.");
            return;
        }

        try {
            String yamlString = mapper.writeValueAsString(this);
            String processedYaml = insertComments(yamlString);

            if (file != null) {
                logger.debug("Saving configuration to file: {}", file.getAbsolutePath());
                Files.write(file.toPath(), processedYaml.getBytes(StandardCharsets.UTF_8));
            } else if (url != null) {
                logger.debug("Saving configuration to URL: {}", url);
                saveToUrl(processedYaml);
            } else {
                logger.debug("Saving configuration to memory");
                this.content = processedYaml;
            }
            logger.debug("Configuration saved successfully");
        } catch (IOException e) {
            logger.error("Failed to save configuration", e);
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    /**
     * Inserts comments into the YAML string based on @Comment annotations.
     *
     * @param yamlString The YAML string to insert comments into.
     * @return A new YAML string with comments inserted.
     */
    private String insertComments(String yamlString) {
        Map<String, String[]> comments = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Comment.class)) {
                Comment comment = field.getAnnotation(Comment.class);
                comments.put(field.getName(), comment.value());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String line : yamlString.split("\n")) {
            for (Map.Entry<String, String[]> entry : comments.entrySet()) {
                if (line.startsWith(entry.getKey() + ":")) {
                    for (String commentLine : entry.getValue()) {
                        sb.append("# ").append(commentLine).append("\n");
                    }
                    break;
                }
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Loads the configuration from the specified file, URL, or in-memory content.
     *
     * @throws RuntimeException if there's an error loading the configuration or if no source is set.
     */
    public void load() {
        try {
            boolean previousNoAutoSave = noAutoSave;
            // Temporarily enable noAutoSave during loading
            noAutoSave = true;

            if (file != null) {
                loadFromFile();
            } else if (url != null) {
                loadFromUrl();
            } else if (content != null) {
                mapper.readerForUpdating(this).readValue(content);
            } else {
                throw new IllegalStateException("Neither file, URL, nor content is set");
            }

            // Restore previous noAutoSave value
            noAutoSave = previousNoAutoSave;
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }


    /**
     * Loads the configuration from the specified URL.
     *
     * @throws IOException if there's an error reading from the URL.
     */
    private void loadFromUrl() throws IOException {
        logger.debug("Loading configuration from URL: {}", url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/yaml");

        try (InputStream is = connection.getInputStream()) {
            byte[] content = is.readAllBytes();

            if (content.length == 0) {
                logger.info("Empty configuration received from server. Creating default configuration.");

                // Temporarily disable noAutoSave to allow saving default config
                boolean previousNoAutoSave = noAutoSave;
                noAutoSave = false;
                save();
                noAutoSave = previousNoAutoSave;

                return;
            }

            try {
                mapper.readerForUpdating(this).readValue(content);
                logger.info("Successfully loaded configuration from URL");
                // Do NOT call save() here - this is likely what's causing the double operation
            } catch (Exception e) {
                logger.error("Error parsing configuration from URL: {}", url, e);

                // Temporarily disable noAutoSave to allow saving default config
                boolean previousNoAutoSave = noAutoSave;
                noAutoSave = false;
                save();
                noAutoSave = previousNoAutoSave;
            }
        }
    }

    /**
     * Loads the configuration from the specified file.
     *
     * @throws IOException if there's an error reading from the file.
     */
    private void loadFromFile() throws IOException {
        if (!file.exists() || Files.size(file.toPath()) == 0) {
            save();
            return;
        }
        mapper.readerForUpdating(this).readValue(file);
    }

    /**
     * Saves the configuration to the specified URL.
     *
     * @param content The configuration content to save.
     * @throws IOException if there's an error writing to the URL.
     */
    private void saveToUrl(String content) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/yaml");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }


    /**
     * Converts the current configuration to a YAML string.
     *
     * @return A YAML string representation of the configuration.
     * @throws IOException if there's an error during serialization.
     */
    @JsonIgnore
    public String toYaml() throws IOException {
        return mapper.writeValueAsString(this);
    }

    /**
     * Gets the ObjectMapper used by this configuration.
     *
     * @return The ObjectMapper instance.
     */
    @JsonIgnore
    public ObjectMapper getMapper() {
        return this.mapper;
    }

    @JsonIgnore
    public File getFile() {
        return file;
    }
}
