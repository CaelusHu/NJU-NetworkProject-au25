package com.example.http;

/**
 * UserService Unit Test
 * Test core functionality of user service independently
 */
public class TestUserService {
    public static void main(String[] args) {
        UserService userService = new UserService();
        
        System.out.println("=== UserService Unit Test ===");
        System.out.println();
        
        // Test 1: Register new user
        System.out.println("1. Test registering new user:");
        boolean regResult1 = userService.register("testuser", "password123");
        System.out.println("   Result: " + (regResult1 ? "Success" : "Failed"));
        System.out.println("   Expected: Success");
        System.out.println();
        
        // Test 2: Register existing user
        System.out.println("2. Test registering existing user:");
        boolean regResult2 = userService.register("testuser", "password456");
        System.out.println("   Result: " + (regResult2 ? "Success" : "Failed"));
        System.out.println("   Expected: Failed (username already exists)");
        System.out.println();
        
        // Test 3: Check username existence
        System.out.println("3. Test checking username existence:");
        boolean exists1 = userService.isUsernameExists("testuser");
        boolean exists2 = userService.isUsernameExists("nonexistent");
        System.out.println("   testuser: " + (exists1 ? "Exists" : "Not exists"));
        System.out.println("   nonexistent: " + (exists2 ? "Exists" : "Not exists"));
        System.out.println("   Expected: testuser exists, nonexistent not exists");
        System.out.println();
        
        // Test 4: User login (correct password)
        System.out.println("4. Test user login (correct password):");
        boolean loginResult1 = userService.login("testuser", "password123");
        System.out.println("   Result: " + (loginResult1 ? "Success" : "Failed"));
        System.out.println("   Expected: Success");
        System.out.println();
        
        // Test 5: User login (wrong password)
        System.out.println("5. Test user login (wrong password):");
        boolean loginResult2 = userService.login("testuser", "wrongpassword");
        System.out.println("   Result: " + (loginResult2 ? "Success" : "Failed"));
        System.out.println("   Expected: Failed");
        System.out.println();
        
        // Test 6: Get user count
        System.out.println("6. Test getting user count:");
        int userCount = userService.getUserCount();
        System.out.println("   Current user count: " + userCount);
        System.out.println("   Expected: 1");
        System.out.println();
        
        // Test 7: Batch registration
        System.out.println("7. Test batch registration:");
        String[] usersToRegister = {"user1", "user2", "user3"};
        for (String user : usersToRegister) {
            boolean result = userService.register(user, "pass123");
            System.out.println("   Register " + user + ": " + (result ? "Success" : "Failed"));
        }
        System.out.println("   Final user count: " + userService.getUserCount());
        System.out.println("   Expected: 4");
        System.out.println();
        
        System.out.println("=== Unit Test Completed ===");
        System.out.println("UserService functionality: " + (verifyResults() ? "Normal" : "Abnormal"));
    }
    
    private static boolean verifyResults() {
        UserService userService = new UserService();
        // Clean up previous test data
        userService = new UserService();
        
        // Verify new user registration
        if (!userService.register("verifyuser", "verifypass")) {
            return false;
        }
        
        // Verify username existence check
        if (!userService.isUsernameExists("verifyuser")) {
            return false;
        }
        
        // Verify login functionality
        if (!userService.login("verifyuser", "verifypass")) {
            return false;
        }
        
        // Verify user count
        if (userService.getUserCount() != 1) {
            return false;
        }
        
        return true;
    }
}