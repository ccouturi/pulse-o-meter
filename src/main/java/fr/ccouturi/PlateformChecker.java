package fr.ccouturi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class PlateformChecker {

    private static Logger LOGGER = LoggerFactory.getLogger(PlateformChecker.class);

    private static final Integer THREAD_COUNT = 5;

    private static final long CACHE_EXPIRATION_PERIOD = 5;// SECONDS

    protected static Cache<String, Object> cache = CacheBuilder.newBuilder()//
            .expireAfterWrite(CACHE_EXPIRATION_PERIOD, TimeUnit.SECONDS) //
            .build();

    // ---------------------------------------------------------------------------------------------

    @JsonIgnore
    private String key = UUID.randomUUID().toString();

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
    public Result[] getCacheResult() {
        LOGGER.debug("Get cached result or check.");
        long start = System.currentTimeMillis();
        try {
            Result[] results = (Result[]) cache.get(key, new Callable<Result[]>() {
                @Override
                public Result[] call() {
                    return check();
                }
            });
            LOGGER.info("Check duration (milliseconds): " + (System.currentTimeMillis() - start));
            return results;
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @JsonIgnore
    private Result[] check() {
        final ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
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
        return collectResults();
    }

    @JsonIgnore
    private Result[] collectResults() {
        Result[] results = new Result[checkers.length];
        for (int index = 0; index < checkers.length; ++index) {
            results[index] = checkers[index].result;
        }
        return results;
    }

}
