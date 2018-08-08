@nomis
Feature: Key worker details

  Acceptance Criteria:
  A logged in staff user can retrieve details of a Key worker within their caseload.

  Background:
    Given a user has authenticated with the API

  Scenario: Request for key worker allocations
    When a key worker allocations request is made with staff id "-5" and agency "LEI"
    Then the correct key worker allocations are returned
    And the key worker has 3 allocations

  Scenario: Request for key worker allocations for a different keyworker
    When a key worker allocations request is made with staff id "-4" and agency "LEI"
    Then the key worker has 2 allocations

  Scenario: Request for key worker allocations for a same keyworker any for a different agency
    When a key worker allocations request is made with staff id "-4" and agency "MDI"
    Then the key worker has 0 allocations

  Scenario: Request for key worker allocations for a different keyworker any for a different agency
    When a key worker allocations request is made with staff id "-6" and agency "MDI"
    Then the key worker has 1 allocations

  Scenario: Request for key worker allocations where key worker does not exist
    When a key worker allocations request is made with staff id "-99" and agency "LEI"
    Then the key worker service returns a resource not found response with message "Resource with id [-99] not found."

  Scenario: Request for key worker allocations for multiple staff Ids
    When a key worker allocations request is made with staff ids "-5,-4" and agency "LEI"
    Then the key worker has 5 allocations

  Scenario: Request for key worker allocations for multiple offender Nos
    When a key worker allocations request is made with nomis ids "A9876RS,A5576RS,A1176RS" and agency "LEI"
    Then the correct key worker allocations are returned
    And the key worker has 3 allocations

@wip
  Scenario: Request for key worker allocation history for multiple staff Ids
    When a key worker allocation history request is made with staff ids "-5,-4"
    Then the key worker has 12 allocation history entries

@wip
  Scenario: Request for key worker allocation history for multiple offender Nos
    When a key worker allocation history request is made with nomis ids "A9876RS,A5576RS,A1176RS,A1234AP"
    Then the key worker has 5 allocation history entries
