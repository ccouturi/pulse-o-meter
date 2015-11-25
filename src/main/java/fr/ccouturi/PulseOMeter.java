package fr.ccouturi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ccouturi.config.PlateformConfig;
import fr.ccouturi.config.PulseOMeterConfig;

public class PulseOMeter extends CachableChecker<Map<String, List<Result>>> {

    private List<PlateformChecker> plateformChekers = new ArrayList<>();

    public PulseOMeter(PulseOMeterConfig config) {
        init(config);
    }

    public PulseOMeter(File configFile) throws IOException {
        if (configFile == null) {
            throw new IllegalArgumentException("Config file cannot be null!");
        }

        try {
            PulseOMeterConfig config = new ObjectMapper().readValue(configFile, PulseOMeterConfig.class);
            init(config);
        } catch (JsonParseException | JsonMappingException e) {
            throw new IllegalArgumentException("Json isn't valid!", e);
        }
    }

    private void init(PulseOMeterConfig config) {
        CachableChecker.initCache(config);
        for (PlateformConfig PlateformConfig : config.getPlateformsConfig()) {
            plateformChekers.add(new PlateformChecker(PlateformConfig));
        }
    }

    @Override
    protected Map<String, List<Result>> check() {
        Map<String, List<Result>> results = new HashMap<>();

        for (PlateformChecker plateformChecker : plateformChekers) {
            List<Result> plateformResults = plateformChecker.getResult();
            results.put(plateformChecker.key, plateformResults);
        }
        return results;
    }

}
