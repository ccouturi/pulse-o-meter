package fr.ccouturi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class Result {

    @JsonProperty
    private String product;

    @JsonProperty
    private Boolean status;

    @JsonProperty
    public String[] urls;

    @JsonProperty
    private String content;

    @JsonProperty
    private String version;

    @JsonProperty("check_date")
    private Date checkDate = new Date();

    private Result() {
        // for introspection
    }

    public Result(String product, Boolean status, String... urls) {
        this.product = product;
        this.status = status;
        this.urls = urls;
    }

    public Result(String product, String content, Boolean status, String... urls) {
        this.product = product;
        this.status = status;
        this.content = content;
        this.urls = urls;
    }

    public Result(String product, String content, Boolean status, String version, Date date, String... urls) {
        this.product = product;
        this.status = status;
        this.content = content;
        this.version = version;
        this.urls = urls;
        this.checkDate = date;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("product: " + getProduct());
        result.append(", status: " + status);
        result.append(", urls: " + urls);
        result.append(", version: "+version);
        result.append(", content: "+content);
        return result.toString();
    }

    @JsonIgnore
    public String getProduct() {
        return product;
    }

    @JsonIgnore
    public Boolean getStatus() {
        return status;
    }

    @JsonIgnore
    public String[] getUrls() {
        return urls;
    }

    @JsonIgnore
    public String getContent() {
        return content;
    }

    @JsonIgnore
    public String getVersion() {
        return version;
    }

    @JsonIgnore
    public Date getCheckDate() {
        return checkDate;
    }

}
