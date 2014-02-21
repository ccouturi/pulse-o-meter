package fr.ccouturi.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PulseOMeterConfig {

    @JsonProperty("plateform_configs")
    private List<PlateformConfig> plateformConfigs;

    private PulseOMeterConfig() {
        // Ok for introspection
    }

    public PulseOMeterConfig(List<PlateformConfig> plateformConfigs) {
        super();
        setPlateformConfigs(plateformConfigs);
    }

    public PulseOMeterConfig(PlateformConfig... plateformConfigs) {
        this(Arrays.asList(plateformConfigs));
    }

    @JsonIgnore
    public List<PlateformConfig> getPlateformsConfig() {
        return plateformConfigs;
    }

    @JsonIgnore
    public void setPlateformConfigs(List<PlateformConfig> plateformConfigs) {
        this.plateformConfigs = plateformConfigs;
    }

    public static PulseOMeterConfig loadFromFile(File config) throws JsonParseException, JsonMappingException, IOException {
        return new ObjectMapper().readValue(config, PulseOMeterConfig.class);
    }
}
