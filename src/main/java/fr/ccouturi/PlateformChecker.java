package fr.ccouturi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.ccouturi.config.HealthCheckerConfig;
import fr.ccouturi.config.PlateformConfig;

public class PlateformChecker extends CachableChecker<List<Result>> {

    private static Logger LOGGER = LoggerFactory.getLogger(PlateformChecker.class);

    private static final Integer THREAD_COUNT = 20;

    // ---------------------------------------------------------------------------------------------

    @JsonProperty
    private String name;

    @JsonIgnore
    private List<HealthChecker> checkers;

    @JsonIgnore
    private List<Runnable> runnables;

    public PlateformChecker(PlateformConfig config) {
        init(config.getPlateformName(), config.getHealthCherckersConfig());
    }

    public PlateformChecker(String name, HealthChecker... checkers) {
        init(name, checkers);
    }

    private void init(String name, HealthChecker... checkers) {
        this.name = name;
        this.checkers = new ArrayList<>(checkers.length);

        runnables = new ArrayList<Runnable>(checkers.length);
        for (HealthChecker checker : checkers) {
            this.checkers.add(checker);
            runnables.add(checker);
        }
    }

    private void init(String name, List<HealthCheckerConfig> checkersConfig) {
        HealthChecker[] checkers = new HealthChecker[checkersConfig.size()];
        for (int index = 0; index < checkersConfig.size(); ++index) {
            checkers[index] = new HealthChecker(checkersConfig.get(index));
        }
        init(name, checkers);
    }

    public PlateformChecker register(HealthChecker checker) {
        checkers.add(checker);
        runnables.add(checker);
        return this;
    }

    public PlateformChecker register(String productName, String... urls) {
        HealthChecker checker = new HealthChecker(productName, urls);
        register(checker);
        return this;
    }

    @Override
    @JsonIgnore
    protected List<Result> check() {
        LOGGER.info("Check plateform " + name);
        final ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        long start = System.currentTimeMillis();
        for (Runnable r : runnables) {
            pool.execute(r);
        }
        pool.shutdown();
        LOGGER.info("Waiting for healthchecks ending.");
        try {
            if (!pool.awaitTermination(50, TimeUnit.SECONDS)) {
                LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
        LOGGER.info(String.format("Check in %s milliseconds for plateform %s.", System.currentTimeMillis() - start, name));
        return collectResults();
    }

    @JsonIgnore
    private List<Result> collectResults() {
        List<Result> results = new ArrayList<Result>();
        for (HealthChecker checker : checkers) {
            results.addAll(checker.results);
        }
        return results;
    }

}
