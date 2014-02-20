package fr.ccouturi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {

    @JsonProperty
    private String product;

    @JsonProperty
    private Boolean status;

    @JsonProperty
    public String[] urls;

    public Result(String product, Boolean status, String... urls) {
        this.product = product;
        this.status = status;
        this.urls = urls;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("product: " + getProduct());
        result.append(", status: " + status);
        result.append(", urls" + urls);
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

}
