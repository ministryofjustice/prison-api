Feature: Keyworker assignments

  Acceptance Criteria:
  A logged in staff user view all their offender assignments on a per caseload basis

  Scenario: A staff user views their assignments for a specified caseload
    Given a user has a token name of "ELITE2_API_USER"
    When I view my assignments
    Then "10" keyworker assignments records are returned
    Then "10" total keyworker assignments records are returned

  Scenario: A different staff user views their assignments for a specified caseload
    Given a user has a token name of "API_TEST_USER"
    When I view my assignments
    Then "2" keyworker assignments records are returned
    Then "2" total keyworker assignments records are returned
