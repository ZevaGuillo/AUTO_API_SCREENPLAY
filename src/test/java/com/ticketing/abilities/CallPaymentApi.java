package com.ticketing.abilities;

import net.serenitybdd.screenplay.Ability;

public class CallPaymentApi implements Ability {
    private final String baseUrl;

    private CallPaymentApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static CallPaymentApi at(String baseUrl) {
        return new CallPaymentApi(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}