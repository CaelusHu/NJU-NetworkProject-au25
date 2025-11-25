package com.example.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/**
 * 完整路由实现
 * C部分：路由分发 + 注册登录业务逻辑
 */
public class CompleteRouter implements Router {
    private final UserService userService;

    public CompleteRouter() {
        this.userService = new UserService();
    }

    @Override
    public HttpResponse route(HttpRequest request) {
        try {
            String method = request.getMethod();
            String path = request.getPath();

            System.out.println("handle request:  " + method + " " + path);

            // 路由分发
            if ("GET".equals(method)) {
                return handleGetRequest(path, request);
            } else if ("POST".equals(method)) {
                return handlePostRequest(path, request);
            } else {
                return HttpResponse.methodNotAllowed();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.internalServerError();
        }
    }
    
    /**
     * 处理GET请求
     */
    private HttpResponse handleGetRequest(String path, HttpRequest request) {
        if ("/".equals(path) || "/index".equals(path)) {
            return homePage();
        } else if ("/user/count".equals(path)) {
            return userCountPage();
        } else {
            return HttpResponse.notFound();
        }
    }

    /**
     * 处理POST请求
     */
    private HttpResponse handlePostRequest(String path, HttpRequest request) {
        if ("/register".equals(path)) {
            return handleRegister(request);
        } else if ("/login".equals(path)) {
            return handleLogin(request);
        } else {
            return HttpResponse.notFound();
        }
    }

    /**
     * 处理用户注册
     */
    private HttpResponse handleRegister(HttpRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            return HttpResponse.badRequest("username and password can not be empty");
        }

        if (userService.isUsernameExists(username)) {
            return HttpResponse.conflict();
        }

        boolean success = userService.register(username, password);
        if (success) {
            return HttpResponse.okText("Welcome back " + username);
        } else {
            return HttpResponse.internalServerError();
        }
    }

    /**
     * 处理用户登录
     */
    private HttpResponse handleLogin(HttpRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            return HttpResponse.badRequest("username and password can not be empty");
        }

        boolean success = userService.login(username, password);
        if (success) {
            return HttpResponse.okText("Welcome back " + username);
        } else {
            return HttpResponse.unauthorized();
        }
    }

     /**
     * 首页
     */
    private HttpResponse homePage() {
        String html = "<html><body>" +
                     "<h1>HTTP服务器 - 注册登录系统</h1>" +
                     "<p>用户总数: " + userService.getUserCount() + "</p>" +
                     "<h2>API接口:</h2>" +
                     "<ul>" +
                     "<li>POST /register - 用户注册 (参数: username, password)</li>" +
                     "<li>POST /login - 用户登录 (参数: username, password)</li>" +
                     "<li>GET /user/count - 获取用户总数</li>" +
                     "</ul>" +
                     "<h2>架构说明:</h2>" +
                     "<p>遵循模块分离原则，解析和业务逻辑分开处理</p>" +
                     "</body></html>";
        
        HttpResponse response = new HttpResponse(200, "OK");
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
        return response;
    }
    
    /**
     * 用户数量页面
     */
    private HttpResponse userCountPage() {
        return HttpResponse.okText("Current user counts: " + userService.getUserCount());
    }
}
