@global
Feature: Custody Status

  @nomis
  Scenario: Retrieve a list of recent movements

    Acceptance Criteria:
    A batch system user can retrieve a list of offenders with recent movements

    Given a system client "batchadmin" has authenticated with the API
    When a request is made to retrieve recent movements
    Then a correct list of records are returned

  @nomis
  Scenario: Get the establishment roll count for a prison

    Acceptance Criteria:
    A logged in user can retrieve a prison's establishment roll count

    Given a user has authenticated with the API
    When a request is made to retrieve the establishment roll count for an agency
    Then a valid list of roll count records are returned
