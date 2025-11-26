package com.example.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务类
 * C部分：注册登录业务逻辑（内存存储）
 */
public class UserService {
    private static final Map<String, String> users = new ConcurrentHashMap<>();

    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @return 注册结果：true表示成功，false表示用户名已存在
     */
    public boolean register(String username, String password) {
        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            return false;
        }

        // 如果用户名不存在，则添加
        return users.putIfAbsent(username, password) == null;
    }

    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果：true表示成功，false表示用户名或密码错误
     */
    public boolean login(String username, String password) {
        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            return false;
        }

        // 验证用户名和密码
        String storedPassword = users.get(username);
        return password.equals(storedPassword);
    }

    /**
     * 获取用户数量
     * 
     * @return 用户总数
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return true表示已存在，false表示不存在
     */
    public boolean isUsernameExists(String username) {
        return users.containsKey(username);
    }
}
