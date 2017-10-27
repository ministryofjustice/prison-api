@global
Feature: Case Note Creation and Update

  Acceptance Criteria:
  A logged in staff user can create and update case notes for an existing offender booking

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized
    And I have created a case note with text of "Hello this is a new case note"

  Scenario: Create a case note
    When a case note is created for an existing offender booking:
      | type               | COMMS                                       |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note is successfully created
    And correct case note source is used

  Scenario: Update a case note
    When the created case note is updated with text "Updated Case Note"
    Then case note is successfully updated with "Updated Case Note"
    And the original text is not replaced

  Scenario: The logged on staff user's caseload does not include the booking id - create
    When a case note with booking id in different caseload is created
    Then resource not found response is received from caseload API

  Scenario: The logged on staff user's caseload does not include the booking id - update
    When a case note with booking id in different caseload is updated
    Then resource not found response is received from caseload API
