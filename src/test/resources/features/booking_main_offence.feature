Feature: Booking Main Offence Detail

  Acceptence Criteria
  A logged on staff user can retrieve details of main offences for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve single main offence
    When a sentence with booking id -1 is requested
    Then 1 offence detail records are returned
    And offence description of "1st" offence detail record is "Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)"

  Scenario: Retrieve multiple main offences
    When a sentence with booking id -7 is requested
    Then 2 offence detail records are returned
    And offence description of "1st" offence detail record is "Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury"
    And offence description of "2nd" offence detail record is "Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point."

  Scenario: Booking id does not exist
    When a sentence with booking id -99 is requested
    Then resource not found response is received from sentence API

  Scenario: The logged on staff user's caseload does not include the booking id
    When a sentence with booking id -16 is requested
    Then resource not found response is received from sentence API

  Scenario: Request main offence details for offender that does not yet have any on record
    When a sentence with booking id -9 is requested
    Then 0 offence detail records are returned