package fr.ccouturi.config;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlateformConfig {

    @JsonProperty("plateform_name")
    private String plateformName;

    @JsonProperty("checker_configs")
    private List<HealthCheckerConfig> healthCherckerConfigs;

    private PlateformConfig() {
        // Ok for introspection
    }

    public PlateformConfig(String plateformName, List<HealthCheckerConfig> healthCherckerConfigs) {
        super();
        setPlateformName(plateformName);
        setHealthCherckerConfigs(healthCherckerConfigs);
    }

    public PlateformConfig(String plateformName, HealthCheckerConfig... healthCherckerConfig) {
        super();
        setPlateformName(plateformName);
        setHealthCherckerConfigs(Arrays.asList(healthCherckerConfig));
    }

    @JsonIgnore
    public String getPlateformName() {
        return plateformName;
    }

    @JsonIgnore
    public void setPlateformName(String plateformName) {
        this.plateformName = plateformName;
    }

    @JsonIgnore
    public List<HealthCheckerConfig> getHealthCherckersConfig() {
        return healthCherckerConfigs;
    }

    @JsonIgnore
    public void setHealthCherckerConfigs(List<HealthCheckerConfig> healthCherckerConfigs) {
        this.healthCherckerConfigs = healthCherckerConfigs;
    }

}
