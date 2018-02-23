@nomis @wip
Feature: Key worker details

  Acceptance Criteria:
  A logged in staff user can retrieve details of a Key worker within their caseload.

  Background:
    Given a user has authenticated with the API

  Scenario: Request for key worker details
    When a key worker details request is made with staff id "-5"
    Then the key worker details are returned
    And the key worker has 4 allocations
    
  Scenario: Request for key worker details which does not exist
    When a key worker details request is made with staff id "-99"
    Then the key worker service returns a resource not found response with message "Key worker with id -99 not found"
