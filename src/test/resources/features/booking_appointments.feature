Feature: Booking Appointments

  Acceptance Criteria
  A logged on staff user can create scheduled appointments for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Create a new appointment
    When A medical appointment is created for an existing offender with booking id "-4", tomorrow at "16:00", at location "-28"
    Then The appointment exists in the database

  Scenario: Create a new appointment with invalid comment
    When An appointment is created with an invalid comment
    Then bad request response, with "Value is too long: max length is 4000" message, is received from booking appointments API

  Scenario: Create a new appointment with invalid type
    When An appointment is created for an invalid type
    Then bad request response, with "Event type not recognised." message, is received from booking appointments API

  Scenario: Create a new appointment with invalid location
    When An appointment is created for an invalid location
    Then bad request response, with "Location does not exist or is not in your caseload." message, is received from booking appointments API

  Scenario: Create a new appointment with invalid booking id
    When An appointment is created for an invalid booking id
    Then resource not found response is received from booking appointments API
