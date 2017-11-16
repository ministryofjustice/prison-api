@global
Feature: Locations

  Acceptance Criteria:
  A logged in staff user can find all locations available to them.
  A logged in staff user can retrieve details of a specific location available to them.
  A logged in staff user can retrieve a list of all offenders associated with a specific location available to them.

  Background:
    Given a user has authenticated with the API

  @nomis
  Scenario: Retrieve all available location records
    When a request is made to retrieve all locations available to the user
    Then "10" location records are returned
    And "29" total location records are available

  @global
  Scenario Outline: Retrieve a specific location record
    When a request is made to retrieve location with locationId of "<locationId>"
    Then "<number>" location records are returned
    And location type is "<type>"
    And description is "<description>"

    Examples:
      | locationId | number | type | description |
      | -1         | 1      | WING | Block A     |
      | -2         | 1      | LAND | Landing A/1 |
      | -3         | 1      | CELL | A-1-1       |

  Scenario: Request for specific location record that does not exist
    When a request is made to retrieve location with locationId of "-9999"
    Then resource not found response is received from locations API