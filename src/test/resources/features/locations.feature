Feature: Locations

  Acceptance Criteria:
  A logged in staff user can find all locations available to them.
  A logged in staff user can retrieve details of a specific location available to them.
  A logged in staff user can retrieve a list of all offenders associated with a specific location available to them.
  A logged in staff user can retrieve the names of all location groups associated with an agency.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve a specific location record
    When a request is made to retrieve location with locationId of "<locationId>"
    Then "<number>" location records are returned
    And location type is "<type>"
    And description is "<description>"

    Examples:
      | locationId | number | type | description |
      | -1         | 1      | WING | Block A     |
      | -2         | 1      | LAND | Landing A/1 |
      | -3         | 1      | CELL | LEI-A-1-1       |

  Scenario: Request for specific location record that does not exist
    When a request is made to retrieve location with locationId of "-9999"
    Then resource not found response is received from locations API

  Scenario Outline: Retrieve a list of inmates at a specific agency location
    When a request is made at agency "<agencyCode>" to retrieve a list of inmates
    Then there are "<countInmates>" offenders returned

    Examples:
    | agencyCode | countInmates |
    | LEI        |  27          |
    | BMI        |  0           |

  Scenario Outline: Retrieve a list of inmates at specific agency to check convicted status
    When a request is made at agency "<agencyCode>" to retrieve a list of inmates
    Then there are "<countInmates>" offenders returned with the convicted status "<convictedStatus>"

    Examples:
      | agencyCode | countInmates | convictedStatus |
      | LEI        | 8            | Convicted       |
      | LEI        | 3            | Remand          |

    Scenario Outline: Retrieve a list of inmates queried by convicted status
      When a request is made at agency "<agencyCode>" to retrieve a list of inmates with a convicted status of "<convictedStatus>"
      Then there are "<countInmates>" offenders returned

      Examples:
      | agencyCode | convictedStatus | countInmates |
      | LEI        | Convicted       | 8            |
      | LEI        | Remand          | 3            |
