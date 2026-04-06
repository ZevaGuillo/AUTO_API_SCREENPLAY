package com.ticketing.hooks;

import com.ticketing.config.Endpoints;
import com.ticketing.config.WaitlistTestData;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cucumber hooks for @waitlist tagged scenarios.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Health-check the Identity service</li>
 *   <li>Create a test user via POST /auth/register</li>
 *   <li>Cache the user ID (X-User-Id) for scenario steps</li>
 *   <li>Create an event with a VIP section via Catalog admin API</li>
 *   <li>Cleanup: cancel any waitlist entry after each scenario</li>
 * </ul>
 *
 * Modelled after AUTO_FRONT_POM_FACTORY TestHooks.java.
 */
public class WaitlistTestHooks {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistTestHooks.class);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(WaitlistTestData.HTTP_TIMEOUT_SECONDS))
            .build();

    /** One-time data seeding guard. */
    private static volatile boolean dataSeeded = false;

    /** Cached user token for @After cleanup. */
    private static volatile String testUserToken = null;

    // ====================================================================
    // @Before — runs BEFORE every @waitlist scenario
    // ====================================================================

    @Before(value = "@waitlist", order = 1)
    public void setupWaitlistTestData(Scenario scenario) {
        logger.info("═══ @Before [waitlist] — {} ═══", scenario.getName());

        // Set the Screenplay stage
        String baseUrl = System.getProperty("baseUrl",
                System.getenv().getOrDefault("BASE_URL", WaitlistTestData.GATEWAY_BASE_URL));
        OnStage.setTheStage(Cast.whereEveryoneCan(CallAnApi.at(baseUrl)));

        if (dataSeeded) {
            logger.info("Test data already seeded. Skipping API calls.");
            return;
        }

        // 1. Health check (GET /auth/health)
        if (!isHealthy()) {
            logger.error("Identity Service unreachable at {}", WaitlistTestData.IDENTITY_BASE_URL);
            throw new IllegalStateException(
                    "Identity Service unreachable: " + WaitlistTestData.IDENTITY_BASE_URL);
        }
        logger.info("Identity Service healthy ✓");

        // 2. Create test user (POST /auth/register)
        String userId = createUser(
                WaitlistTestData.TEST_USER_EMAIL,
                WaitlistTestData.TEST_USER_PASSWORD,
                WaitlistTestData.TEST_USER_ROLE);
        if (userId == null) {
            throw new IllegalStateException(
                    "Could not create test user: " + WaitlistTestData.TEST_USER_EMAIL);
        }
        WaitlistTestData.setTestUserId(userId);
        logger.info("Test user ready: {} (uid={}) ✓", WaitlistTestData.TEST_USER_EMAIL, userId);

        // 3. Cache user token for @After cleanup (POST /auth/token)
        testUserToken = getToken(
                WaitlistTestData.TEST_USER_EMAIL,
                WaitlistTestData.TEST_USER_PASSWORD);
        if (testUserToken != null) {
            logger.info("Test user token cached for cleanup ✓");
        }

        // 4. Create event with VIP section via Catalog admin API
        String eventId = createEventIfAbsent();
        if (eventId == null) {
            throw new IllegalStateException(
                    "Could not create waitlist event: " + WaitlistTestData.WAITLIST_EVENT_NAME);
        }
        WaitlistTestData.setEventId(eventId);
        logger.info("Waitlist event ready: {} (id={}) ✓",
                WaitlistTestData.WAITLIST_EVENT_NAME, eventId);

        dataSeeded = true;
        logger.info("═══ Data seeding complete ═══");
    }

    // ====================================================================
    // @After — runs AFTER every @waitlist scenario
    // ====================================================================

    @After(value = "@waitlist", order = 1)
    public void teardownWaitlistTestData(Scenario scenario) {
        logger.info("═══ @After [waitlist] — {} — {} ═══",
                scenario.getName(), scenario.getStatus());

        if (scenario.isFailed()) {
            logger.error("Scenario FAILED: {}", scenario.getName());
        }

        // Cancel any waitlist entry so the next scenario starts clean
        if (testUserToken != null) {
            cancelWaitlistEntry(testUserToken);
            logger.info("Waitlist entry cancelled (cleanup) ✓");
        } else {
            String token = getToken(
                    WaitlistTestData.TEST_USER_EMAIL,
                    WaitlistTestData.TEST_USER_PASSWORD);
            if (token != null) {
                cancelWaitlistEntry(token);
                testUserToken = token;
                logger.info("Waitlist entry cancelled (cleanup, fresh token) ✓");
            } else {
                logger.warn("Could not obtain token for cleanup — skipping waitlist cancel.");
            }
        }

        logger.info("═══ Teardown complete ═══");
    }

    // ====================================================================
    // Identity API helpers
    // ====================================================================

    private boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WaitlistTestData.IDENTITY_BASE_URL + "/health"))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.warn("Identity health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * POST /auth/register — creates a user.
     * @return the user UID from the response, or a generated fallback if 400 (already exists).
     */
    private String createUser(String email, String password, String role) {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                email, password, role);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WaitlistTestData.IDENTITY_BASE_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(WaitlistTestData.HTTP_TIMEOUT_SECONDS))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status == 201) {
                String uid = extractJsonValue(response.body(), "uid");
                if (uid == null) uid = extractJsonValue(response.body(), "id");
                if (uid == null) uid = extractJsonValue(response.body(), "sub");
                logger.info("User created: {} (role={})", email, role);
                // If no uid in response, get it from token
                if (uid == null) {
                    String tkn = getToken(email, password);
                    if (tkn != null) uid = extractUidFromToken(tkn);
                }
                return uid;
            } else if (status == 400) {
                logger.info("User already exists (idempotent): {}", email);
                // Get the uid by logging in
                String token = getToken(email, password);
                if (token != null) {
                    String uid = extractUidFromToken(token);
                    if (uid != null) return uid;
                }
                return null;
            } else {
                logger.error("Failed to create user {} — HTTP {} : {}", email, status, response.body());
                return null;
            }
        } catch (Exception e) {
            logger.error("Identity unreachable when creating user {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * POST /auth/token — exchanges email + password for a JWT.
     */
    private String getToken(String email, String password) {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email, password);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WaitlistTestData.IDENTITY_BASE_URL + "/token"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(WaitlistTestData.HTTP_TIMEOUT_SECONDS))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String token = extractJsonValue(response.body(), "token");
                logger.info("Token obtained for {}", email);
                return token;
            } else {
                logger.error("Failed to get token for {} — HTTP {}", email, response.statusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Identity unreachable when getting token: {}", e.getMessage());
            return null;
        }
    }

    // ====================================================================
    // Catalog API helpers
    // ====================================================================

    private String createEventIfAbsent() {
        String adminEventsUrl = WaitlistTestData.CATALOG_BASE_URL + "/admin/events";
        String listEventsUrl = WaitlistTestData.CATALOG_BASE_URL + "/events";
        String name = WaitlistTestData.WAITLIST_EVENT_NAME;

        // Check if event already exists (GET /catalog/events)
        try {
            HttpResponse<String> listResp = doGet(listEventsUrl);
            if (listResp.statusCode() == 200 && listResp.body().contains(name)) {
                logger.info("Event '{}' already exists — fetching ID.", name);
                return extractIdByName(listResp.body(), name);
            }
        } catch (Exception e) {
            logger.debug("Could not check events list: {}", e.getMessage());
        }

        // Create event
        String body = String.format(
                "{\"name\":\"%s\",\"description\":\"%s\","
                + "\"eventDate\":\"%s\",\"venue\":\"%s\","
                + "\"maxCapacity\":%s,\"basePrice\":%s}",
                name,
                WaitlistTestData.WAITLIST_EVENT_DESCRIPTION,
                WaitlistTestData.WAITLIST_EVENT_DATE,
                WaitlistTestData.WAITLIST_EVENT_VENUE,
                WaitlistTestData.WAITLIST_EVENT_MAX_CAPACITY,
                WaitlistTestData.WAITLIST_EVENT_BASE_PRICE);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(adminEventsUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(WaitlistTestData.HTTP_TIMEOUT_SECONDS))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                String eventId = extractUuid(response.body());
                logger.info("Event created: {} (HTTP {})", name, status);

                // Generate seats with VIP section
                if (eventId != null) {
                    generateSeats(adminEventsUrl, eventId);
                }
                return eventId;
            } else if (status == 409) {
                logger.info("Event already exists (409): {}", name);
                HttpResponse<String> listResp = doGet(listEventsUrl);
                return extractIdByName(listResp.body(), name);
            } else {
                logger.warn("Event API returned HTTP {} for '{}': {}", status, name, response.body());
                return null;
            }
        } catch (Exception e) {
            logger.warn("Could not create event: {}", e.getMessage());
            return null;
        }
    }

    private void generateSeats(String adminEventsUrl, String eventId) {
        String seatsUrl = adminEventsUrl + "/" + eventId + "/seats";
        String body = "{\"sectionConfigurations\":["
                + "{\"sectionCode\":\"VIP\",\"rows\":5,\"seatsPerRow\":10,\"priceMultiplier\":2.0},"
                + "{\"sectionCode\":\"General\",\"rows\":10,\"seatsPerRow\":20,\"priceMultiplier\":1.0}"
                + "]}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(seatsUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(WaitlistTestData.HTTP_TIMEOUT_SECONDS))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                logger.info("Seats generated for event {} ✓", eventId);
            } else {
                logger.info("Seats generation returned HTTP {} (may already exist)", status);
            }
        } catch (Exception e) {
            logger.warn("Could not generate seats for event {}: {}", eventId, e.getMessage());
        }
    }

    // ====================================================================
    // Waitlist API helpers
    // ====================================================================

    private void cancelWaitlistEntry(String userToken) {
        String eventId = WaitlistTestData.getEventId();
        String section = WaitlistTestData.WAITLIST_EVENT_SECTION;
        if (eventId == null || section == null) {
            logger.debug("No eventId/section — skipping waitlist cancel");
            return;
        }
        // Extract userId from token for X-User-Id header
        String userId = extractUidFromToken(userToken);
        if (userId == null) {
            logger.debug("Could not extract userId from token — skipping cancel");
            return;
        }
        try {
            String cancelUrl = String.format("%s/api/waitlist/cancel?eventId=%s&section=%s",
                    WaitlistTestData.GATEWAY_BASE_URL, eventId, section);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(cancelUrl))
                    .header("X-User-Id", userId)
                    .header("Accept", "application/json")
                    .DELETE()
                    .timeout(Duration.ofSeconds(WaitlistTestData.HTTP_TIMEOUT_SECONDS))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300 || status == 404) {
                logger.debug("Waitlist cancel: HTTP {}", status);
            } else {
                logger.warn("Waitlist cancel returned HTTP {}", status);
            }
        } catch (Exception e) {
            logger.warn("Could not cancel waitlist entry: {}", e.getMessage());
        }
    }

    // ====================================================================
    // JSON helpers
    // ====================================================================

    private HttpResponse<String> doGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(WaitlistTestData.HTTP_TIMEOUT_SECONDS))
                .build();
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
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

    private String extractUuid(String json) {
        Pattern p = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        // Fallback: numeric id
        Pattern np = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher nm = np.matcher(json);
        if (nm.find()) return nm.group(1);
        return null;
    }

    private String extractIdByName(String json, String name) {
        int nameIdx = json.indexOf(name);
        if (nameIdx == -1) return null;
        // Search backwards for the nearest "id" field
        String before = json.substring(Math.max(0, nameIdx - 200), nameIdx);
        Pattern p = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
        Matcher m = p.matcher(before);
        String lastId = null;
        while (m.find()) lastId = m.group(1);
        if (lastId != null) return lastId;
        // Numeric fallback
        Pattern np = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher nm = np.matcher(before);
        while (nm.find()) lastId = nm.group(1);
        return lastId;
    }

    /**
     * Decodes a JWT payload to extract "uid" or "sub" claim.
     */
    private String extractUidFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            String uid = extractJsonValue(payload, "uid");
            if (uid == null) uid = extractJsonValue(payload, "sub");
            return uid;
        } catch (Exception e) {
            logger.debug("Could not decode token: {}", e.getMessage());
            return null;
        }
    }
}
