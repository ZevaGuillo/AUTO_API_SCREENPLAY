# AUTO_API_SCREENPLAY

API Automation Testing Framework using **Java 21**, **Serenity BDD Screenplay Pattern**, and **Cucumber BDD** for testing a ticketing system's Event CRUD lifecycle.

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Serenity BDD | 5.3.2 | Reporting & Screenplay pattern |
| Serenity Screenplay | 5.3.2 | Actor-based test design |
| Cucumber | 7.34.2 | BDD/Gherkin support |
| JUnit 5 | 6.0.3 | Test framework |
| REST Assured | 5.4.0 | HTTP client |
| Gradle | 8.10 | Build tool |

## Project Structure

```
AUTO_API_SCREENPLAY/
├── src/test/
│   ├── java/com/ticketing/
│   │   ├── runners/              # Cucumber test runners
│   │   ├── stepdefinitions/      # Cucumber step definitions
│   │   ├── hooks/                # @Before/@After hooks
│   │   ├── tasks/                # Screenplay Tasks (actions)
│   │   ├── questions/            # Screenplay Questions (assertions)
│   │   ├── models/               # Data models
│   │   └── abilities/            # Actor abilities
│   └── resources/
│       ├── features/            # Gherkin feature files
│       └── serenity.conf         # Serenity configuration
├── build.gradle                  # Gradle build config
└── settings.gradle               # Gradle settings
```

## Prerequisites

- **Java 21** or higher
- **Gradle 8.10** (wrapper included)
- Access to the API endpoints

## Quick Start

### 1. Clone and setup

```bash
git clone <repository-url>
cd AUTO_API_SCREENPLAY
```

### 2. Run tests with default settings

```bash
./gradlew test
```

Default base URL: `http://localhost:50001`

## Running Tests

### Basic Commands

```bash
# Run all tests
./gradlew test

# Run with custom base URL
./gradlew test -DbaseUrl=https://api-staging.example.com

# Clean, test, and generate aggregated reports
./gradlew clean test aggregate

# Run specific test class
./gradlew test --tests "com.ticketing.runners.EventLifecycleRunner"
```

### Environment Variables

| Property | Default | Description |
|----------|---------|-------------|
| `baseUrl` | `http://localhost:50001` | API base URL |

## Test Scenario

The test validates a complete CRUD lifecycle:

```
Feature: Complete CRUD lifecycle of an event via API

  Scenario: Validate full lifecycle of an event
    Given the catalog API is available
    When the admin creates a new event
    Then the event should be created successfully
    When the user retrieves the created event
    Then the event data should be correct
    When the admin updates the event information
    Then the event should reflect the updated data
    When the admin deactivates the event
    Then the event should be marked as inactive
    When the user retrieves the event again
    Then the event should remain inactive
```

## Screenplay Pattern

This project follows the **Screenplay Pattern** for maintainable, scalable tests.

### Components

| Component | Package | Responsibility |
|-----------|---------|----------------|
| **Tasks** | `tasks/` | Actions the actor performs |
| **Questions** | `questions/` | Queries to retrieve data |
| **Abilities** | `abilities/` | What actors can do |
| **Actors** | Step definitions | Perform tasks and ask questions |

### Available Tasks

| Task | HTTP Method | Endpoint |
|------|-------------|----------|
| `CreateEvent` | POST | `/admin/events` |
| `GetEvent` | GET | `/events/{id}` |
| `UpdateEvent` | PUT | `/admin/events/{id}` |
| `DeactivateEvent` | POST | `/admin/events/{id}/deactivate` |

## Reports

Serenity generates comprehensive HTML reports.

### Location

Reports are generated in: `target/site/serenity/`

### Generate Reports

```bash
# Generate single-page HTML report
./gradlew aggregate

# Or run full cycle
./gradlew clean test aggregate
```

### Report Contents

- Test results summary
- Step-by-step scenario execution
- Request/response logs
- Screenshots on failure
- JSON data extraction results

## Configuration

### serenity.conf

Located at: `src/test/resources/serenity.conf`

```properties
serenity {
    rest.default-base-url = "http://localhost:50001"
    logging = VERBOSE
    take.screenshots = FOR_FAILURES
    test.reports = target/site/serenity
    project.key = "TICKETING"
}
```

### Changing Base URL

**Option 1: Command line**
```bash
./gradlew test -DbaseUrl=http://other-host:8080
```

**Option 2: Environment variable**
```bash
BASE_URL=http://other-host:8080 ./gradlew test
```

## Dependencies

All dependencies are managed in `build.gradle`. Key dependencies:

- `serenity-core`, `serenity-cucumber`, `serenity-screenplay`, `serenity-screenplay-rest`
- `cucumber-junit-platform-engine`
- `junit-jupiter-engine`
- `rest-assured`
- `assertj-core`
- `webdrivermanager`
- `logback-classic`

## License

Internal project - Ticketing System API Testing
