@global
Feature: User Locations

  Acceptance Criteria:
  Return locations for logged in staff users based on user-related context information (e.g. number of caseloads/agencies):
    - only institution-level locations are returned for a logged in staff user with multiple caseloads (and, hence, multiple agencies)
    - only institution-level locations are returned for a logged in staff user with single caseload associated with multiple agencies
    - institution and wing-level locations are returned for a logged in staff user with a single caseload associated with single agency

  Scenario Outline: Retrieve user locations
    Given user "<username>" with password "<password>" has authenticated with the API
    When a request is made to retrieve user locations
    Then "<number>" user locations are returned
    And user location agency ids are "<agency id>"
    And user location descriptions are "<description>"
    And user location prefixes are "<prefix>"

    Examples:
      | username        | password | number | agency id           | description                                 | prefix              |
      | itag_user       | password | 5      | BXI,LEI,WAI,MDI,SYI | BRIXTON,LEEDS,THE WEARE,MOORLAND,SHREWSBURY | BXI,LEI,WAI,MDI,SYI |
      | api_test_user   | password | 2      | BXI,LEI             | BRIXTON,LEEDS                               | BXI,LEI             |
