@global
Feature: Keyworker assignments

  Acceptance Criteria:
  A logged in staff user view all their offender assignments on a per caseload basis

  Background:
    Given a user has authenticated with the API

  Scenario: A staff user views their assignments for a specified caseload
    When I view my assignments
    Then "15" total keyworker assignments records are returned


