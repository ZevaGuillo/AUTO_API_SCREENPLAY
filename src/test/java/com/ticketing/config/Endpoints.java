package com.ticketing.config;

public final class Endpoints {

    private Endpoints() {}

    private static final String CATALOG_PREFIX = "/catalog";
    private static final String ADMIN = CATALOG_PREFIX + "/admin";

    public static final String CREATE_EVENT = ADMIN + "/events";
    public static final String GET_EVENT = CATALOG_PREFIX + "/events/{id}";
    public static final String UPDATE_EVENT = ADMIN + "/events/{id}";
    public static final String DEACTIVATE_EVENT = ADMIN + "/events/{id}/deactivate";
}
