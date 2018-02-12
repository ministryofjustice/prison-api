@global
Feature: Agencies

  Acceptance Criteria
  A logged on staff user can obtain:
    - a list of all active agencies.
    - details for a specified active agency.
    - a list of all active locations associated with an active agency.
    - a list of all active locations associated with an active agency that can be used for a specified type of event.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve all agencies
    When a request is submitted to retrieve all agencies
    Then "10" agency records are returned
    And "10" total agency records are available
    Then the returned agencies are as follows:
      | agencyId | agencyType | description  |
      | ABDRCT   | CRT        | Court 2      |
      | BMI      | INST       | BIRMINGHAM   |
      | BXI      | INST       | BRIXTON      |
      | COURT1   | CRT        | Court 1      |
      | LEI      | INST       | LEEDS        |
      | MDI      | INST       | MOORLAND     |
      | MUL      | INST       | MUL          |
      | SYI      | INST       | SHREWSBURY   |
      | TRO      | INST       | TROOM        |
      | WAI      | INST       | THE WEARE    |

  Scenario Outline: Retrieve agency details
    When a request is submitted to retrieve agency "<agencyId>"
    Then the returned agency agencyId is "<agencyId>"
    And the returned agency agencyType is "<agencyType>"
    And the returned agency description is "<description>"
    Examples:
      | agencyId | agencyType | description |
      | LEI      | INST       | LEEDS       |
      | WAI      | INST       | THE WEARE   |


  Scenario: Retrieve all locations for an agency
    When a request is submitted to retrieve location codes for agency "LEI"
    Then "29" location records are returned for agency

  Scenario: Retrieve locations, for an agency, that can be used for appointments
    When a request is submitted to retrieve location codes for agency "LEI" and event type "APP"
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |

  Scenario: Retrieve locations, for an agency, that can be used for appointments, in descending order of description
    When a request is submitted to retrieve location codes for agency "LEI" and event type "APP" sorted by "userDescription" in "descending" order
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |
      
  Scenario: Retrieve locations, for an agency, that can be used for any events
    When a request is submitted to retrieve location codes for agency "LEI" for any events
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |
