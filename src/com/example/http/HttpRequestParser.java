package com.example.http;

import java.io.IOException;
import java.io.InputStream;


/**
 * HTTP请求解析器
 * 专门负责解析HTTP请求报文
 * 遵循README要求：模块分离，不耦合逻辑
 */
public class HttpRequestParser {

    /**
     * 解析HTTP请求
     * 
     * @param inputStream 客户端输入流
     * @return 解析后的HttpRequest对象
     * @throws IOException 如果解析失败
     */
    public static HttpRequest parse(String rawRequest) throws IOException {
        HttpRequest request = new HttpRequest();
        //TODO 解析输入返回HttpRequest对象

        return request;
    }
}