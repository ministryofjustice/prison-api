Feature: Case Notes

  Acceptance Criteria:
  A logged in staff user can create a case note for an existing offender booking

  Background:
    Given I have created a case note text of "Hello this is a new case note"

  Scenario: Create a case note
    Given a user has authenticated with the API
    When a case note is created for an existing offender booking:
    | type  | CNOTE  |
    | subType  | GEN  |
    | text  | A new case note (from Serenity BDD test)  |
    Then case note is successfully created

  Scenario: Update a case note
    Given a case note has already been created and the case note is updated with text "Updated Case Note"
    When case note is successfully updated
    Then the amended flag is set
