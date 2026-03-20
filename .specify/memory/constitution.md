# Ticketing API Automation Constitution

## Core Principles

### I. Screenplay Pattern
All test automation MUST follow the Screenplay Pattern with clear separation of Abilities, Tasks, Interactions, and Questions. Each layer MUST have single responsibility: Abilities represent actor capabilities, Tasks represent business actions, Interactions handle technical HTTP operations, and Questions validate responses.

### II. Serenity BDD Integration
All tests MUST use Serenity BDD for reporting and living documentation. Tests MUST produce actionable reports with screen capturing for failures and clear step-by-step trace of user journeys.

### III. Test-First BDD Approach
All test scenarios MUST be written in Gherkin format (Cucumber) before implementation. Feature files MUST contain business-readable scenarios that map directly to acceptance criteria.

### IV. Service Layer Abstraction
HTTP communication MUST be abstracted through custom Interactions layer. Direct REST calls in step definitions are prohibited; all requests MUST go through reusable Interaction classes (PostRequest, GetRequest, PutRequest).

### V. Single Responsibility Tasks
Each Task class MUST represent exactly one business action. Composite workflows MUST be orchestrated by combining individual Tasks. Task classes MUST NOT mix multiple operations.

## Technology Stack

**Language**: Java 11+
**Framework**: Serenity BDD 4.1.0
**Pattern**: Screenplay Pattern
**API Testing**: Serenity Rest (RestAssured 5.4.0)
**Build Tool**: Gradle
**BDD**: Cucumber

## Project Structure

The project MUST follow this structure:

```
src/test/java/com/ticketing/
├── abilities/          # Actor capabilities (CallCatalogApi, etc.)
├── tasks/             # Business actions (CreateEvent, GetEventById, etc.)
├── interactions/      # HTTP operations (PostRequest, GetRequest, etc.)
├── questions/         # Response validations (ResponseStatus, JsonPathValue, etc.)
├── models/            # Data models (Event, etc.)
├── stepdefinitions/   # Cucumber step definitions
└── runners/           # Test runners
```

## Development Workflow

All test automation MUST:
1. Define feature scenarios in Gherkin (.feature files)
2. Implement Step Definitions using Task classes
3. Create reusable Tasks for business actions
4. Build Interaction layer for HTTP operations
5. Add Questions for response validation
6. Execute via CucumberWithSerenity runner

## Governance

**Constitution Version**: This constitution supersedes all other practices.

**Amendments**: Any changes to this constitution MUST be documented with:
- Reason for change
- Impact assessment
- Migration plan if needed

**Compliance**: All PRs and reviews MUST verify alignment with these principles. Complexity beyond these guidelines MUST be justified in documentation.

**Version**: 1.0.0 | **Ratified**: 2026-03-19 | **Last Amended**: 2026-03-19