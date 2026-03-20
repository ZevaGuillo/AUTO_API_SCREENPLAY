package com.ticketing.abilities;

import net.serenitybdd.screenplay.Ability;

public class CallOrderingApi implements Ability {
    private final String baseUrl;

    private CallOrderingApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static CallOrderingApi at(String baseUrl) {
        return new CallOrderingApi(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}