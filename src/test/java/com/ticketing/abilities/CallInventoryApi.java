package com.ticketing.abilities;

import net.serenitybdd.screenplay.Ability;

public class CallInventoryApi implements Ability {
    private final String baseUrl;

    private CallInventoryApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static CallInventoryApi at(String baseUrl) {
        return new CallInventoryApi(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}