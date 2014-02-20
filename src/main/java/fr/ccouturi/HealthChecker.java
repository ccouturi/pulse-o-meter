package fr.ccouturi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource;

public class HealthChecker extends CachableChecker<Result> implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(HealthChecker.class);

    private static final Integer CHECK_CONNECT_TIME_OUT = 3000;// MILLISECONDS
    private static final Integer CHECK_READ_TIME_OUT = 3000;// MILLISECONDS

    // ---------------------------------------------------------------------------------------------

    private Client client;

    private String product;

    public String[] urls;

    public Result result;

    public HealthChecker(String product) {
        this(product, new String[0]);
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
        LOGGER.info("Check product health: " + product);
        for (String url : urls) {
            try {
                WebResource r = client.resource(url);
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
        result = findInCacheOrCompute();
    }
}
