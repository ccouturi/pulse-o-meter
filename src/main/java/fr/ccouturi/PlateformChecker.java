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

public class PlateformChecker extends CachableChecker<Result[]> {

    private static Logger LOGGER = LoggerFactory.getLogger(PlateformChecker.class);

    private static final Integer THREAD_COUNT = 5;

    // ---------------------------------------------------------------------------------------------

    @JsonProperty
    private String name;

    @JsonIgnore
    private List<HealthChecker> checkers;

    @JsonIgnore
    private List<Runnable> runnables;

    public PlateformChecker(String name, HealthChecker... checkers) {
        this.name = name;
        this.checkers = new ArrayList<>(checkers.length);

        runnables = new ArrayList<Runnable>();
        for (HealthChecker checker : checkers) {
            this.checkers.add(checker);
            runnables.add(checker);
        }
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
    protected Result[] check() {
        final ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        long start = System.currentTimeMillis();
        for (Runnable r : runnables) {
            pool.execute(r);
        }
        pool.shutdown();
        LOGGER.info("Waiting for healthchecks ending.");
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
        LOGGER.info(String.format("Check in %s milliseconds for plateform %s.", System.currentTimeMillis() - start, name));
        return collectResults();
    }

    @JsonIgnore
    private Result[] collectResults() {
        int checkersCount = checkers.size();
        Result[] results = new Result[checkersCount];
        int index = 0;
        for (HealthChecker checker : checkers) {
            results[index++] = checker.result;
        }
        return results;
    }

}
