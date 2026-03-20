# Research: Fix Screenplay API Test

**Feature**: 001-fix-screenplay-api  
**Created**: 2026-03-19

## Research Summary

This feature corrects critical implementation errors in the previous Screenplay API test approach.

## Key Corrections

### 1. Missing Screenplay Stage Initialization

**Problem**: Previous code threw `NoStageException` because Stage was never initialized.

**Solution**: Create `Hooks.java` with `@Before` annotation:

```java
@Before
public void setTheStage() {
    OnStage.setTheStage(
        Cast.whereEveryoneCan(CallAnApi.at("http://localhost:50001"))
    );
}
```

### 2. Missing CallAnApi Ability

**Problem**: `OnStage.theActorCalled("Admin")` doesn't know which API to call without an Ability.

**Solution**: Use `Cast.whereEveryoneCan(CallAnApi.at(BASE_URL))` in Hooks.

### 3. Custom Interactions Anti-Pattern

**Problem**: Created custom `PostRequest`, `GetRequest` classes that duplicate native Serenity REST interactions.

**Solution**: Delete custom interactions. Use native:
- `Post.to("/endpoint")`
- `Get.resource("/endpoint/{id}")`
- `Put.to("/endpoint/{id}")`

### 4. Wrong Task Design

**Problem**: `ManageEventLifecycle` task violated Single Responsibility Principle.

**Solution**: Create separate Task classes:
- `CreateEvent`
- `GetEvent`
- `UpdateEvent`
- `DeactivateEvent`

### 5. Missing Response Validation

**Problem**: Questions didn't use `SerenityRest.lastResponse()`.

**Solution**: All validations use:
```java
SerenityRest.lastResponse().jsonPath().getString("id")
```

## Technology Stack Confirmation

| Technology | Version | Source |
|------------|---------|--------|
| Serenity BDD | 5.3.2 | build.gradle |
| Serenity Screenplay | 5.3.2 | build.gradle |
| Serenity Screenplay REST | 5.3.2 | build.gradle |
| Cucumber | 7.34.2 | build.gradle |
| REST Assured | 5.4.0 | build.gradle |
| JUnit 5 | 6.0.3 | build.gradle |

## API Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | /events | Create event | 201 |
| GET | /events/{id} | Get event | 200 |
| PUT | /events/{id} | Update event | 200 |
| POST | /events/{id}/deactivate | Deactivate event | 200 |

## Conclusion

All corrections identified and documented. Implementation can proceed with correct Screenplay Pattern approach.