package com.example.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * HTTP请求解析器
 * 专门负责解析HTTP请求报文
 * 遵循README要求：模块分离，不耦合逻辑
 */
public class HttpRequestParser {

    /**
     * 解析HTTP请求（支持String输入）
     * 
     * @param requestString HTTP请求字符串
     * @return 解析后的HttpRequest对象
     * @throws IOException 如果解析失败
     */
    public static HttpRequest parse(String requestString) throws IOException {
        if (requestString == null || requestString.isEmpty()) {
            throw new IOException("请求字符串不能为空");
        }

        // 将String转换为BufferedReader
        BufferedReader reader = new BufferedReader(
                new StringReader(requestString));

        HttpRequest request = new HttpRequest();

        // 1. 解析请求行
        parseRequestLine(reader, request);

        // 2. 解析请求头
        parseHeaders(reader, request);

        // 3. 解析请求体（如果有）
        parseBody(reader, request);

        // 4. 解析查询参数（如果有）
        parseQueryParameters(request);

        return request;
    }

    /**
     * 解析HTTP请求（支持InputStream输入）
     * 
     * @param inputStream 客户端输入流
     * @return 解析后的HttpRequest对象
     * @throws IOException 如果解析失败
     */
    public static HttpRequest parse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        return parse(reader);
    }

    /**
     * 内部解析方法，接受BufferedReader
     */
    private static HttpRequest parse(BufferedReader reader) throws IOException {
        HttpRequest request = new HttpRequest();

        // 1. 解析请求行
        parseRequestLine(reader, request);

        // 2. 解析请求头
        parseHeaders(reader, request);

        // 3. 解析请求体（如果有）
        parseBody(reader, request);

        // 4. 解析查询参数（如果有）
        parseQueryParameters(request);

        return request;
    }

    /**
     * 解析请求行
     */
    private static void parseRequestLine(BufferedReader reader, HttpRequest request) throws IOException {
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            throw new IOException("无效的请求行");
        }

        String[] parts = line.split(" ", 3);
        if (parts.length < 2) {
            throw new IOException("请求行格式错误: " + line);
        }

        request.setMethod(parts[0].toUpperCase());
        request.setPath(parts[1]);
        request.setProtocol(parts.length > 2 ? parts[2] : "HTTP/1.1");
    }

    /**
     * 解析请求头
     */
    private static void parseHeaders(BufferedReader reader, HttpRequest request) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                request.addHeader(name, value);
            }
        }
    }

    /**
     * 解析请求体
     */
    private static void parseBody(BufferedReader reader, HttpRequest request) throws IOException {
        String contentLengthHeader = request.getHeader("Content-Length");
        if (contentLengthHeader != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthHeader);
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    int bytesRead = reader.read(bodyChars, 0, contentLength);
                    if (bytesRead == contentLength) {
                        request.setBody(new String(bodyChars));
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略无效的Content-Length
            }
        }
    }

    /**
     * 解析查询参数
     */
    private static void parseQueryParameters(HttpRequest request) {
        String path = request.getPath();
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            String queryString = path.substring(queryIndex + 1);
            parseQueryString(queryString, request);
        }

        // 如果是POST请求且Content-Type是表单类型，解析请求体中的参数
        String method = request.getMethod();
        String contentType = request.getHeader("Content-Type");
        String body = request.getBody();

        if ("POST".equals(method) && body != null && body.length() > 0) {
            if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                parseQueryString(body, request);
            }
        }
    }

    /**
     * 解析查询字符串
     */
    private static void parseQueryString(String queryString, HttpRequest request) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                request.addParameter(key, value);
            }
        }
    }

    /**
     * 辅助方法：创建模拟的HTTP请求字符串
     * 用于测试和示例
     */
    public static String createSampleRequestString() {
        return "GET /index.html?param1=value1&param2=value2 HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: curl/7.68.0\r\n" +
                "Accept: */*\r\n" +
                "Connection: keep-alive\r\n" +
                "\r\n";
    }

    /**
     * 辅助方法：创建模拟的POST请求字符串
     * 用于测试和示例
     */
    public static String createSamplePostRequestString() {
        return "POST /register HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "User-Agent: curl/7.68.0\r\n" +
                "Accept: */*\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Content-Length: 27\r\n" +
                "Connection: keep-alive\r\n" +
                "\r\n" +
                "username=testuser&password=testpass";
    }
}