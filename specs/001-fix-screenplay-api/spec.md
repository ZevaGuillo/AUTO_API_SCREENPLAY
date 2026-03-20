# Feature Specification: Fix Screenplay API Test for Event CRUD Lifecycle

**Feature Branch**: `001-fix-screenplay-api`  
**Created**: 2026-03-19  
**Status**: Draft  
**Input**: User description: "Refactor Screenplay API Test: Fix NoStageException and implement proper Event CRUD lifecycle test with Screenplay pattern"

## User Scenarios & Testing *(mandatory)*

The focus of this feature is on fixing the test automation infrastructure rather than adding user-facing functionality. The user scenarios describe how QA engineers will use the improved test framework.

### User Story 1 - Fix Stage Initialization (Priority: P1)

QA engineers need the Screenplay Stage to be properly initialized before each test runs so that actor interactions work correctly without throwing NoStageException errors.

**Why this priority**: Without proper Stage initialization, no Screenplay tests can execute, making all API testing impossible.

**Independent Test**: Can be verified by running any test and confirming no NoStageException is thrown.

**Acceptance Scenarios**:

1. **Given** a Cucumber test is executed, **When** the @Before hook runs, **Then** the Screenplay Stage MUST be initialized with a Cast
2. **Given** Stage is initialized, **When** an actor is referenced, **Then** the actor should exist in the stage

---

### User Story 2 - Implement Shared State Management (Priority: P1)

QA engineers need to persist the event ID across test steps so that subsequent operations (retrieve, update, deactivate) can use the same event without hardcoding.

**Why this priority**: Without shared state, each step operates in isolation and cannot maintain the CRUD flow context.

**Independent Test**: Can be verified by checking that eventId is accessible in all subsequent steps after creation.

**Acceptance Scenarios**:

1. **Given** an event is created, **When** the response contains an ID, **Then** that ID MUST be stored in session variables
2. **Given** an event ID is stored, **When** a subsequent step needs it, **Then** the ID MUST be retrievable via session variable

---

### User Story 3 - Create Reusable Tasks for API Actions (Priority: P1)

QA engineers need encapsulated Task classes for each API operation so that step definitions remain clean and focused on business logic.

**Why this priority**: Without proper Task encapsulation, step definitions contain technical details and become hard to maintain.

**Independent Test**: Can be verified by importing Tasks in step definitions and executing API calls through them.

**Acceptance Scenarios**:

1. **Given** the API is available, **When** CreateEvent task is executed, **Then** a POST request MUST be sent to /events endpoint
2. **Given** an event exists, **When** GetEventById task is executed with an ID, **Then** a GET request MUST be sent to /events/{id}
3. **Given** an event exists, **When** UpdateEvent task is executed with ID and data, **Then** a PUT request MUST be sent to /events/{id}
4. **Given** an event exists, **When** DeactivateEvent task is executed with an ID, **Then** a POST request MUST be sent to /events/{id}/deactivate

---

### User Story 4 - Create Reusable Questions for Validations (Priority: P1)

QA engineers need encapsulated Question classes for each type of response validation so that assertions are reusable and readable.

**Why this priority**: Without proper Question encapsulation, validation logic leaks into step definitions making tests brittle.

**Independent Test**: Can be verified by using Questions in then steps and confirming they return correct values.

**Acceptance Scenarios**:

1. **Given** an API call was executed, **When** ResponseStatus.code() is used, **Then** it MUST return the HTTP status code
2. **Given** an API call was executed, **When** JsonPathValue.field() is used with a field name, **Then** it MUST return the field value from the response

---

### User Story 5 - Verify Complete CRUD Lifecycle (Priority: P2)

QA engineers need a complete end-to-end test that validates all CRUD operations work together in sequence.

**Why this priority**: This validates that all components work together and catches integration issues.

**Independent Test**: Can be verified by running the full scenario and checking all validations pass.

**Acceptance Scenarios**:

1. **Given** the catalog API is available, **When** admin creates an event, **Then** event should be created with 201 status
2. **Given** event was created, **When** user retrieves the event, **Then** event data should match what was created
3. **Given** event was retrieved, **When** admin updates the event, **Then** updated data should be reflected in response
4. **Given** event was updated, **When** admin deactivates the event, **Then** event should be marked as inactive
5. **Given** event was deactivated, **When** user retrieves the event again, **Then** event should remain inactive

---

### Edge Cases

- What happens when API returns an error status (4xx/5xx)?
- What happens when the event ID from response is null or empty?
- What happens when network connection fails during API call?
- What happens when JSON response body is malformed?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Test framework MUST initialize Screenplay Stage before each scenario using @Before hook
- **FR-002**: Test framework MUST provide Cast with at least Admin and User actors
- **FR-003**: Test framework MUST store event ID in Serenity session variables after creation
- **FR-004**: Test framework MUST retrieve event ID from session variables in subsequent steps
- **FR-005**: Test framework MUST provide CreateEvent task that sends POST to /events
- **FR-006**: Test framework MUST provide GetEventById task that sends GET to /events/{id}
- **FR-007**: Test framework MUST provide UpdateEvent task that sends PUT to /events/{id}
- **FR-008**: Test framework MUST provide DeactivateEvent task that sends POST to /events/{id}/deactivate
- **FR-009**: Test framework MUST provide ResponseStatus Question to retrieve HTTP status code
- **FR-010**: Test framework MUST provide JsonPathValue Question to retrieve JSON field values
- **FR-011**: All step definitions MUST use Tasks for API operations, not direct REST calls
- **FR-012**: All step definitions MUST use Questions for validations
- **FR-013**: Test MUST validate actual API response data, not hardcoded values

### Key Entities *(include if feature involves data)*

- **Event**: Represents an event in the ticketing system with fields: id, name, description, date, venue, isActive, capacity
- **Actor**: Represents test actors (Admin, User) with CallAnApi ability pointing to BASE_URL
- **SessionState**: Represents the shared state container for passing data between steps via Serenity.setSessionVariable()

### Critical Implementation Requirements

- **Stage Initialization**: MUST use @Before hook with `OnStage.setTheStage(Cast.whereEveryoneCan(CallAnApi.at(BASE_URL)))`
- **Native Interactions**: MUST use `Post.to()`, `Get.resource()`, `Put.to()` from Serenity Screenplay REST (NO custom interactions)
- **Response Access**: MUST use `SerenityRest.lastResponse()` for all validations
- **State Management**: MUST use `Serenity.setSessionVariable()` and `Serenity.sessionVariableCalled()`
- **Assertions**: MUST use `Ensure.that(...).isEqualTo(...)` instead of `assertThat(...)` for better Serenity reporting
- **Model Classes**: MUST use Event model class for request bodies (REST Assured auto-serializes)
- **Headers**: MUST set `contentType(ContentType.JSON)` and `accept(ContentType.JSON)` on all requests
- **Configurable BASE_URL**: MUST use `System.getProperty("baseUrl", "http://localhost:50001")` for CI/CD compatibility

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All test runs complete without NoStageException errors (100% success rate)
- **SC-002**: Event ID is successfully persisted across all CRUD steps in the scenario
- **SC-003**: Each API operation (create, retrieve, update, deactivate) is encapsulated in its own Task class
- **SC-004**: Each validation (status code, field value) is encapsulated in its own Question class
- **SC-005**: Step definitions contain no direct REST API calls
- **SC-006**: Full CRUD lifecycle test executes successfully with all assertions passing