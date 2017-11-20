@global
Feature: Booking Appointments

  Acceptance Criteria
  A logged on staff user can retrieve scheduled appointments for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve scheduled appointments for an existing offender that is not in a caseload accessible to authenticated user
    When scheduled appointments are requested for an offender with booking id "-16"
    Then resource not found response is received from booking appointments API

  Scenario: Retrieve current day's scheduled appointments for an existing offender that is not in a caseload accessible to authenticated user
    When scheduled appointments for current day are requested for an offender with booking id "-16"
    Then resource not found response is received from booking appointments API

  Scenario: Retrieve scheduled appointments for an offender that does not exist
    When scheduled appointments are requested for an offender with booking id "-99"
    Then resource not found response is received from booking appointments API

  Scenario: Retrieve current day's scheduled appointments for an offender that does not exist
    When scheduled appointments for current day are requested for an offender with booking id "-99"
    Then resource not found response is received from booking appointments API

  Scenario: Retrieve scheduled appointments for an existing offender having no appointments
    When scheduled appointments are requested for an offender with booking id "-9"
    Then response from booking appointments API is an empty list

  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, using a fromDate later than toDate
    When scheduled appointments between "2017-09-18" and "2017-09-12" are requested for an offender with booking id "-1"
    Then bad request response, with "Invalid date range: toDate is before fromDate." message, is received from booking appointments API

  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments
    When scheduled appointments are requested for an offender with booking id "-1"
    Then "10" appointments are returned
    And "15" appointments in total are available
    And booking id for all appointments is "-1"
    And event class for all appointments is "INT_MOV"
    And event status for all appointments is "SCH"
    And event type for all appointments is "APP"
    And event type description for all appointments is "Appointment"
    And event source for all appointments is "APP"
    And event sub type for "1st" returned appointment is "MEDE"
    And event sub type for "8th" returned appointment is "CHAP"
    And event sub type description for "3rd" returned appointment is "Medical - Dentist"
    And event sub type description for "6th" returned appointment is "Medical - Psy"
    And event date for "3rd" returned appointment is "2017-03-15"
    And event date for "9th" returned appointment is "2017-08-15"
    And start time for "2nd" returned appointment is "2017-02-15 14:30:00"
    And start time for "7th" returned appointment is "2017-07-22 09:30:00"
    And end time for "4th" returned appointment is "2017-04-15 15:00:00"
    And end time for "10th" returned appointment is "2017-09-15 15:00:00"
    And event location for "1st" returned appointment is "Medical Centre"
    And event location for "2nd" returned appointment is "Wakefield"
    And event location for "7th" returned appointment is "Classroom 1"
    And event location for "8th" returned appointment is "LEEDS"

  Scenario: Retrieve current day's scheduled appointments for an existing offender having no appointments on current day
    When scheduled appointments for current day are requested for an offender with booking id "-2"
    Then "0" appointments are returned

  Scenario: Retrieve current day's scheduled appointments for an existing offender having one or more appointments on current day
    When scheduled appointments for current day are requested for an offender with booking id "-3"
    Then "2" appointments are returned

  Scenario: Retrieve this week's scheduled appointments for an existing offender having one or more appointments this week
    When scheduled appointments for this week are requested for an offender with booking id "-3"
    Then "5" appointments are returned

  Scenario: Retrieve next week's scheduled appointments for an existing offender having one or more appointments next week
    When scheduled appointments for next week are requested for an offender with booking id "-3"
    Then "2" appointments are returned

  @nomis
  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, from a specified date
    When scheduled appointments from "2017-05-01" are requested for an offender with booking id "-1"
    Then "10" appointments are returned
    And "11" appointments in total are available
    And start time for "1st" returned appointment is "2017-05-15 14:30:00"
    And start time for "7th" returned appointment is "2017-09-18 13:30:00"
    And event sub type for "1st" returned appointment is "MEDE"
    And event sub type for "7th" returned appointment is "IMM"
    And event location for "7th" returned appointment is "Birmingham Youth Court"
    And end time for "10th" returned appointment is "2017-12-15 15:00:00"

  @elite
  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, from a specified date
    When scheduled appointments from "2017-05-01" are requested for an offender with booking id "-1"
    Then "10" appointments are returned
    And "11" appointments in total are available
    And start time for "1st" returned appointment is "2017-05-15 14:30:00"
    And start time for "7th" returned appointment is "2017-09-18 13:30:00"
    And event sub type for "1st" returned appointment is "MEDE"
    And event sub type for "7th" returned appointment is "IMM"
    And event location for "7th" returned appointment is "Justice Avenue"
    And end time for "10th" returned appointment is "2017-12-15 15:00:00"

  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, to a specified date
    When scheduled appointments to "2017-07-30" are requested for an offender with booking id "-1"
    Then "7" appointments are returned
    And start time for "1st" returned appointment is "2017-01-15 14:30:00"
    And event sub type for "7th" returned appointment is "EDUC"
    And end time for "4th" returned appointment is "2017-04-15 15:00:00"
    And event location for "1st" returned appointment is "Medical Centre"

  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, between specified dates
    When scheduled appointments between "2017-03-10" and "2017-11-13" are requested for an offender with booking id "-1"
    Then "10" appointments are returned
    And "10" appointments in total are available
    And start time for "1st" returned appointment is "2017-03-15 14:30:00"
    And event sub type for "4th" returned appointment is "MEPS"
    And end time for "10th" returned appointment is "2017-10-15 15:00:00"
    And event location for "6th" returned appointment is "LEEDS"

  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, sorted by descending appointment end date
    When scheduled appointments, sorted by "endTime" in "descending" order, are requested for an offender with booking id "-1"
    Then "10" appointments are returned
    And "15" appointments in total are available
    And start time for "1st" returned appointment is "2017-12-25 09:00:00"
    And event sub type for "1st" returned appointment is "CHAP"
    And end time for "10th" returned appointment is "2017-06-15 15:00:00"
    And event location for "6th" returned appointment is "Medical Centre"
  