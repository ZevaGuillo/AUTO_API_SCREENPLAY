package com.ticketing.config;

/**
 * Static holder for JWT tokens used across tests.
 */
public final class AuthTokenHolder {

    private static volatile String adminToken;
    private static volatile String userToken;

    private AuthTokenHolder() {}

    public static String getAdminToken() {
        return adminToken;
    }

    public static void setAdminToken(String token) {
        adminToken = token;
    }

    public static String getUserToken() {
        return userToken;
    }

    public static void setUserToken(String token) {
        userToken = token;
    }
}
