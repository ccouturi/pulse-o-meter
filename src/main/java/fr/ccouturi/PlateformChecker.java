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

public class PlateformChecker {

    private static Logger LOGGER = LoggerFactory.getLogger(PlateformChecker.class);

    private static final Integer THREAD_COUNT = 5;

    // ---------------------------------------------------------------------------------------------

    @JsonProperty
    private String name;

    @JsonIgnore
    private HealthChecker[] checkers;

    @JsonIgnore
    private List<Runnable> runnables;

    public PlateformChecker(String name, HealthChecker... checkers) {
        this.name = name;
        this.checkers = checkers;

        runnables = new ArrayList<Runnable>();
        for (HealthChecker checker : checkers) {
            runnables.add(checker);
        }
    }

    @JsonProperty
    public Result[] getResults() {
        long start = System.currentTimeMillis();
        ExecutorService execute = Executors.newFixedThreadPool(THREAD_COUNT);
        executeRunnables(execute);

        Result[] results = new Result[checkers.length];
        for (int index = 0; index < checkers.length; ++index) {
            results[index] = checkers[index].result;
        }
        LOGGER.info("Check duration (milliseconds): " + (System.currentTimeMillis() - start));
        return results;
    }

    @JsonIgnore
    private void executeRunnables(final ExecutorService service) {
        for (Runnable r : runnables) {
            service.execute(r);
        }
        service.shutdown();
        LOGGER.info("Waiting for healthchecks ending.");
        try {
            if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
        }
        LOGGER.info("Done.");
    }

}
