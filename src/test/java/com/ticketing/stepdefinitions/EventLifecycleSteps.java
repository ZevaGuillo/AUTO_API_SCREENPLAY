package com.ticketing.stepdefinitions;

import com.ticketing.config.TestData;
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

    @Given("an event has been created")
    public void anEventHasBeenCreated() {
        adminCreatesNewEvent();
        eventShouldBeCreatedSuccessfully();
    }

    @Given("the event has been deactivated")
    public void theEventHasBeenDeactivated() {
        OnStage.theActorCalled("Admin").attemptsTo(
            DeactivateEvent.withId(storedEventId)
        );
    }

    @When("the admin creates a new event")
    public void adminCreatesNewEvent() {
        eventToCreate = TestData.createEventData();
        
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
            Ensure.that(EventField.value("name")).isEqualTo(eventToCreate.getName()),
            Ensure.that(EventField.value("venue")).isEqualTo(eventToCreate.getVenue())
        );
    }

    @When("the admin updates the event information")
    public void adminUpdatesEventInformation() {
        eventToUpdate = TestData.updateEventData();
        
        OnStage.theActorCalled("Admin").attemptsTo(
            UpdateEvent.with(storedEventId, eventToUpdate)
        );
    }

    @Then("the event should reflect the updated data")
    public void eventShouldReflectUpdatedData() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(200),
            Ensure.that(EventField.value("name")).isEqualTo(eventToUpdate.getName()),
            Ensure.that(EventField.value("description")).isEqualTo(eventToUpdate.getDescription()),
            Ensure.that(EventField.value("maxCapacity")).isEqualTo(String.valueOf(eventToUpdate.getMaxCapacity()))
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
