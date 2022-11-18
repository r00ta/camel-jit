package com.redhat.developer.r00ta.camel.jit.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JITMessage {

    @JsonProperty("body")
    private String body;

    @JsonProperty("headers")
    private Map<String, Object> headers;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
