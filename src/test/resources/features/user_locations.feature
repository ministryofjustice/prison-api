Feature: User Locations

  Acceptance Criteria:
  Return locations for logged in staff users based on user-related context information (e.g. number of caseloads/agencies):
    - only institution-level locations are returned for a logged in staff user with multiple caseloads (and, hence, multiple agencies)
    - only institution-level locations are returned for a logged in staff user with single caseload associated with multiple agencies
    - institution and wing-level locations are returned for a logged in staff user with a single caseload associated with single agency

  Scenario Outline: Retrieve user locations
    Given a user has a token name of "<token>"
    When a request is made to retrieve user locations non-residential, include non-res = "<include non-res>"
    Then "<number>" user locations are returned
    And user location agency ids are "<agency id>"
    And user location descriptions are "<description>"
    And user location prefixes are "<prefix>"

    Examples:
      | token         | include non-res | number | agency id                                               | description                                              | prefix                                                                                       |
      | NORMAL_USER   | false           | 9      | LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI                     | Block A,C,D,E,F,H,I,Leeds,S                              | LEI,LEI-A,LEI-C,LEI-D,LEI-E,LEI-F,LEI-H,LEI-I,LEI-S                                          |
      | API_TEST_USER | false           | 10     | BXI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI                 | Block A,Brixton,C,D,E,F,H,I,Leeds,S                      | BXI,LEI,LEI-A,LEI-C,LEI-D,LEI-E,LEI-F,LEI-H,LEI-I,LEI-S                                      |
      | NORMAL_USER   | true            | 13     | LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI     | Block A,C,Court,Cswap,D,E,F,H,I,Leeds,Recp,S,Tap         | LEI,LEI-A,LEI-C,LEI-COURT,LEI-CSWAP,LEI-D,LEI-E,LEI-F,LEI-H,LEI-I,LEI-RECP,LEI-S,LEI-TAP     |
      | API_TEST_USER | true            | 14     | BXI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI,LEI | Block A,Brixton,C,Court,Cswap,D,E,F,H,I,Leeds,Recp,S,Tap | BXI,LEI,LEI-A,LEI-C,LEI-COURT,LEI-CSWAP,LEI-D,LEI-E,LEI-F,LEI-H,LEI-I,LEI-RECP,LEI-S,LEI-TAP |