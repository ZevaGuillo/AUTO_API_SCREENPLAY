# Tasks: Fix Screenplay API Test for Event CRUD Lifecycle

**Feature**: 001-fix-screenplay-api  
**Generated**: 2026-03-19  
**Total Tasks**: 20

## Summary

This feature refactors the test automation infrastructure to use correct Screenplay Pattern with Serenity BDD. Tasks are organized by User Story to enable independent implementation and testing.

## Dependencies

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundational - DELETE anti-patterns)
    ↓
Phase 3 (US1 - Stage Init)
    ↓
Phase 4 (US2 - State Management)
    ↓
Phase 5 (US3 - Tasks) ← Only after Stage is ready
    ↓
Phase 6 (US4 - Questions) ← Only after Tasks are ready
    ↓
Phase 7 (US5 - CRUD Lifecycle) ← Only after all above complete
    ↓
Phase 8 (Polish & Verification)
```

## Parallel Execution Opportunities

**None** - All phases have dependencies on previous phases completing successfully.

---

## Phase 1: Setup

**Goal**: Prepare project structure and directories

- [ ] T001 Create hooks directory in src/test/java/com/ticketing/hooks/
- [ ] T002 [P] Create tasks directory in src/test/java/com/ticketing/tasks/
- [ ] T003 [P] Create questions directory in src/test/java/com/ticketing/questions/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Goal**: Remove anti-patterns before implementing correct patterns

- [ ] T004 Delete src/test/java/com/ticketing/interactions/PostRequest.java (anti-pattern)
- [ ] T005 Delete src/test/java/com/ticketing/interactions/GetRequest.java (anti-pattern)
- [ ] T006 Delete src/test/java/com/ticketing/interactions/PutRequest.java (anti-pattern)
- [ ] T007 Delete src/test/java/com/ticketing/tasks/ManageEventLifecycle.java (violates Single Responsibility)

**Independent Test Criteria**: No old custom interaction classes exist in project

---

## Phase 3: US1 - Fix Stage Initialization (Priority: P1)

**Goal**: Initialize Screenplay Stage to prevent NoStageException

**Story**: QA engineers need the Screenplay Stage to be properly initialized before each test runs.

- [ ] T008 Create Hooks.java in src/test/java/com/ticketing/hooks/Hooks.java
- [ ] T009 Add @Before hook with OnStage.setTheStage(Cast.whereEveryoneCan(CallAnApi.at(BASE_URL)))
- [ ] T010 Configure BASE_URL via System.getProperty with default "http://localhost:50001"

**Hooks.java Pattern**:
```java
package com.ticketing.hooks;

import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.actors.OnStage;

public class Hooks {

    @Before
    public void setTheStage() {
        String baseUrl = System.getProperty("baseUrl", "http://localhost:50001");
        OnStage.setTheStage(
            Cast.whereEveryoneCan(CallAnApi.at(baseUrl))
        );
    }
}
```

**Independent Test Criteria**: Run any test - no NoStageException should occur

---

## Phase 4: US2 - Implement Shared State Management (Priority: P1)

**Goal**: Persist event ID across test steps using Serenity session variables

**Story**: QA engineers need to persist the event ID across test steps so that subsequent operations can use the same event.

- [ ] T011 Create StoreEventId task in src/test/java/com/ticketing/tasks/StoreEventId.java
- [ ] T012 Create RetrieveEventId task in src/test/java/com/ticketing/tasks/RetrieveEventId.java

**StoreEventId.java Pattern**:
```java
package com.ticketing.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.questions.SerenityRest;
import net.thucydides.core.annotations.Step;
import net.serenitybdd.Serenity;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class StoreEventId implements Task {

    @Step("Store event ID from response")
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        String eventId = SerenityRest.lastResponse().jsonPath().getString("id");
        Serenity.setSessionVariable("eventId").to(eventId);
    }
    
    public static StoreEventId fromResponse() {
        return instrumented(StoreEventId.class);
    }
}
```

**RetrieveEventId.java Pattern**:
```java
package com.ticketing.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.Serenity;
import net.thucydides.core.annotations.Step;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class RetrieveEventId implements Task {

    @Step("Retrieve stored event ID")
    @Override
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T actor) {
        String eventId = Serenity.sessionVariableCalled("eventId");
        actor.remember("currentEventId", eventId);
    }
    
    public static RetrieveEventId fromSession() {
        return instrumented(RetrieveEventId.class);
    }
    
    public String getEventId() {
        return Serenity.sessionVariableCalled("eventId");
    }
}
```

**Independent Test Criteria**: Event ID retrieved from session variable matches stored ID

---

## Phase 5: US3 - Create Reusable Tasks for API Actions (Priority: P1)

**Goal**: Encapsulate each API operation in its own Task class using native Serenity REST interactions

**Story**: QA engineers need encapsulated Task classes for each API operation.

**Prerequisites**: Phase 3 (Stage Init) must complete first

- [ ] T013 Create Event.java model in src/test/java/com/ticketing/models/Event.java
- [ ] T014 Create CreateEvent.java task in src/test/java/com/ticketing/tasks/CreateEvent.java
- [ ] T015 Create GetEvent.java task in src/test/java/com/ticketing/tasks/GetEvent.java
- [ ] T016 Create UpdateEvent.java task in src/test/java/com/ticketing/tasks/UpdateEvent.java
- [ ] T017 Create DeactivateEvent.java task in src/test/java/com/ticketing/tasks/DeactivateEvent.java

**Event.java Model**:
```java
package com.ticketing.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("venue")
    private String venue;
    
    @JsonProperty("isActive")
    private boolean isActive;
    
    @JsonProperty("capacity")
    private int capacity;
    
    // Getters and setters
}
```

**CreateEvent.java Pattern**:
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

**GetEvent.java Pattern**:
```java
package com.ticketing.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;
import net.thucydides.core.annotations.Step;
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
    
    @Step("Retrieve event with ID #eventId")
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
```

**UpdateEvent.java Pattern**:
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

**DeactivateEvent.java Pattern**:
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

**Independent Test Criteria**: Each Task compiles and can be instantiated

---

## Phase 6: US4 - Create Reusable Questions for Validations (Priority: P1)

**Goal**: Encapsulate response validations in proper Question classes

**Story**: QA engineers need encapsulated Question classes for response validation.

**Prerequisites**: Tasks (Phase 5) must complete first

- [ ] T018 Create ResponseStatus.java question in src/test/java/com/ticketing/questions/ResponseStatus.java
- [ ] T019 Create EventField.java question in src/test/java/com/ticketing/questions/EventField.java

**ResponseStatus.java CORRECT Pattern**:
```java
package com.ticketing.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.SerenityRest;
import net.serenitybdd.screenplay.Actor;

public class ResponseStatus implements Question<Integer> {
    
    public static ResponseStatus code() {
        return new ResponseStatus();
    }
    
    @Override
    public Integer answeredBy(Actor actor) {
        return SerenityRest.lastResponse().statusCode();
    }
}
```

**EventField.java CORRECT Pattern**:
```java
package com.ticketing.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.SerenityRest;
import net.serenitybdd.screenplay.Actor;

public class EventField implements Question<String> {
    
    private final String fieldName;
    
    public EventField(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public static EventField value(String fieldName) {
        return new EventField(fieldName);
    }
    
    @Override
    public String answeredBy(Actor actor) {
        return SerenityRest.lastResponse().jsonPath().getString(fieldName);
    }
}
```

**Independent Test Criteria**: Questions implement Question<T> interface correctly

---

## Phase 7: US5 - Verify Complete CRUD Lifecycle (Priority: P2)

**Goal**: Implement full end-to-end CRUD scenario integrating all components

**Story**: QA engineers need a complete end-to-end test validating all CRUD operations.

**Prerequisites**: All previous phases must complete

- [ ] T020 Create EventLifecycleSteps.java in src/test/java/com/ticketing/stepdefinitions/EventLifecycleSteps.java
- [ ] T021 Verify EventLifecycleRunner.java in src/test/java/com/ticketing/runners/EventLifecycleRunner.java

**EventLifecycleSteps.java Pattern**:
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
    private String eventId;

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
        
        eventId = SerenityRest.lastResponse().jsonPath().getString("id");
        Serenity.setSessionVariable("eventId").to(eventId);
    }

    @When("the user retrieves the created event")
    public void userRetrievesCreatedEvent() {
        eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorCalled("User").attemptsTo(
            GetEvent.withId(eventId)
        );
    }

    @Then("the event data should be correct")
    public void eventDataShouldBeCorrect() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(EventField.value("name")).isEqualTo("Concert Test Event"),
            Ensure.that(EventField.value("venue")).isEqualTo("Test Arena")
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
        
        eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorCalled("Admin").attemptsTo(
            UpdateEvent.with(eventId, eventToUpdate)
        );
    }

    @Then("the event should reflect the updated data")
    public void eventShouldReflectUpdatedData() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(200),
            Ensure.that(EventField.value("name")).isEqualTo("Updated Concert Event"),
            Ensure.that(EventField.value("venue")).isEqualTo("Updated Arena"),
            Ensure.that(EventField.value("capacity")).isEqualTo("6000")
        );
    }

    @When("the admin deactivates the event")
    public void adminDeactivatesTheEvent() {
        eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorCalled("Admin").attemptsTo(
            DeactivateEvent.withId(eventId)
        );
    }

    @Then("the event should be marked as inactive")
    public void eventShouldBeMarkedAsInactive() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(ResponseStatus.code()).isEqualTo(200),
            Ensure.that(EventField.value("isActive")).isEqualTo("false")
        );
    }

    @When("the user retrieves the event again")
    public void userRetrievesEventAgain() {
        eventId = Serenity.sessionVariableCalled("eventId");
        OnStage.theActorCalled("User").attemptsTo(
            GetEvent.withId(eventId)
        );
    }

    @Then("the event should remain inactive")
    public void eventShouldRemainInactive() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            Ensure.that(EventField.value("isActive")).isEqualTo("false")
        );
    }
}
```

**Independent Test Criteria**: Full scenario executes without errors, all assertions pass

---

## Phase 8: Polish & Cross-Cutting Concerns

**Goal**: Verify all components work together and generate reports

- [ ] T022 Run gradle clean test to verify compilation
- [ ] T023 Run gradle test with -DbaseUrl parameter to verify configurable BASE_URL
- [ ] T024 Verify Serenity reports are generated in target/site/serenity/

**Verification Commands**:
```bash
# Default execution
gradle test

# Custom base URL
gradle test -DbaseUrl=https://api-staging.example.com

# With report
gradle test aggregate
```

---

## Task Summary

| Phase | User Story | Task Count |
|-------|------------|------------|
| 1 | Setup | 3 |
| 2 | Foundational | 4 |
| 3 | US1 - Stage Init | 3 |
| 4 | US2 - State Management | 2 |
| 5 | US3 - Tasks | 5 |
| 6 | US4 - Questions | 2 |
| 7 | US5 - CRUD Lifecycle | 2 |
| 8 | Polish | 3 |

**Total**: 26 tasks

---

## Success Criteria Mapping

| Success Criteria | Validated By |
|-----------------|--------------|
| SC-001: No NoStageException | Phase 3 (T008-T010) |
| SC-002: Event ID persisted | Phase 4 (T011-T012), Phase 7 |
| SC-003: Tasks per operation | Phase 5 (T013-T017) |
| SC-004: Questions implement Question<T> | Phase 6 (T018-T019) |
| SC-005: No direct REST in steps | Phase 2 deletion + Phase 7 |
| SC-006: Full CRUD passes | Phase 8 verification |

---

## Common Mistakes (Reference)

| ❌ WRONG | ✅ CORRECT |
|----------|-----------|
| `SerenityRest.theResponse().statusCode()` | `SerenityRest.lastResponse().statusCode()` in answeredBy() |
| `return SerenityRest.theResponse().body()` | `return SerenityRest.lastResponse().jsonPath().getString()` |
| No @Before hook | Add @Before with OnStage.setTheStage() |
| Hardcoded baseUrl | System.getProperty("baseUrl", "default") |
| assertThat(...) | Ensure.that(...).isEqualTo(...) |