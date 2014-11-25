package fr.ccouturi;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import fr.ccouturi.config.HealthCheckerConfig;

public class HealthChecker extends CachableChecker<List<Result>> implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(HealthChecker.class);

    private static final Integer CHECK_CONNECT_TIME_OUT = 10000;// MILLISECONDS
    private static final Integer CHECK_READ_TIME_OUT = 10000;// MILLISECONDS
    private static final String VERB = "head";

    // ---------------------------------------------------------------------------------------------

    private Client client;

    private String product;

    public String[] urls;

    public List<Result> results;

    public String verb = VERB;

    public boolean proxy = false;

    public int timeout;

    public HealthChecker(HealthCheckerConfig config) {
        this(config.getName(), config.getUrl());
        verb = config.getVerb();
        proxy = config.isProxy();
        timeout = config.getTimeout();
    }

    public HealthChecker(String product, String... urls) {
        this.product = product;
        this.urls = urls;

        client = Client.create();
        client.setConnectTimeout(CHECK_CONNECT_TIME_OUT);
        if (timeout <= 0) {
            client.setReadTimeout(CHECK_READ_TIME_OUT);
        } else {
            client.setReadTimeout(timeout);
        }
    }

    @Override
    protected List<Result> check() {
        List<Result> results = new ArrayList<Result>();
        LOGGER.debug("Check product health: " + product);
        if (urls == null || urls.length == 0) {
            results.add(new Result(product, null, urls));
        }
        for (String url : urls) {
            try {
                WebResource r = client.resource(url);
                ClientResponse response = null;
                switch (verb.toLowerCase()) {
                case "get":
                    response = r.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                    if (proxy) {
                        results.addAll(parseResponse(response));
                    } else {
                        results.add(parseResponse(response.getStatus(), response.getEntity(String.class)));
                    }
                    break;
                default:
                    results.add(parseResponse(r.head().getStatus(), ""));
                    break;
                }
            } catch (ClientHandlerException e) {
                LOGGER.warn(String.format("Exception (%s) during healthcheck inspection for: %s.", e.getMessage(), url));
                results.add(new Result(product, Boolean.FALSE, urls));
            }
        }
        return results;
    }

    private List<Result> parseResponse(ClientResponse response) {
        if (200 == response.getStatus()) {
            return response.getEntity(new GenericType<List<Result>>() {
            });
        } else {
            List<Result> results = new ArrayList<Result>();
            results.add(new Result(product, Boolean.FALSE, urls));
            return results;
        }
    }

    private Result parseResponse(int status, String content) {
        if (200 == status) {
            return new Result(product, content, Boolean.TRUE, urls);
        } else {
            LOGGER.info(String.format("Healthcheck status code != 200 for: %s (status code: %s)", urls, status));
            return new Result(product, content, Boolean.FALSE, urls);
        }
    }

    @Override
    public void run() {
        results = findInCacheOrCompute();
    }
}
