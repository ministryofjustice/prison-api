@global @nomis
Feature: Custody Status

  Scenario: Retrieve a list of recent movements

    Acceptance Criteria:
    A batch system user can retrieve a list of offenders with recent movements

    Given a system client "batchadmin" has authenticated with the API
    When a request is made to retrieve recent movements
    Then a correct list of records are returned

  Scenario Outline: Retrieve a list of recent movements for offenders
    Given a user has authenticated with the API with "GLOBAL_SEARCH_USER" and "password"
    When a make a request for recent movements for "A6676RS" and "Z0021ZZ"
    Then the records should contain a entry for "<movementType>" and "<destination>"
    Examples:
      | movementType| destination|
      | TRN         | MDI        |
      | REL         | OUT        |

  Scenario: Get the establishment roll count for a prison

    Acceptance Criteria:
    A logged in user can retrieve a prison's establishment roll count

    Given a user has authenticated with the API
    When a request is made to retrieve the establishment roll count for an agency
    Then a valid list of roll count records are returned

  Scenario: Get the establishment unassigned roll count for a prison

    Acceptance Criteria:
    A logged in user can retrieve a prison's establishment unassigned roll count

    Given a user has authenticated with the API
    When a request is made to retrieve the establishment unassigned roll count for an agency
    Then a valid list of unassigned roll count records are returned

  Scenario: Get a days movement count for a prison

    Acceptance Criteria:
    A logged in user can get the count of prisoners in and out on a specific day

    Given a user has authenticated with the API
    When a request is made to retrieve the movement counts for an agency on "2017-08-16"
    Then valid movement counts are returned
