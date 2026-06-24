package com.battery.config;

import com.battery.entity.LoginResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthTokenStore {

    private static final Map<String, LoginResponse> TOKEN_STORE = new ConcurrentHashMap<>();

    public static void put(String token, LoginResponse user) {
        TOKEN_STORE.put(token, user);
    }

    public static LoginResponse get(String token) {
        return TOKEN_STORE.get(token);
    }

    public static boolean isValid(String token) {
        return token != null && TOKEN_STORE.containsKey(token);
    }

    public static void remove(String token) {
        TOKEN_STORE.remove(token);
    }
}
