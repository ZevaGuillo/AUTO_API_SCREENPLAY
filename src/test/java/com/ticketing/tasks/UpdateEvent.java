package com.ticketing.tasks;

import com.ticketing.config.Endpoints;
import com.ticketing.models.Event;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Put;
import io.restassured.http.ContentType;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class UpdateEvent implements Task {
    
    private final String eventId;
    private final Event event;
    
    public UpdateEvent(String eventId, Event event) {
        this.eventId = eventId;
        this.event = event;
    }
    
    public static UpdateEvent with(String eventId, Event event) {
        return instrumented(UpdateEvent.class, eventId, event);
    }
    
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Put.to(Endpoints.UPDATE_EVENT)
                .with(req -> req
                    .pathParam("id", eventId)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(event))
        );
    }
}
