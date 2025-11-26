package com.example.http;

public interface Router {

    HttpResponse route(HttpRequest request) throws Exception;

}
