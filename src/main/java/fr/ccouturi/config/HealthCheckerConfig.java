package fr.ccouturi.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthCheckerConfig {

    @JsonProperty
    private String name;

    @JsonProperty
    private String verb = "head";

    @JsonProperty
    private String[] url;

    @JsonProperty
    private boolean proxy = false;

    @JsonProperty
    private int timeout = 5000;

    private HealthCheckerConfig() {
        // Ok for introspection
    }

    public HealthCheckerConfig(String name, String... url) {
        super();
        setName(name);
        setUrl(url);
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonIgnore
    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String[] getUrl() {
        return url;
    }

    @JsonIgnore
    public void setUrl(String[] url) {
        this.url = url;
    }

    @JsonIgnore
    public String getVerb() {
        return verb;
    }

    @JsonIgnore
    public void setVerb(String verb) {
        this.verb = verb;
    }

    @JsonIgnore
    public boolean isProxy() {
        return proxy;
    }

    @JsonIgnore
    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    @JsonIgnore
    public int getTimeout() {
        return timeout;
    }

    @JsonIgnore
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
