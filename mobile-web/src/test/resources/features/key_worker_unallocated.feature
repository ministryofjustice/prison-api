@global @nomis
Feature: Unallocated Offenders

  Acceptance Criteria:
  A logged in staff user can retrieve a list of unallocated offenders using an agency id or the derived agencies based
  on the staff user's caseload

  Background:
    Given a user has authenticated with the API

  Scenario: Request for unallocated offenders for specified agency
    When an unallocated offender request is made with agency id "LEI"
    Then a list of "7" unallocated offenders are returned
    And the list is sorted by lastName asc

  Scenario: Request for unallocated offenders without specified agency
    When an unallocated offender request is made
    Then a list of "7" unallocated offenders are returned
    And the list is sorted by lastName asc

  Scenario: Request for unallocated offenders for unauthorised agency
    When an unallocated offender request is made with agency id "ABC"
    Then a resource not found response is received with message "Agency with id ABC not found."

  Scenario: Request for unallocated offenders returns no results
    When an unallocated offender request is made with agency id "WAI"
    Then a list of "0" unallocated offenders are returned