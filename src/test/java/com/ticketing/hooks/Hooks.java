package com.ticketing.hooks;

import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.actors.OnStage;

public class Hooks {

    private static final String DEFAULT_BASE_URL = "http://localhost:5000";

    @Before
    public void setTheStage() {
        String baseUrl = System.getProperty("baseUrl", System.getenv().getOrDefault("BASE_URL", DEFAULT_BASE_URL));
        OnStage.setTheStage(
            Cast.whereEveryoneCan(
                CallAnApi.at(baseUrl)
            )
        );
    }
}
