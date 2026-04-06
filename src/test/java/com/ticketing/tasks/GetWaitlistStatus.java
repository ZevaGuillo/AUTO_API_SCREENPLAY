package com.ticketing.tasks;

import com.ticketing.config.Endpoints;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;
import io.restassured.http.ContentType;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Screenplay Task: GET /api/waitlist/status
 * Queries the waitlist status for a given user via X-User-Id header.
 */
public class GetWaitlistStatus implements Task {

    private final String userId;
    private final String eventId;
    private final String section;

    public GetWaitlistStatus(String userId, String eventId, String section) {
        this.userId = userId;
        this.eventId = eventId;
        this.section = section;
    }

    public static GetWaitlistStatus forUser(String userId, String eventId, String section) {
        return instrumented(GetWaitlistStatus.class, userId, eventId, section);
    }

    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
                Get.resource(Endpoints.WAITLIST_STATUS)
                        .with(req -> req
                                .accept(ContentType.JSON.toString())
                                .header("X-User-Id", userId)
                                .queryParam("eventId", eventId)
                                .queryParam("section", section))
        );
    }
}
