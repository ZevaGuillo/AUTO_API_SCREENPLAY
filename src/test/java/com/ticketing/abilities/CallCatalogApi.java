package com.ticketing.abilities;

import net.serenitybdd.screenplay.Ability;

public class CallCatalogApi implements Ability {
    private final String baseUrl;

    private CallCatalogApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static CallCatalogApi at(String baseUrl) {
        return new CallCatalogApi(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}