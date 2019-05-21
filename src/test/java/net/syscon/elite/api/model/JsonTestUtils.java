package net.syscon.elite.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import net.syscon.elite.web.config.ObjectMapperConfigurer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTestUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        ObjectMapperConfigurer.configure(MAPPER);
    }

    public static <T> void assertSerialization(T object, final String resourceName) {

        val actualJson = serializeObject(object);
        val actualNormalisedJson = normalise(actualJson);

        val expectedJson = readResource(resourceName);
        val expectedNormalisedJson = normalise(expectedJson);

        assertThat(actualNormalisedJson).isEqualTo(expectedNormalisedJson);
    }

    private static <T> String serializeObject(T object) {
        try {
            return MAPPER.writer().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object: " + object, e);
        }
    }

    private static String readResource(String resourceName) {
        try {
            val path = Paths.get(ClassLoader.getSystemResource(resourceName).toURI());
            return Files.readString(path);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Problem reading resource: " + resourceName, e);
        }
    }

    private static String normalise(String json) {
        try {
            return MAPPER.writer().withDefaultPrettyPrinter().writeValueAsString(MAPPER.readTree(json));
        } catch (IOException e) {
            throw new RuntimeException("Could not read json:" + json, e);
        }
    }
}
