Feature: NOMIS API V1

  Acceptance Criteria:
  A nomis api client can retrieve alerts for an offender.

  Scenario: Alerts are requested for an offender
    When a user has a token name of "SYSTEM_USER_READ_WRITE"
    And v1 alerts are requested for noms Id "A1234AA"
    Then 2 v1 alerts are returned
