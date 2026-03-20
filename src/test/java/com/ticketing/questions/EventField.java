package com.ticketing.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.LastResponse;
import net.serenitybdd.screenplay.Actor;

public class EventField implements Question<String> {
    
    private final String fieldName;
    
    public EventField(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public static EventField value(String fieldName) {
        return new EventField(fieldName);
    }
    
    @Override
    public String answeredBy(Actor actor) {
        return LastResponse.received().answeredBy(actor).jsonPath().getString(fieldName);
    }
}
