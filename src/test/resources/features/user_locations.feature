Feature: User Locations

  Acceptance Criteria:
  Return locations for logged in staff users based on user-related context information (e.g. number of caseloads/agencies):
    - only institution-level locations are returned for a logged in staff user with multiple caseloads (and, hence, multiple agencies)
    - only institution-level locations are returned for a logged in staff user with single caseload associated with multiple agencies
    - institution and wing-level locations are returned for a logged in staff user with a single caseload associated with single agency

  Scenario Outline: Retrieve user locations
    Given a user has a token name of "<token>"
    When a request is made to retrieve user locations
    Then "<number>" user locations are returned
    And user location agency ids are "<agency id>"
    And user location descriptions are "<description>"
    And user location prefixes are "<prefix>"

    Examples:
      | token         | number | agency id                                       | description                                    | prefix                                                                 |
      | NORMAL_USER   | 11     | LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI     | Block A,C,Cswap,D,E,F,H,I,Leeds,S,Recp         | LEI,LEI-A,LEI-C,LEI-CSWAP,LEI-D,LEI-E,LEI-F,LEI-H,LEI-I,LEI-S,RECP     |
      | API_TEST_USER | 12     | BXI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI | Block A,Brixton,C,Cswap,D,E,F,H,I,Leeds,S,Recp | BXI,LEI,LEI-A,LEI-C,LEI-CSWAP,LEI-D,LEI-E,LEI-F,LEI-H,LEI-I,LEI-S,RECP |