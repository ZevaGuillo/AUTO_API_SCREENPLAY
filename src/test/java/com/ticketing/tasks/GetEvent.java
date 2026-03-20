package com.ticketing.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;
import io.restassured.http.ContentType;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class GetEvent implements Task {
    
    private final String eventId;
    
    public GetEvent(String eventId) {
        this.eventId = eventId;
    }
    
    public static GetEvent withId(String eventId) {
        return instrumented(GetEvent.class, eventId);
    }
    
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Get.resource("/events/{id}")
                .with(req -> req
                    .pathParam("id", eventId)
                    .accept(ContentType.JSON.toString()))
        );
    }
}
