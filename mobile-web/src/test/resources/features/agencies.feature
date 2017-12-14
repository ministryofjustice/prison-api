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
    Then "7" agency records are returned
    And "7" total agency records are available
## Need to fix EA-254 before we can test the result data
#    Then the returned agencies are as follows:
#      | agencyId | agencyType | description  | 
#      | ABDRCT   | CRT        | Court 2      |
#      | BMI      | INST       | BIRMINGHAM   |
#      | BXI      | INST       | BRIXTON      |
#      | COURT1   | CRT        | Court 1      |
#      | LEI      | INST       | LEEDS        |
#      | MUL      | INST       | MUL          |
#      | WAI      | INST       | THE WEARE    |

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
    When a request is submitted to retrieve location codes for agency "LEI" and event type "APP"
## Need to fix EA-254 before we can test the result data
#    Then the returned agency locations are as follows:
#      | locationId | description |
#      | -26        | LEI-CARP    |
#      | -25        | LEI-CHAP    |
#      | -27        | LEI-CRM1    |
#      | -29        | LEI-MED     |
