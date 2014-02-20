package fr.ccouturi;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource;

public class HealthChecker implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(HealthChecker.class);

    private static final long CACHE_EXPIRATION_PERIOD = 5;// SECONDS

    private static final Integer CHECK_CONNECT_TIME_OUT = 3000;// MILLISECONDS
    private static final Integer CHECK_READ_TIME_OUT = 3000;// MILLISECONDS

    protected static Cache<String, Object> cache = CacheBuilder.newBuilder()//
            .expireAfterWrite(CACHE_EXPIRATION_PERIOD, TimeUnit.SECONDS) //
            .build();

    // ---------------------------------------------------------------------------------------------

    private String key = UUID.randomUUID().toString();

    private String product;

    public String[] urls;

    public Result result;

    public HealthChecker(String product) {
        this(product, new String[0]);
    }

    public HealthChecker(String product, String... urls) {
        this.product = product;
        this.urls = urls;
    }

    @JsonIgnore
    public Result getCacheResult() {
        LOGGER.info("Get cached result or check.");
        try {
            return (Result) cache.get(key, new Callable<Result>() {
                @Override
                public Result call() {
                    return compute();
                }
            });
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public Result compute() {
        LOGGER.debug("Check product health: " + product);
        Client c = Client.create();
        c.setConnectTimeout(CHECK_CONNECT_TIME_OUT);
        c.setReadTimeout(CHECK_READ_TIME_OUT);
        for (String url : urls) {
            try {
                WebResource r = c.resource(url);
                int result = r.head().getStatus();
                if (200 != result) {
                    LOGGER.info(String.format("Healthcheck status code != 200 for: %s (status code: %s)", url, result));
                    return new Result(product, Boolean.FALSE, urls);
                }
                return new Result(product, Boolean.TRUE, urls);
            } catch (ClientHandlerException e) {
                LOGGER.warn(String.format("Exception (%s) during healthcheck inspection for: %s.", e.getMessage(), url));
                return new Result(product, Boolean.FALSE, urls);
            }
        }
        return new Result(product, null, urls);
    }

    @Override
    public void run() {
        result = getCacheResult();
    }
}
