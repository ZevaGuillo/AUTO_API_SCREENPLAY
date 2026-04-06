package com.ticketing.tasks;

import com.ticketing.config.Endpoints;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import io.restassured.http.ContentType;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Screenplay Task: POST /api/waitlist/join
 * Sends a join request with X-User-Id header, eventId and sectionCode.
 */
public class JoinWaitlist implements Task {

    private final String userId;
    private final String eventId;
    private final String section;

    public JoinWaitlist(String userId, String eventId, String section) {
        this.userId = userId;
        this.eventId = eventId;
        this.section = section;
    }

    public static JoinWaitlist forUser(String userId, String eventId, String section) {
        return instrumented(JoinWaitlist.class, userId, eventId, section);
    }

    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        String body = String.format(
                "{\"eventId\":\"%s\",\"section\":\"%s\"}",
                eventId, section);

        actor.attemptsTo(
                Post.to(Endpoints.WAITLIST_JOIN)
                        .with(req -> req
                                .contentType(ContentType.JSON)
                                .accept(ContentType.JSON)
                                .header("X-User-Id", userId)
                                .body(body))
        );
    }
}
