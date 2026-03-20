package com.ticketing.tasks;

import com.ticketing.models.Event;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import io.restassured.http.ContentType;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class CreateEvent implements Task {
    
    private final Event event;
    
    public CreateEvent(Event event) {
        this.event = event;
    }
    
    public static CreateEvent withData(Event event) {
        return instrumented(CreateEvent.class, event);
    }
    
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Post.to("/admin/events")
                .with(req -> req
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(event))
        );
    }
}
