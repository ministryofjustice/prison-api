@global
Feature: Prison Contact Details

  Acceptance Criteria:
  A logged in user can retrieve a list of Prison contact details
  A logged in user can retrieve contact details for a given Prison

  Background:
    Given user "hpa_user" with password "password" has authenticated with the API

  @nomis
  Scenario: Retrieve a list of all Prison contact details
    When a request is made to retrieve all prison contact details
    Then a list of prison contact details are returned

  @nomis
  Scenario: Retrieve contact details for a specific Prison
    When a request is made to retrieve contact details for prison "BMI"
    Then a single prison contact details record is returned

  @nomis
  Scenario: Prison exists but has no associated address
    When a request is made to retrieve contact details for prison "WAI"
    Then a response of resource not found is received

  @elite
  Scenario: Endpoint returns dummy record when in Elite mode
    When a request is made to retrieve all prison contact details
    Then a dummy list of prison contact details are returned
