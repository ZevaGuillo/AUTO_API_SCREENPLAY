package com.ticketing.config;

public final class Endpoints {

    private Endpoints() {}

    private static final String CATALOG_PREFIX = "/catalog";
    private static final String ADMIN = CATALOG_PREFIX + "/admin";

    public static final String CREATE_EVENT = ADMIN + "/events";
    public static final String GET_EVENT = CATALOG_PREFIX + "/events/{id}";
    public static final String UPDATE_EVENT = ADMIN + "/events/{id}";
    public static final String DEACTIVATE_EVENT = ADMIN + "/events/{id}/deactivate";

    // ── Identity ───────────────────────────────────────────────────────
    private static final String AUTH_PREFIX = "/auth";

    public static final String AUTH_REGISTER = AUTH_PREFIX + "/register";
    public static final String AUTH_TOKEN    = AUTH_PREFIX + "/token";
    public static final String AUTH_HEALTH   = AUTH_PREFIX + "/health";

    // ── Waitlist ───────────────────────────────────────────────────────
    private static final String WAITLIST_PREFIX = "/api/waitlist";

    public static final String WAITLIST_JOIN   = WAITLIST_PREFIX + "/join";
    public static final String WAITLIST_CANCEL = WAITLIST_PREFIX + "/cancel";
    public static final String WAITLIST_STATUS = WAITLIST_PREFIX + "/status";
}
