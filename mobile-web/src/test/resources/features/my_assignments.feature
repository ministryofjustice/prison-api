@global
Feature: Keyworker assignments

  Acceptance Criteria:
  A logged in staff user view all their offender assignments on a per caseload basis

  Scenario: A staff user views their assignments for a specified caseload
    Given a user has logged in with username "itag_user" and password "password"
    When I view my assignments
    Then "10" keyworker assignments records are returned
    Then "10" total keyworker assignments records are returned

  Scenario: A different staff user views their assignments for a specified caseload
    Given a user has logged in with username "api_test_user" and password "password"
    When I view my assignments
    Then "5" keyworker assignments records are returned
    Then "5" total keyworker assignments records are returned


