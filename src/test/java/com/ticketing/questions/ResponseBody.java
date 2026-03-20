package com.ticketing.questions;

import io.restassured.response.Response;
import net.serenitybdd.screenplay.Question;

public class ResponseBody {

    public static Question<String> asString() {
        return actor -> {
            Response response = (Response) actor.recall("lastResponse");
            return response != null ? response.getBody().asString() : "";
        };
    }

    public static Question<String> asPrettyString() {
        return actor -> {
            Response response = (Response) actor.recall("lastResponse");
            return response != null ? response.getBody().asPrettyString() : "";
        };
    }
}