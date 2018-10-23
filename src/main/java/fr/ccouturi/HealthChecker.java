package fr.ccouturi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ccouturi.config.HealthCheckerConfig;

public class HealthChecker extends CachableChecker<List<Result>>implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(HealthChecker.class);

    private static final String PDUI_HEADER = "PDUI-Version";
    private static final Integer CHECK_CONNECT_TIME_OUT = 10000;// MILLISECONDS
    private static final Integer CHECK_READ_TIME_OUT = 10000;// MILLISECONDS
    private static final String VERB = "head";

    // ---------------------------------------------------------------------------------------------

    public static void main(String args[]) {
        HealthCheckerConfig config = new HealthCheckerConfig("test", "https://peoplecare.integration.people-doc.com/healthchecks");
        config.setVerb("get");
        config.setProxy(true);
        System.out.println(new HealthChecker(config).check());
    }

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

        ClientConfig config = new ClientConfig()//
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)//
                .property(ClientProperties.CONNECT_TIMEOUT, CHECK_CONNECT_TIME_OUT)//
                .property(ClientProperties.READ_TIMEOUT, timeout <= 0 ? CHECK_READ_TIME_OUT : timeout);
        client = ClientBuilder.newClient(config);

    }

    @Override
    protected List<Result> check() {
        List<Result> results = new ArrayList<>();
        LOGGER.debug("Check product health: " + product);
        if (urls == null || urls.length == 0) {
            results.add(new Result(product, null, urls));
        }
        for (String url : urls) {
            try {
                WebTarget r = client.target(url);
                Response response;
                switch (verb.toLowerCase()) {
                case "get":
                    response = r.request().accept(MediaType.APPLICATION_JSON).get();
                    if (proxy) {
                        results.addAll(parseResponse(response));
                    } else {
                        results.add(parseResponse(response.getStatus(), getVersion(response), getPduiVersion(response), response.readEntity(String.class)));
                    }
                    break;
                case "head":
                    response = r.request().accept(MediaType.APPLICATION_JSON).head();
                    results.add(parseResponse(response.getStatus(), getVersion(response), getPduiVersion(response), response.readEntity(String.class)));
                    break;
                default:
                    response = r.request().accept(MediaType.APPLICATION_JSON).head();
                    results.add(parseResponse(response.getStatus(), getVersion(response), getPduiVersion(response),""));
                    break;
                }
            } catch (Exception e) {
                LOGGER.warn(String.format("Exception (%s) during healthcheck inspection for: %s.", e.getMessage(), url));
                results.add(new Result(product, e.getMessage(), Boolean.FALSE, urls));
            }
        }
        return results;
    }

    private String getVersion(Response response) {
        String result = null;
        if (null != response ) {
            Object x_version = response.getHeaders().getFirst("X-Version");
            Object foundVersion = (null != x_version) ? x_version : response.getHeaders().getFirst("Version");
            if(null != foundVersion) {
                result = foundVersion.toString();
            }
        }
        return result;
    }

    private String getPduiVersion(Response response) {
        String result = null;
        if (null != response ) {
            Object pduiVersion = response.getHeaders().getFirst(PDUI_HEADER);
            if(null != pduiVersion) {
                result = pduiVersion.toString();
            }
        }
        return result;
    }

    private List<Result> parseResponse(Response response) throws IOException {
        if (200 == response.getStatus()) {
            List<Result> results = new ArrayList<>();
            results.addAll(new ObjectMapper().readValue(response.readEntity(String.class),  new TypeReference<List<Result>>() {
            }));
            results.add(new Result(product, "", Boolean.TRUE, getVersion(response), getPduiVersion(response), new Date(), urls));
            return results;
        } else {
            LOGGER.warn(String.format("Unexpected status code [%s].", response.getStatus()));
            List<Result> results = new ArrayList<>();
            results.add(new Result(product, response.readEntity(String.class), Boolean.FALSE, urls));
            return results;
        }
    }

    private Result parseResponse(int status, String version, String content) {
        return parseResponse(status, version, null, content);
    }

    private Result parseResponse(int status, String version, String pduiVersion, String content) {
        if (200 == status) {
            return new Result(product, "", Boolean.TRUE, version, pduiVersion, new Date(), urls);
        } else {
            LOGGER.info(String.format("Healthcheck status code != 200 for: %s (status code: %s)", urls.toString(), status));
            return new Result(product, content, Boolean.FALSE, version, new Date(), urls);
        }
    }

    @Override
    public void run() {
        results = findInCacheOrCompute();
    }
}
