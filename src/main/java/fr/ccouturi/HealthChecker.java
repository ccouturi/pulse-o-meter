package fr.ccouturi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import fr.ccouturi.config.HealthCheckerConfig;

public class HealthChecker extends CachableChecker<Result> implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(HealthChecker.class);

    private static final Integer CHECK_CONNECT_TIME_OUT = 3000;// MILLISECONDS
    private static final Integer CHECK_READ_TIME_OUT = 3000;// MILLISECONDS
    private static final String VERB = "head";

    // ---------------------------------------------------------------------------------------------

    private Client client;

    private String product;

    public String[] urls;

    public Result result;

    public String verb = VERB;

    public HealthChecker(HealthCheckerConfig config) {
        this(config.getName(), config.getUrl());
        verb = config.getVerb();
    }

    public HealthChecker(String product, String... urls) {
        this.product = product;
        this.urls = urls;

        client = Client.create();
        client.setConnectTimeout(CHECK_CONNECT_TIME_OUT);
        client.setReadTimeout(CHECK_READ_TIME_OUT);
    }

    @Override
    protected Result check() {
        LOGGER.debug("Check product health: " + product);
        for (String url : urls) {
            try {
                WebResource r = client.resource(url);
                int result;
                switch (verb.toLowerCase()) {
                case "get":
                    result = r.get(ClientResponse.class).getStatus();
                    break;
                default:
                    result = r.head().getStatus();
                    break;
                }
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
        result = findInCacheOrCompute();
    }
}
