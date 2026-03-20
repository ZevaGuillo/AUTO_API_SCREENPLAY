package com.ticketing.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.LastResponse;
import net.serenitybdd.screenplay.Actor;

public class ResponseStatus implements Question<Integer> {
    
    public static ResponseStatus code() {
        return new ResponseStatus();
    }
    
    @Override
    public Integer answeredBy(Actor actor) {
        return LastResponse.received().answeredBy(actor).getStatusCode();
    }
}
