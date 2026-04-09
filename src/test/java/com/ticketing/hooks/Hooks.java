package com.ticketing.hooks;

import com.ticketing.config.AuthTokenHolder;
import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.actors.OnStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final String DEFAULT_BASE_URL = "http://localhost:5000";
    private static final String ADMIN_EMAIL = "admin-test@test.com";
    private static final String ADMIN_PASSWORD = "AdminTest1234!";
    private static volatile boolean adminSeeded = false;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Before(order = 0)
    public void setTheStage() {
        String baseUrl = System.getProperty("baseUrl", System.getenv().getOrDefault("BASE_URL", DEFAULT_BASE_URL));
        OnStage.setTheStage(
            Cast.whereEveryoneCan(
                CallAnApi.at(baseUrl)
            )
        );

        if (!adminSeeded) {
            seedAdminToken(baseUrl);
            adminSeeded = true;
        }
    }

    private void seedAdminToken(String baseUrl) {
        String authBase = baseUrl + "/auth";

        // 1. Register admin user (idempotent — 400 if already exists)
        String registerBody = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"role\":\"Admin\"}",
                ADMIN_EMAIL, ADMIN_PASSWORD);
        try {
            HttpRequest regReq = HttpRequest.newBuilder()
                    .uri(URI.create(authBase + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> regResp = HTTP.send(regReq, HttpResponse.BodyHandlers.ofString());
            logger.info("Admin register: HTTP {} — {}", regResp.statusCode(), regResp.body());
        } catch (Exception e) {
            logger.warn("Could not register admin user: {}", e.getMessage());
        }

        // 2. Obtain JWT token
        String tokenBody = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                ADMIN_EMAIL, ADMIN_PASSWORD);
        try {
            HttpRequest tokReq = HttpRequest.newBuilder()
                    .uri(URI.create(authBase + "/token"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> tokResp = HTTP.send(tokReq, HttpResponse.BodyHandlers.ofString());
            if (tokResp.statusCode() == 200) {
                String token = extractJsonValue(tokResp.body(), "token");
                AuthTokenHolder.setAdminToken(token);
                logger.info("Admin token obtained ✓");
            } else {
                logger.error("Failed to get admin token — HTTP {}: {}", tokResp.statusCode(), tokResp.body());
            }
        } catch (Exception e) {
            logger.error("Could not obtain admin token: {}", e.getMessage());
        }
    }

    static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIdx = json.indexOf(pattern);
        if (keyIdx == -1) return null;
        int colonIdx = json.indexOf(':', keyIdx + pattern.length());
        if (colonIdx == -1) return null;
        int startQuote = json.indexOf('"', colonIdx + 1);
        if (startQuote == -1) return null;
        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote);
    }
}
