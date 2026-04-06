package com.ticketing.config;

/**
 * Centralised configuration for waitlist test data.
 * Values come from system properties first, falling back to defaults.
 */
public final class WaitlistTestData {

    private WaitlistTestData() {}

    // ── API Gateway ────────────────────────────────────────────────────
    public static final String GATEWAY_BASE_URL =
            System.getProperty("gateway.api.url", "http://localhost:5000");

    public static final String IDENTITY_BASE_URL = GATEWAY_BASE_URL + "/auth";
    public static final String CATALOG_BASE_URL  = GATEWAY_BASE_URL + "/catalog";

    // ── Test user for waitlist scenarios ────────────────────────────────
    public static final String TEST_USER_EMAIL =
            System.getProperty("test.wl.user.email", "waitlist-api-user@test.com");

    public static final String TEST_USER_PASSWORD =
            System.getProperty("test.wl.user.password", "WlTest1234!");

    public static final String TEST_USER_ROLE = "User";

    // ── User ID for X-User-Id header (assigned after registration) ─────
    private static volatile String testUserId;

    public static String getTestUserId() { return testUserId; }
    public static void setTestUserId(String id) { testUserId = id; }

    // ── Non-existent user (valid GUID format but never registered — for 404 scenarios) ──
    public static final String NON_EXISTENT_USER_ID = "00000000-0000-0000-0000-000000000000";

    // ── Event for join waitlist ────────────────────────────────────────
    public static final String WAITLIST_EVENT_NAME = "Concierto Waitlist API";
    public static final String WAITLIST_EVENT_DESCRIPTION =
            "Evento de prueba para API waitlist";
    public static final String WAITLIST_EVENT_DATE = "2026-12-31T20:00:00Z";
    public static final String WAITLIST_EVENT_VENUE = "Teatro Nacional";
    public static final String WAITLIST_EVENT_MAX_CAPACITY = "500";
    public static final String WAITLIST_EVENT_BASE_PRICE = "50";
    public static final String WAITLIST_EVENT_SECTION = "VIP";

    private static volatile String eventId;

    public static String getEventId() { return eventId; }
    public static void setEventId(String id) { eventId = id; }

    // ── HTTP ───────────────────────────────────────────────────────────
    public static final int HTTP_TIMEOUT_SECONDS = 10;
}
