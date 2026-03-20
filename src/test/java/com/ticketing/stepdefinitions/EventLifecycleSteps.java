package com.ticketing.stepdefinitions;

import com.ticketing.models.Event;
import com.ticketing.questions.EventField;
import com.ticketing.questions.ResponseStatus;
import com.ticketing.tasks.CreateEvent;
import com.ticketing.tasks.GetEvent;
import com.ticketing.tasks.UpdateEvent;
import com.ticketing.tasks.DeactivateEvent;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

public class EventLifecycleSteps {

    private Event eventToCreate;
    private Event eventToUpdate;
    private String storedEventId;

    @Given("the catalog API is available")
    public void catalogApiIsAvailable() {
        OnStage.theActorCalled("Admin");
        OnStage.theActorCalled("User");
    }

    @When("the admin creates a new event")
    public void adminCreatesNewEvent() {
        eventToCreate = new Event();
        eventToCreate.setName("Concert Test Event");
        eventToCreate.setDescription("Test concert event");
        eventToCreate.setEventDate("2026-12-31T22:00");
        eventToCreate.setVenue("Test Arena");
        eventToCreate.setMaxCapacity(5000);
        eventToCreate.setBasePrice(99.99);
        
        OnStage.theActorInTheSpotlight().attemptsTo(
            CreateEvent.withData(eventToCreate)
        );
    }

    @Then("the event should be created successfully")
    public void eventShouldBeCreatedSuccessfully() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(201)
        );
        
        storedEventId = LastResponse.received().answeredBy(OnStage.theActorInTheSpotlight()).jsonPath().getString("id");
    }

    @When("the user retrieves the created event")
    public void userRetrievesCreatedEvent() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            GetEvent.withId(storedEventId)
        );
    }

    @Then("the event data should be correct")
    public void eventDataShouldBeCorrect() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(200),
            Ensure.that(EventField.value("name")).isEqualTo("Concert Test Event"),
            Ensure.that(EventField.value("venue")).isEqualTo("Test Arena")
        );
    }

    @When("the admin updates the event information")
    public void adminUpdatesEventInformation() {
        eventToUpdate = new Event();
        eventToUpdate.setName("Updated Concert Event");
        eventToUpdate.setDescription("Updated description");
        eventToUpdate.setEventDate("2026-12-31T22:00");
        eventToUpdate.setVenue("Updated Arena");
        eventToUpdate.setMaxCapacity(6000);
        eventToUpdate.setBasePrice(149.99);
        
        OnStage.theActorCalled("Admin").attemptsTo(
            UpdateEvent.with(storedEventId, eventToUpdate)
        );
    }

    @Then("the event should reflect the updated data")
    public void eventShouldReflectUpdatedData() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(200),
            Ensure.that(EventField.value("name")).isEqualTo("Updated Concert Event"),
            Ensure.that(EventField.value("description")).isEqualTo("Updated description"),
            Ensure.that(EventField.value("maxCapacity")).isEqualTo("6000")
        );
    }

    @When("the admin deactivates the event")
    public void adminDeactivatesTheEvent() {
        OnStage.theActorCalled("Admin").attemptsTo(
            DeactivateEvent.withId(storedEventId)
        );
    }

    @Then("the event should be marked as inactive")
    public void eventShouldBeMarkedAsInactive() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(200)
        );
    }

    @When("the user retrieves the event again")
    public void userRetrievesEventAgain() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            GetEvent.withId(storedEventId)
        );
    }

    @Then("the event should remain inactive")
    public void eventShouldRemainInactive() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(EventField.value("isActive")).isEqualTo("false")
        );
    }
}
