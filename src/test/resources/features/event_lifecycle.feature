Feature: Complete CRUD lifecycle of an event via API

  Scenario: Create a new event
    Given the catalog API is available
    When the admin creates a new event
    Then the event should be created successfully

  Scenario: Retrieve a created event
    Given the catalog API is available
    And an event has been created
    When the user retrieves the created event
    Then the event data should be correct

  Scenario: Update an existing event
    Given the catalog API is available
    And an event has been created
    When the admin updates the event information
    Then the event should reflect the updated data

  Scenario: Deactivate an event
    Given the catalog API is available
    And an event has been created
    When the admin deactivates the event
    Then the event should be marked as inactive

  Scenario: A deactivated event remains inactive on retrieval
    Given the catalog API is available
    And an event has been created
    And the event has been deactivated
    When the user retrieves the event again
    Then the event should remain inactive