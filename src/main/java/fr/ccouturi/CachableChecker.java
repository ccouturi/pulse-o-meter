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

import fr.ccouturi.config.PulseOMeterConfig;

public abstract class CachableChecker<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(CachableChecker.class);

    protected static Cache<String, Object> cache;

    @JsonIgnore
    protected String key = UUID.randomUUID().toString();

    public static void initCache(PulseOMeterConfig config) {
        cache = CacheBuilder.newBuilder()//
                .expireAfterWrite(config.getCachePeriod(), TimeUnit.SECONDS) //
                .build();
    }

    public T findInCacheOrCompute() {
        LOGGER.debug("Get cached result or compute.");
        try {
            return (T) cache.get(key, new Callable<T>() {
                @Override
                public T call() {
                    return check();
                }
            });
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public T getResult() {
        return findInCacheOrCompute();
    }

    protected abstract T check();

}
