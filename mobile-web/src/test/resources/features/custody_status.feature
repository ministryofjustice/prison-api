@global
Feature: Custody Status

  Acceptance Criteria:
  A logged in user can retrieve a list of custody status records
  A logged in user can retrieve a specific custody status record

  Background:
    Given user "hpa_user" with password "password" has authenticated with the API

    @ignore
  @nomis
  Scenario: Retrieve a list of all custody status records
    When a request is made to retrieve all custody status records
    Then a list of records are returned

  @nomis
  Scenario: Retrieve a specific custody status record
    When a request is made to retrieve a specific custody status record
    Then a single record is returned
