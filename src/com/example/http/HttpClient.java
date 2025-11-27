package com.example.http;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * HTTP客户端：基于Socket发送GET/POST请求，处理响应，支持301/302重定向和304缓存。
 * 支持命令行交互，演示注册/登录。
 */
public class HttpClient {
    // 本地服务器主机和端口
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    // 最大重定向次数，防止循环
    private static final int MAX_REDIRECTS = 3;
    // 简单缓存：路径 -> (ETag/Last-Modified, body)
    private static final Map<String, Map<String, String>> cache = new HashMap<>();

    public static void main(String[] args) {
        // 命令行交互循环
        Scanner scanner = new Scanner(System.in);
        System.out.println("HTTP Client started. Commands: get /path, post /path param1=value1&param2=value2, register username password, login username password, exit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                // 解析命令
                String[] parts = input.split(" ", 3);
                String command = parts[0].toLowerCase();
                String path;
                String params = "";

                if (command.equals("get") && parts.length >= 2) {
                    path = parts[1];
                    sendRequest("GET", path, "", 0);  // 发送GET请求
                } else if (command.equals("post") && parts.length >= 3) {
                    path = parts[1];
                    params = parts[2];
                    sendRequest("POST", path, params, 0);  // 发送POST请求
                } else if (command.equals("register") && parts.length >= 3) {
                    String username = parts[1];
                    String password = parts[2];
                    params = "username=" + username + "&password=" + password;
                    sendRequest("POST", "/register", params, 0);  // 注册POST
                } else if (command.equals("login") && parts.length >= 3) {
                    String username = parts[1];
                    String password = parts[2];
                    params = "username=" + username + "&password=" + password;
                    sendRequest("POST", "/login", params, 0);  // 登录POST
                } else {
                    System.out.println("Invalid command.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Client stopped.");
    }

    /**
     * 发送HTTP请求并处理响应（递归处理重定向）。
     * @param method GET或POST
     * @param path 路径
     * @param params 查询参数或表单body（格式param1=value1&param2=value2）
     * @param redirectCount 当前重定向次数
     */
    private static void sendRequest(String method, String path, String params, int redirectCount) throws IOException {
        // 检查重定向次数上限
        if (redirectCount > MAX_REDIRECTS) {
            System.out.println("Too many redirects.");
            return;
        }

        // 建立Socket连接
        try (Socket socket = new Socket(HOST, PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            // 构建请求报文
            StringBuilder request = new StringBuilder();
            String fullPath = path;
            if (method.equals("GET") && !params.isEmpty()) {
                fullPath += "?" + params;  // GET参数附加到路径
            }
            request.append(method).append(" ").append(fullPath).append(" HTTP/1.1\r\n");
            request.append("Host: ").append(HOST).append("\r\n");
            request.append("Connection: keep-alive\r\n");  // 支持长连接
            request.append("User-Agent: SimpleHttpClient\r\n");
            request.append("Accept: */*\r\n");

            // 添加缓存头（If-None-Match 或 If-Modified-Since）
            Map<String, String> cachedEntry = cache.get(path);
            if (cachedEntry != null) {
                String etag = cachedEntry.get("ETag");
                String lastModified = cachedEntry.get("Last-Modified");
                if (etag != null) {
                    request.append("If-None-Match: ").append(etag).append("\r\n");
                } else if (lastModified != null) {
                    request.append("If-Modified-Since: ").append(lastModified).append("\r\n");
                }
            }

            if (method.equals("POST") && !params.isEmpty()) {
                request.append("Content-Type: application/x-www-form-urlencoded\r\n");
                request.append("Content-Length: ").append(params.getBytes("UTF-8").length).append("\r\n");
            }
            request.append("\r\n");

            // 发送请求
            out.write(request.toString().getBytes("UTF-8"));
            if (method.equals("POST") && !params.isEmpty()) {
                out.write(params.getBytes("UTF-8"));
            }
            out.flush();

            // 读取响应
            Map<String, String> responseHeaders = new HashMap<>();
            String responseLine = readLine(in);
            if (responseLine == null) {
                System.out.println("No response.");
                return;
            }

            // 解析响应行
            String[] statusParts = responseLine.split(" ", 3);
            int statusCode = Integer.parseInt(statusParts[1]);
            String reason = statusParts[2];

            // 读取响应头
            String headerLine;
            while ((headerLine = readLine(in)) != null && !headerLine.isEmpty()) {
                int colon = headerLine.indexOf(":");
                if (colon > 0) {
                    String key = headerLine.substring(0, colon).trim();
                    String value = headerLine.substring(colon + 1).trim();
                    responseHeaders.put(key.toLowerCase(), value);
                }
            }

            // 读取body
            int contentLength = 0;
            if (responseHeaders.containsKey("content-length")) {
                contentLength = Integer.parseInt(responseHeaders.get("content-length"));
            }
            byte[] body = readBytes(in, contentLength);
            String bodyStr = new String(body, "UTF-8");

            // 处理状态码
            if (statusCode == 301 || statusCode == 302) {
                String newLocation = responseHeaders.get("location");
                if (newLocation != null) {
                    System.out.println("Redirecting to: " + newLocation);
                    sendRequest(method, newLocation, params, redirectCount + 1);  // 递归重定向
                    return;
                }
            } else if (statusCode == 304) {
                if (cachedEntry != null) {
                    System.out.println("304 Not Modified - Using cache.");
                    bodyStr = cachedEntry.get("body");  // 使用缓存body
                } else {
                    System.out.println("304 but no cache available.");
                }
            } else if (statusCode == 200) {
                // 更新缓存
                Map<String, String> newCache = new HashMap<>();
                String etag = responseHeaders.get("etag");
                String lastModified = responseHeaders.get("last-modified");
                if (etag != null) {
                    newCache.put("ETag", etag);
                } else if (lastModified != null) {
                    newCache.put("Last-Modified", lastModified);
                }
                newCache.put("body", bodyStr);
                String cacheControl = responseHeaders.get("cache-control");
                if (cacheControl != null && cacheControl.contains("no-cache")) {
                    cache.remove(path);  // 如果no-cache，不存储
                } else {
                    cache.put(path, newCache);
                }
            }

            // 显示响应
            System.out.println("Status: " + statusCode + " " + reason);
            System.out.println("Headers: " + responseHeaders);
            String contentType = responseHeaders.getOrDefault("content-type", "text/plain");
            if (contentType.startsWith("text/")) {
                System.out.println("Body: " + bodyStr);
            } else if (contentType.startsWith("image/")) {
                System.out.println("Body: Binary image data (" + body.length + " bytes)");
                // 保存到文件以证明非文本支持
                String extension = contentType.split("/")[1];
                try (FileOutputStream fos = new FileOutputStream("received_image." + extension)) {
                    fos.write(body);
                    System.out.println("Image saved to received_image." + extension);
                } catch (IOException e) {
                    System.out.println("Failed to save image: " + e.getMessage());
                }
            } else if (contentType.startsWith("application/json")) {
                System.out.println("JSON Body: " + bodyStr);  // 格式化打印JSON
            } else {
                System.out.println("Body: " + bodyStr);
            }

            // 错误处理不友好
            if (statusCode >= 400) {
                System.out.println("Error occurred: " + reason);
            }
        }
    }

    // 读取一行
    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int b = in.read();
            if (b == -1) {
                if (buf.size() == 0) return null;
                break;
            }
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                buf.write(b);
            }
            previous = b;
        }
        return buf.toString("UTF-8");
    }

    // 读取固定长度bytes
    private static byte[] readBytes(InputStream in, int length) throws IOException {
        byte[] data = new byte[length];
        int read = 0;
        while (read < length) {
            int r = in.read(data, read, length - read);
            if (r == -1) throw new EOFException("Unexpected EOF");
            read += r;
        }
        return data;
    }
}