package fr.ccouturi.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestConfig {

    @Test
    public void testJsonSerialization() throws JsonProcessingException {
        HealthCheckerConfig productA = new HealthCheckerConfig("productA", "http://url1.fr");
        HealthCheckerConfig productB = new HealthCheckerConfig("productB", "http://url1.fr", "http://url2.fr");
        PlateformConfig plateform1 = new PlateformConfig("staging", productA, productB);
        PlateformConfig plateform2 = new PlateformConfig("prod");
        PulseOMeterConfig config = new PulseOMeterConfig(plateform1, plateform2);

        String expectedProductA = "{\"name\":\"productA\",\"verb\":\"head\",\"url\":[\"http://url1.fr\"]}";
        String expectedProductB = "{\"name\":\"productB\",\"verb\":\"head\",\"url\":[\"http://url1.fr\",\"http://url2.fr\"]}";
        String expectedPateform1 = String.format("{\"plateform_name\":\"staging\",\"checker_configs\":[%s,%s]}", expectedProductA, expectedProductB);
        String expectedPateform2 = "{\"plateform_name\":\"prod\",\"checker_configs\":[]}";
        String expectedConfig = String.format("{\"plateform_configs\":[%s,%s]}", expectedPateform1, expectedPateform2);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(expectedProductA, mapper.writeValueAsString(productA));
        assertEquals(expectedProductB, mapper.writeValueAsString(productB));
        assertEquals(expectedPateform1, mapper.writeValueAsString(plateform1));
        assertEquals(expectedPateform2, mapper.writeValueAsString(plateform2));
        assertEquals(expectedConfig, mapper.writeValueAsString(config));
    }

    @Test
    public void shouldLoadConfigFromFile() throws JsonParseException, JsonMappingException, IOException {
        PulseOMeterConfig config = PulseOMeterConfig.loadFromFile(loadFile("valid_config_file.json"));
        assertEquals("staging", config.getPlateformsConfig().get(0).getPlateformName());
        assertEquals("http://url2.fr", config.getPlateformsConfig().get(0).getHealthCherckersConfig().get(1).getUrl()[1]);
    }

    @Test(expected = JsonParseException.class)
    public void shouldThrowException_ifConfigFileIsNotValid() throws JsonParseException, JsonMappingException, IOException {
        PulseOMeterConfig.loadFromFile(loadFile("invalid_config_file.json"));
    }

    private File loadFile(String fileNameForClassLoader) {
        return new File(Thread.currentThread().getContextClassLoader()//
                .getResource(fileNameForClassLoader).getFile());
    }
}
