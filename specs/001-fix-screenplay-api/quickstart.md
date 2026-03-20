# Quickstart: Fix Screenplay API Test

**Feature**: 001-fix-screenplay-api  
**Created**: 2026-03-19

## Prerequisites

- Java 21
- Gradle 8.10
- API running (configurable via -DbaseUrl)

## Run Tests

```bash
# Default (localhost:50001)
gradle test

# Custom base URL
gradle test -DbaseUrl=https://api-staging.example.com
```

## Project Structure

```
src/test/java/com/ticketing/
├── hooks/Hooks.java                    # Stage initialization
├── models/Event.java                   # Event model
├── tasks/
│   ├── CreateEvent.java               # POST /events
│   ├── GetEvent.java                   # GET /events/{id}
│   ├── UpdateEvent.java                # PUT /events/{id}
│   └── DeactivateEvent.java            # POST /events/{id}/deactivate
├── questions/
│   ├── ResponseStatus.java             # Status code
│   └── EventField.java                 # JSON field extraction
├── stepdefinitions/
│   └── EventLifecycleSteps.java        # Cucumber steps
└── runners/
    └── EventLifecycleRunner.java
```

## Key Implementation Details

### 1. Stage Initialization (Hooks.java)

```java
@Before
public void setTheStage() {
    String baseUrl = System.getProperty("baseUrl", "http://localhost:50001");
    OnStage.setTheStage(
        Cast.whereEveryoneCan(CallAnApi.at(baseUrl))
    );
}
```

### 2. Event Model

```java
public class Event {
    private String id;
    private String name;
    private String description;
    private String date;
    private String venue;
    private boolean isActive;
    private int capacity;
    // getters, setters
}
```

### 3. Tasks (1 Task = 1 Action)

```java
// CreateEvent.java
public class CreateEvent implements Task {
    @Override
    public <T extends Actor> void performAs(T actor) {
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

### 4. Validations (Ensure)

```java
// Using Ensure for better reporting
OnStage.theActorInTheSpotlight().attemptsTo(
    Ensure.that(ResponseStatus.code()).isEqualTo(201),
    Ensure.that(EventField.field("name")).isEqualTo("Concert Test Event")
);
```

### 5. State Management

```java
// Store
Serenity.setSessionVariable("eventId").to(eventId);

// Retrieve
String eventId = Serenity.sessionVariableCalled("eventId");
```

## Common Mistakes

| ❌ WRONG | ✅ CORRECT |
|----------|-----------|
| assertThat(...) | Ensure.that(...).isEqualTo(...) |
| .body("{\"name\":\"Test\"}") | .body(eventObject) |
| contentType("application/json") | contentType(ContentType.JSON) |
| No Accept header | .accept(ContentType.JSON) |
| Hardcoded baseUrl | System.getProperty("baseUrl", ...) |

## Expected Behavior

1. ✅ Stage initialized via @Before hook
2. ✅ All actors have CallAnApi ability
3. ✅ Admin creates event (POST /events)
4. ✅ Event ID stored in session
5. ✅ User retrieves event (GET /events/{id})
6. ✅ Admin updates event (PUT /events/{id})
7. ✅ Admin deactivates event (POST /events/{id}/deactivate)
8. ✅ Final validation confirms isActive=false

## Verification

- No NoStageException errors
- Event ID persists across steps
- All assertions pass with Ensure
- Serenity reports show all interactions