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