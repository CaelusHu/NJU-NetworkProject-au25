package com.example.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求数据对象
 * 存储解析后的HTTP请求信息
 */
public class HttpRequest {
    private String method;
    private String path;
    private String protocol;
    private Map<String, String> headers;
    private String body;
    private Map<String, String> parameters;

    public HttpRequest() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
    }

    // Getters and Setters
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name.toLowerCase(), value);
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", protocol='" + protocol + '\'' +
                ", headers=" + headers +
                ", bodyLength=" + (body != null ? body.length() : 0) +
                ", parameters=" + parameters +
                '}';
    }
}