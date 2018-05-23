@global
Feature: Custody Status

  Acceptance Criteria:
  A logged in user can retrieve a list of offenders with recent movements

  Background:
    Given a system client "batchadmin" has authenticated with the API

  @nomis
  Scenario: Retrieve a list of recent movements
    When a request is made to retrieve recent movements
    Then a correct list of records are returned
