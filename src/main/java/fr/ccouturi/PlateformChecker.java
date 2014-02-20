package fr.ccouturi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlateformChecker {

    private static Logger LOGGER = LoggerFactory.getLogger(PlateformChecker.class);

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
        // ExecutorService execute = Executors.newFixedThreadPool(10);
        // executeRunnables(execute);

        for (int index = 0; index < checkers.length; ++index) {
            checkers[index].run();
        }

        Result[] results = new Result[checkers.length];
        for (int index = 0; index < checkers.length; ++index) {
            results[index] = checkers[index].result;
        }
        return results;
    }

    @JsonIgnore
    private void executeRunnables(final ExecutorService service) {
        for (Runnable r : runnables) {
            service.execute(r);
        }
        LOGGER.info("Waiting for healthchecks ending.");
        service.shutdown();
        LOGGER.info("Done.");
    }

}
