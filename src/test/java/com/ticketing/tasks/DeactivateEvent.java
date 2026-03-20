package com.ticketing.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import io.restassured.http.ContentType;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class DeactivateEvent implements Task {
    
    private final String eventId;
    
    public DeactivateEvent(String eventId) {
        this.eventId = eventId;
    }
    
    public static DeactivateEvent withId(String eventId) {
        return instrumented(DeactivateEvent.class, eventId);
    }
    
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Post.to("/admin/events/{id}/deactivate")
                .with(req -> req
                    .pathParam("id", eventId)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON))
        );
    }
}
