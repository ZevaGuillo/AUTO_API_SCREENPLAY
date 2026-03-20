package com.ticketing.hooks;

import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.actors.OnStage;

public class Hooks {

    @Before
    public void setTheStage() {
        String baseUrl = System.getProperty("baseUrl", "http://localhost:50001");
        OnStage.setTheStage(
            Cast.whereEveryoneCan(
                CallAnApi.at(baseUrl)
            )
        );
    }
}
