@global
Feature: Case Notes

  Acceptance Criteria:
  A logged in staff user can create and updated case notes for an existing offender booking

  Background:
    Given a user has authenticated with the API
    And I have created a case note text of "Hello this is a new case note"

  Scenario: Create a case note
    Given a case note is created for an existing offender booking:
    | type  | CNOTE  |
    | subType  | GEN  |
    | text  | A new case note (from Serenity BDD test)  |
    Then case note is successfully created

  Scenario: Update a case note
    When the created case note is updated with text "Updated Case Note"
    Then case note is successfully updated with "Updated Case Note"
    And the original text is not replaced
