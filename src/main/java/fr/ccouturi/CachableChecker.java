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

public abstract class CachableChecker<T> {

    private static final long CACHE_EXPIRATION_PERIOD = 5;// SECONDS

    private static Logger LOGGER = LoggerFactory.getLogger(CachableChecker.class);

    protected static Cache<String, Object> cache = CacheBuilder.newBuilder()//
            .expireAfterWrite(CACHE_EXPIRATION_PERIOD, TimeUnit.SECONDS) //
            .build();

    @JsonIgnore
    protected String key = UUID.randomUUID().toString();

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
