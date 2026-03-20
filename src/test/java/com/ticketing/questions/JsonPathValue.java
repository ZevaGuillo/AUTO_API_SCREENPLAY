package com.ticketing.questions;

import io.restassured.response.Response;
import net.serenitybdd.screenplay.Question;

public class JsonPathValue {

    public static <T> Question<T> from(String jsonPath) {
        return actor -> {
            Response response = (Response) actor.recall("lastResponse");
            return response != null ? response.jsonPath().get(jsonPath) : null;
        };
    }

    public static Question<String> field(String fieldName) {
        return from(fieldName);
    }

    public static Question<Integer> integerField(String fieldName) {
        return from(fieldName);
    }

    public static Question<Boolean> booleanField(String fieldName) {
        return from(fieldName);
    }
}