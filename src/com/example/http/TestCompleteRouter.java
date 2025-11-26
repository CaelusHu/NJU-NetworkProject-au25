package com.example.http;

import java.util.HashMap;
import java.util.Map;

/**
 * CompleteRouter Integration Test
 * Test collaboration between router and business logic
 */
public class TestCompleteRouter {
    public static void main(String[] args) {
        CompleteRouter router = new CompleteRouter();
        
        System.out.println("=== CompleteRouter Integration Test ===");
        System.out.println();
        
        // Test 1: Home page routing
        System.out.println("1. Test home page routing:");
        HttpRequest homeRequest = createHttpRequest("GET", "/");
        HttpResponse homeResponse = router.route(homeRequest);
        System.out.println("   Status code: " + homeResponse.getStatusCode());
        System.out.println("   Response length: " + homeResponse.getBody().length + " bytes");
        System.out.println("   Expected: 200 OK, returns HTML page");
        System.out.println();
        
        // Test 2: User registration
        System.out.println("2. Test user registration:");
        HttpRequest registerRequest = createPostRequest("/register", 
            new HashMap<String, String>() {{
                put("username", "testrouter");
                put("password", "routerpass");
            }});
        HttpResponse registerResponse = router.route(registerRequest);
        System.out.println("   Status code: " + registerResponse.getStatusCode());
        System.out.println("   Response content: " + new String(registerResponse.getBody()));
        System.out.println("   Expected: 200 OK, registration success message");
        System.out.println();
        
        // Test 3: Duplicate registration
        System.out.println("3. Test duplicate registration:");
        HttpRequest duplicateRegisterRequest = createPostRequest("/register", 
            new HashMap<String, String>() {{
                put("username", "testrouter");
                put("password", "anotherpass");
            }});
        HttpResponse duplicateResponse = router.route(duplicateRegisterRequest);
        System.out.println("   Status code: " + duplicateResponse.getStatusCode());
        System.out.println("   Response content: " + new String(duplicateResponse.getBody()));
        System.out.println("   Expected: 409 Conflict, username already exists");
        System.out.println();
        
        // Test 4: User login
        System.out.println("4. Test user login:");
        HttpRequest loginRequest = createPostRequest("/login", 
            new HashMap<String, String>() {{
                put("username", "testrouter");
                put("password", "routerpass");
            }});
        HttpResponse loginResponse = router.route(loginRequest);
        System.out.println("   Status code: " + loginResponse.getStatusCode());
        System.out.println("   Response content: " + new String(loginResponse.getBody()));
        System.out.println("   Expected: 200 OK, login success message");
        System.out.println();
        
        // Test 5: Login failure
        System.out.println("5. Test login failure:");
        HttpRequest failedLoginRequest = createPostRequest("/login", 
            new HashMap<String, String>() {{
                put("username", "testrouter");
                put("password", "wrongpass");
            }});
        HttpResponse failedLoginResponse = router.route(failedLoginRequest);
        System.out.println("   Status code: " + failedLoginResponse.getStatusCode());
        System.out.println("   Response content: " + new String(failedLoginResponse.getBody()));
        System.out.println("   Expected: 401 Unauthorized, login failed");
        System.out.println();
        
        // Test 6: User count statistics
        System.out.println("6. Test user count statistics:");
        HttpRequest countRequest = createHttpRequest("GET", "/user/count");
        HttpResponse countResponse = router.route(countRequest);
        System.out.println("   Status code: " + countResponse.getStatusCode());
        System.out.println("   Response content: " + new String(countResponse.getBody()));
        System.out.println("   Expected: 200 OK, user count");
        System.out.println();
        
        // Test 7: 404 Not Found
        System.out.println("7. Test 404 Not Found:");
        HttpRequest notFoundRequest = createHttpRequest("GET", "/nonexistent");
        HttpResponse notFoundResponse = router.route(notFoundRequest);
        System.out.println("   Status code: " + notFoundResponse.getStatusCode());
        System.out.println("   Response content: " + new String(notFoundResponse.getBody()));
        System.out.println("   Expected: 404 Not Found");
        System.out.println();
        
        System.out.println("=== Integration Test Completed ===");
        System.out.println("CompleteRouter functionality: " + (verifyRouterFunctionality() ? "Normal" : "Abnormal"));
    }
    
    /**
     * Create a mock HttpRequest object
     */
    private static HttpRequest createHttpRequest(String method, String path) {
        HttpRequest request = new HttpRequest();
        request.setMethod(method);
        request.setPath(path);
        request.setProtocol("HTTP/1.1");
        return request;
    }
    
    /**
     * Create a mock POST request
     */
    private static HttpRequest createPostRequest(String path, Map<String, String> parameters) {
        HttpRequest request = createHttpRequest("POST", path);
        
        // Set Content-Type for POST request
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        
        // Add parameters
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            request.addParameter(entry.getKey(), entry.getValue());
        }
        
        return request;
    }
    
    /**
     * Verify if router functionality is normal
     */
    private static boolean verifyRouterFunctionality() {
        CompleteRouter router = new CompleteRouter();
        
        // Test home page
        HttpRequest homeRequest = createHttpRequest("GET", "/");
        HttpResponse homeResponse = router.route(homeRequest);
        if (homeResponse.getStatusCode() != 200) {
            return false;
        }
        
        // Test registration and login flow
        HttpRequest registerRequest = createPostRequest("/verify", 
            new HashMap<String, String>() {{
                put("username", "verifyrouter");
                put("password", "verifypass");
            }});
        HttpResponse registerResponse = router.route(registerRequest);
        if (registerResponse.getStatusCode() != 200) {
            return false;
        }
        
        HttpRequest loginRequest = createPostRequest("/login", 
            new HashMap<String, String>() {{
                put("username", "verifyrouter");
                put("password", "verifypass");
            }});
        HttpResponse loginResponse = router.route(loginRequest);
        if (loginResponse.getStatusCode() != 200) {
            return false;
        }
        
        return true;
    }
}