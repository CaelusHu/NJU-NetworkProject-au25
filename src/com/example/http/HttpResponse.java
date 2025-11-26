package com.example.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private String reason;
    private Map<String,String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse(int statusCode, String reason) {
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public void setBody(byte[] body) {
        this.body = body;
        this.headers.put("Content-Length", String.valueOf(body.length));
    }

    public void setHeader(String k, String v) { headers.put(k, v); }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(reason).append("\r\n");

        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));

        sb.append("\r\n");
        byte[] head = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] res = new byte[head.length + body.length];
        System.arraycopy(head, 0, res, 0, head.length);
        System.arraycopy(body, 0, res, head.length, body.length);
        return res;
    }
    
    // Getter methods for testing
    public int getStatusCode() {
        return statusCode;
    }

    public byte[] getBody() {
        return body;
    }

    public String getReason() {
        return reason;
    }

    public static HttpResponse okText(String text) {
        HttpResponse r = new HttpResponse(200, "OK");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody(text.getBytes(StandardCharsets.UTF_8));
        return r;
    }

    public static HttpResponse notFound() {
        HttpResponse r = new HttpResponse(404, "Not Found");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody("404 Not Found".getBytes(StandardCharsets.UTF_8));
        return r;
    }

    public static HttpResponse methodNotAllowed() {
        HttpResponse r = new HttpResponse(405, "Method Not Allowed");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody("405 Method Not Allowed".getBytes(StandardCharsets.UTF_8));
        return r;
    }

    public static HttpResponse internalServerError() {
        HttpResponse r = new HttpResponse(500, "Internal Server Error");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody("500 Internal Server Error".getBytes(StandardCharsets.UTF_8));
        return r;
    }

    //新增一些注册登录会用到的状态码
    public static HttpResponse created() {
        HttpResponse r = new HttpResponse(201, "Created");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody("Created".getBytes(StandardCharsets.UTF_8));
        return r;
    }

    public static HttpResponse badRequest() {
        return badRequest("Bad Request");
    }

    public static HttpResponse badRequest(String message) {
        HttpResponse r = new HttpResponse(400, "Bad Request");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody(message.getBytes(StandardCharsets.UTF_8));
        return r;
    }

    public static HttpResponse unauthorized() {
        HttpResponse r = new HttpResponse(401, "Unauthorized");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody("Unauthorized".getBytes(StandardCharsets.UTF_8));
        return r;
    }

    public static HttpResponse conflict() {
        HttpResponse r = new HttpResponse(409, "Conflict");
        r.setHeader("Content-Type", "text/plain; charset=utf-8");
        r.setBody("Conflict".getBytes(StandardCharsets.UTF_8));
        return r;
    }

    // ==================== 新增：重定向状态码 ====================

    /**
     * 301 永久重定向
     */
    public static HttpResponse movedPermanently(String newLocation) {
        HttpResponse r = new HttpResponse(301, "Moved Permanently");
        r.setHeader("Location", newLocation);
        r.setHeader("Content-Type", "text/html; charset=utf-8");
        r.setBody(("<html><body>Moved permanently to <a href=\"" + newLocation + "\">" + newLocation + "</a></body></html>").getBytes(StandardCharsets.UTF_8));
        return r;
    }

    /**
     * 302 临时重定向
     */
    public static HttpResponse found(String newLocation) {
        HttpResponse r = new HttpResponse(302, "Found");
        r.setHeader("Location", newLocation);
        r.setHeader("Content-Type", "text/html; charset=utf-8");
        r.setBody(("<html><body>Found at <a href=\"" + newLocation + "\">" + newLocation + "</a></body></html>").getBytes(StandardCharsets.UTF_8));
        return r;
    }

    /**
     * 304 未修改（用于缓存）
     */
    public static HttpResponse notModified() {
        HttpResponse r = new HttpResponse(304, "Not Modified");
        // 304 响应不应包含body
        return r;
    }

    // ==================== 新增：非文本MIME类型支持 ====================

    /**
     * 返回JSON响应
     */
    public static HttpResponse okJson(String json) {
        HttpResponse r = new HttpResponse(200, "OK");
        r.setHeader("Content-Type", "application/json; charset=utf-8");
        r.setBody(json.getBytes(StandardCharsets.UTF_8));
        return r;
    }

    /**
     * 返回图片响应（非文本类型）
     */
    public static HttpResponse okImage(byte[] imageData, String imageType) {
        HttpResponse r = new HttpResponse(200, "OK");
        r.setHeader("Content-Type", "image/" + imageType); // 如 image/png, image/jpeg
        r.setBody(imageData);
        return r;
    }

    /**
     * 返回二进制文件响应
     */
    public static HttpResponse okBinary(byte[] data, String filename) {
        HttpResponse r = new HttpResponse(200, "OK");
        r.setHeader("Content-Type", "application/octet-stream");
        r.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        r.setBody(data);
        return r;
    }

    // ==================== 辅助：MIME类型工具方法 ====================

    /**
     * 根据文件扩展名获取MIME类型
     */
    public static String getMimeType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    /**
     * 设置ETag头（用于缓存验证）
     */
    public void setETag(String etag) {
        setHeader("ETag", "\"" + etag + "\"");
    }

    /**
     * 设置Last-Modified头
     */
    public void setLastModified(String lastModified) {
        setHeader("Last-Modified", lastModified);
    }
}
