Feature: Case Notes

  Acceptance Criteria:
  A logged in staff user can create a case note for an existing offender booking

  Scenario: Create a case note
    Given a user has authenticated with the API
    When a case note is created for an existing offender booking:
    | type  | CNOTE  |
    | subType  | GEN  |
    | text  | A new case note (from Serenity BDD test)  |
    Then case note is successfully created
