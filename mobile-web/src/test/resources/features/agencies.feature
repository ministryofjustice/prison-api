@global
Feature: Agencies

  Acceptance Criteria
  A logged on staff user can obtain:
    - a list of all agencies.
    - a list of all locations associated with an agency.
    - a single agency details

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve all agencies
    When a request is submitted to retrieve all agencies
    Then the returned agencies are as follows:
      | agencyId | agencyType   | description      |
      | CABA | Bail             |  a |
      | CHAP | Baptism          |  a |
      | EDUC | Computers        |  a |
      | KWS  | Key Work Session |  a |
      | MEDE | Dentist          |  a |
      | RES  | Resolve          |  a |
   
  Scenario Outline: Retrieve agency details
    When a request is submitted to retrieve agency "<agencyId>"
    Then the returned agency agencyId is "<agencyId>"
    And the returned agency agencyType is "<agencyType>"
    And the returned agency description is "<description>"
    Examples:
      | agencyId | agencyType | description |
      | LEI      | INST       | LEEDS       |
      | WAI      | INST       | THE WEARE   |
    
  Scenario: Retrieve location codes for an agency
    When a request is submitted to retrieve location codes for agency "LEI"
    Then the returned agency locations are as follows:
      | locationId | description |
      | CABA | Bail              |
      | CHAP | Baptism           |
      | EDUC | Computers         |
      | KWS  | Key Work Session  |
      | MEDE | Dentist           |
      | RES  | Resolve           |
