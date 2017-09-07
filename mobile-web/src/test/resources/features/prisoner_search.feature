@global
Feature: Prisoner Search

  Acceptance Criteria:
  A logged in staff user can search for prisoners across the entire prison system

  Background:
    Given a user has logged in with username "hpa_user" and password "password"

  Scenario: Search prisoners within a date of birth range
    When a search is made for prisoners with type "dobFrom" and value "1970-01-01" for range 0 -> 2
    Then "2" prisoner records are returned
    And  "3" total prisoner records are available

