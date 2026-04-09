package com.ticketing.tasks;

import com.ticketing.config.Endpoints;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Delete;
import io.restassured.http.ContentType;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Screenplay Task: DELETE /api/waitlist/cancel
 * Cancels the user's waitlist entry using X-User-Id header.
 */
public class CancelWaitlist implements Task {

    private final String userId;
    private final String eventId;
    private final String section;
    private final String token;

    public CancelWaitlist(String userId, String eventId, String section, String token) {
        this.userId = userId;
        this.eventId = eventId;
        this.section = section;
        this.token = token;
    }

    public static CancelWaitlist forUser(String userId, String eventId, String section, String token) {
        return instrumented(CancelWaitlist.class, userId, eventId, section, token);
    }

    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
                Delete.from(Endpoints.WAITLIST_CANCEL)
                        .with(req -> req
                                .accept(ContentType.JSON)
                                .header("X-User-Id", userId)
                                .header("Authorization", "Bearer " + token)
                                .queryParam("eventId", eventId)
                                .queryParam("section", section))
        );
    }
}
