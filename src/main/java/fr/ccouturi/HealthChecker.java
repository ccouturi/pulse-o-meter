package fr.ccouturi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.*;
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

    public List<Result> results = new ArrayList<>();

    public String verb = VERB;

    public boolean proxy = false;

    public HealthChecker(HealthCheckerConfig config) {
        this(config.getName(), config.getTimeout(), config.getUrl());
        verb = config.getVerb();
        proxy = config.isProxy();
    }

    public HealthChecker(String product, int timeout, String... urls) {
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
                        results.add(parseResponse(response.getStatus(), response.getHeaders().getFirst("version"), response.getEntity(String.class)));
                    }
                    break;
                case "head":
                    response = r.accept(MediaType.APPLICATION_JSON).head();
                    results.add(parseResponse(response.getStatus(), response.getHeaders().getFirst("version"), response.getEntity(String.class)));
                    break;
                default:
                    results.add(parseResponse(r.head().getStatus(), getVersion(response), ""));
                    break;
                }
            } catch (ClientHandlerException e) {
                LOGGER.warn(String.format("Exception (%s) during healthcheck inspection for: %s.", e.getMessage(), url));
                results.add(new Result(product, Boolean.FALSE, urls));
            }
        }
        return results;
    }

    private String getVersion(ClientResponse response) {
        if (response != null && response.getHeaders() != null) {
            if (response.getHeaders().getFirst("X-Version") != null) {
                response.getHeaders().getFirst("X-Version");
            } else {
                return response.getHeaders().getFirst("version");
            }
        }
        return null;
    }

    private List<Result> parseResponse(ClientResponse response) {
        if (200 == response.getStatus()) {
            List<Result> results = new ArrayList<>();
            results.addAll(response.getEntity(new GenericType<List<Result>>() {
            }));
            results.add(new Result(product, "", Boolean.TRUE, getVersion(response), new Date(), urls));
            return results;
        } else {
            List<Result> results = new ArrayList<Result>();
            results.add(new Result(product, Boolean.FALSE, urls));
            return results;
        }
    }

    private Result parseResponse(int status, String version, String content) {
        if (200 == status) {
            return new Result(product, content, Boolean.TRUE, version, new Date(), urls);
        } else {
            LOGGER.info(String.format("Healthcheck status code != 200 for: %s (status code: %s)", urls, status));
            return new Result(product, content, Boolean.FALSE, version, new Date(), urls);
        }
    }

    @Override
    public void run() {
        results = findInCacheOrCompute();
    }
}
