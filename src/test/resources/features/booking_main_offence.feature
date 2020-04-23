Feature: Booking Offences

  Acceptance Criteria
  A logged on staff user can retrieve details of offences for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve multiple main offences using POST
    Given a user has a token name of "SYSTEM_USER_READ_WRITE"
    When a sentence with booking ids -1,-7 is requested using POST
    Then 3 offence records are returned
    And for "1st" offence record, offence code is "RC86355", statute code is "RC86"
    And for "2nd" offence record, offence code is "RC86360", statute code is "RC86"
    And for "3rd" offence record, offence code is "RV98011", statute code is "RV98"

  Scenario: Retrieve multiple main offences using POST with no results
    Given a user has a token name of "SYSTEM_USER_READ_WRITE"
    When a sentence with booking ids -98,-99 is requested using POST
    Then 0 offence records are returned
