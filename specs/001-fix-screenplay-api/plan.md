# Implementation Plan: Fix Screenplay API Test for Event CRUD Lifecycle

**Branch**: `001-fix-screenplay-api` | **Date**: 2026-03-19 | **Spec**: [Link to spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-fix-screenplay-api/spec.md`

## Summary

Refactorizar la infraestructura de tests API de Serenity BDD para usar correctamente el Screenplay Pattern con Serenity Screenplay REST. Correcciones:

1. ✅ Stage initialization con Cast en @Before hook
2. ✅ Actors con CallAnApi ability configurable
3. ✅ Native Serenity REST interactions (Post.to, Get.resource, Put.to)
4. ✅ Model classes para requests (Event.java)
5. ✅ Ensure para validaciones (mejor reporting)
6. ✅ Headers globales (ContentType.JSON, Accept)
7. ✅ BASE_URL configurable via properties

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Serenity BDD 5.3.2, Serenity Screenplay 5.3.2, Serenity Screenplay REST 5.3.2, Cucumber 7.34.2, REST Assured 5.4.0, JUnit 5  
**Storage**: N/A  
**Testing**: Serenity BDD with Cucumber, JUnit Platform  
**Target Platform**: Linux/Windows  
**Project Type**: Test automation framework  
**Performance Goals**: Tests must run in under 5 minutes  
**Base URL**: Configurable via `baseUrl` property (default: http://localhost:50001)  

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Screenplay Pattern | ✅ PASS | Abilities/Tasks/Questions separation |
| II. Serenity BDD Integration | ✅ PASS | Uses Serenity Ensure for assertions |
| III. Test-First BDD Approach | ✅ PASS | Feature file in Gherkin |
| IV. Service Layer Abstraction | ✅ PASS | Native Serenity REST |
| V. Single Responsibility Tasks | ✅ PASS | 1 Task = 1 action |

## Project Structure

```text
src/test/java/com/ticketing/
├── hooks/Hooks.java                    # Stage initialization
├── models/Event.java                   # Event model for requests
├── tasks/
│   ├── CreateEvent.java               # POST /events
│   ├── GetEvent.java                   # GET /events/{id}
│   ├── UpdateEvent.java                # PUT /events/{id}
│   └── DeactivateEvent.java            # POST /events/{id}/deactivate
├── questions/
│   ├── ResponseStatus.java             # Status code validation
│   └── EventField.java                 # JSON field extraction
├── stepdefinitions/
│   └── EventLifecycleSteps.java        # Cucumber steps
└── runners/
    └── EventLifecycleRunner.java
```

## Configuration

### serenity.conf
```properties
serenity.rest.default-base-url = ${baseUrl:http://localhost:50001}
serenity.logging = VERBOSE
```

### Hooks.java (CON BASE_URL CONFIGURABLE)
```java
@Before
public void setTheStage() {
    String baseUrl = System.getProperty("baseUrl", "http://localhost:50001");
    OnStage.setTheStage(
        Cast.whereEveryoneCan(
            CallAnApi.at(baseUrl)
        )
    );
}
```

## Event Model (Request/Response)

```java
package com.ticketing.models;

public class Event {
    private String id;
    private String name;
    private String description;
    private String date;
    private String venue;
    private boolean isActive;
    private int capacity;
    
    // Getters y setters (Jackson serializa automáticamente)
}
```

## Task Design (1 Task = 1 Action)

### CreateEvent.java
```java
package com.ticketing.tasks;

import com.ticketing.models.Event;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.thucydides.core.annotations.Step;
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
    
    @Step("Create a new event")
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Post.to("/events")
                .with(req -> req
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(event))
        );
    }
}
```

### GetEvent.java
```java
package com.ticketing.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;
import net.thucydides.core.annotations.Step;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class GetEvent implements Task {
    
    private final String eventId;
    
    public GetEvent(String eventId) {
        this.eventId = eventId;
    }
    
    public static GetEvent withId(String eventId) {
        return instrumented(GetEvent.class, eventId);
    }
    
    @Step("Retrieve event with ID #eventId")
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Get.resource("/events/{id}")
                .with(req -> req
                    .pathParam("id", eventId)
                    .accept("application/json"))
        );
    }
}
```

### UpdateEvent.java
```java
package com.ticketing.tasks;

import com.ticketing.models.Event;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Put;
import net.thucydides.core.annotations.Step;
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
    
    @Step("Update event with ID #eventId")
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Put.to("/events/{id}")
                .with(req -> req
                    .pathParam("id", eventId)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(event))
        );
    }
}
```

### DeactivateEvent.java
```java
package com.ticketing.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.thucydides.core.annotations.Step;
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
    
    @Step("Deactivate event with ID #eventId")
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        actor.attemptsTo(
            Post.to("/events/{id}/deactivate")
                .with(req -> req
                    .pathParam("id", eventId)
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON))
        );
    }
}
```

## Validations (Ensure + SerenityRest)

### ResponseStatus.java
```java
package com.ticketing.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.SerenityRest;

public class ResponseStatus {
    
    public static Question<Integer> code() {
        return SerenityRest.theResponse().statusCode();
    }
}
```

### EventField.java
```java
package com.ticketing.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.SerenityRest;

public class EventField {
    
    public static Question<String> field(String fieldName) {
        return SerenityRest.theResponse().body().jsonPath().getString(fieldName);
    }
    
    public static Question<Boolean> booleanField(String fieldName) {
        return SerenityRest.theResponse().body().jsonPath().getBoolean(fieldName);
    }
}
```

## Step Definitions (Ensure)

```java
package com.ticketing.stepdefinitions;

import com.ticketing.models.Event;
import com.ticketing.questions.EventField;
import com.ticketing.questions.ResponseStatus;
import com.ticketing.tasks.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.rest.questions.SerenityRest;
import net.serenitybdd.Serenity;
import java.time.LocalDate;

public class EventLifecycleSteps {

    private Event eventToCreate;
    private Event eventToUpdate;

    @Given("the catalog API is available")
    public void catalogApiIsAvailable() {
        // Stage initialized via Hooks.java
    }

    @When("the admin creates a new event")
    public void adminCreatesNewEvent() {
        eventToCreate = new Event();
        eventToCreate.setName("Concert Test Event");
        eventToCreate.setDescription("Test concert event");
        eventToCreate.setDate(LocalDate.of(2026, 12, 31).toString());
        eventToCreate.setVenue("Test Arena");
        eventToCreate.setCapacity(5000);
        
        OnStage.theActorInTheSpotlight().attemptsTo(
            CreateEvent.withData(eventToCreate)
        );
    }

    @Then("the event should be created successfully")
    public void eventShouldBeCreatedSuccessfully() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(201)
        );
        
        String eventId = SerenityRest.lastResponse().jsonPath().getString("id");
        Serenity.setSessionVariable("eventId").to(eventId);
    }

    @When("the user retrieves the created event")
    public void userRetrievesCreatedEvent() {
        String eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorInTheSpotlight().attemptsTo(
            GetEvent.withId(eventId)
        );
    }

    @Then("the event data should be correct")
    public void eventDataShouldBeCorrect() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(EventField.field("name")).isEqualTo("Concert Test Event")
        );
    }

    @When("the admin updates the event information")
    public void adminUpdatesEventInformation() {
        eventToUpdate = new Event();
        eventToUpdate.setName("Updated Concert Event");
        eventToUpdate.setDescription("Updated description");
        eventToUpdate.setDate(LocalDate.of(2026, 12, 31).toString());
        eventToUpdate.setVenue("Updated Arena");
        eventToUpdate.setCapacity(6000);
        
        String eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorInTheSpotlight().attemptsTo(
            UpdateEvent.with(eventId, eventToUpdate)
        );
    }

    @Then("the event should reflect the updated data")
    public void eventShouldReflectUpdatedData() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(200),
            Ensure.that(EventField.field("name")).isEqualTo("Updated Concert Event")
        );
    }

    @When("the admin deactivates the event")
    public void adminDeactivatesTheEvent() {
        String eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorInTheSpotlight().attemptsTo(
            DeactivateEvent.withId(eventId)
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
        String eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorInTheSpotlight().attemptsTo(
            GetEvent.withId(eventId)
        );
    }

    @Then("the event should remain inactive")
    public void eventShouldRemainInactive() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(EventField.booleanField("isActive")).isFalse()
        );
    }
}
```

## Running Tests

```bash
# Default (localhost:50001)
gradle test

# Custom base URL
gradle test -DbaseUrl=https://api-staging.example.com
```

## Summary of Decisions

| Decision | Rationale |
|----------|-----------|
| Use Ensure for assertions | Better Serenity integration and reporting |
| Event model class | Type safety, auto-serialization by REST Assured |
| ContentType.JSON headers | Proper request/response format |
| Configurable BASE_URL | CI/CD compatibility |
| Separate Task classes | Single Responsibility principle |

---

**Plan ready for**: `/speckit.tasks` command to generate task breakdown