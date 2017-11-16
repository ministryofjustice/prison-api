@global @wip
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

#  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments
#    When scheduled appointments are requested for an offender with booking id "-1"
#    Then "10" appointments are returned
#    And "15" appointments in total are available
#    And booking id for all appointments is "-1"
#    And event class for all appointments is "INT_MOV"
#    And event status for all appointments is "SCH"
#    And event type for all appointments is "appointment"
#    And event type description for all appointments is "appointment"
#    And event source for all appointments is "VIS"
#    And event sub type for "1st" returned appointment is "appointment"
#    And event sub type for "4th" returned appointment is "appointment"
#    And event sub type description for "2nd" returned appointment is "appointments"
#    And event sub type description for "5th" returned appointment is "appointments"
#    And event date for "3rd" returned appointment is "2017-02-10"
#    And event date for "5th" returned appointment is "2017-04-10"
#    And start time for "4th" returned appointment is "2017-03-10 14:30:00"
#    And start time for "2nd" returned appointment is "2017-01-10 14:30:00"
#    And end time for "1st" returned appointment is "2016-12-11 15:30:00"
#    And end time for "4th" returned appointment is "2017-03-10 16:30:00"
#    And event location for "1st" returned appointment is "appointmenting Room"
#    And event location for "4th" returned appointment is "Chapel"
#    And event source code for "4th" returned appointment is "OFFI"
#    And event source code for "3rd" returned appointment is "SCON"
#    And event source description for "1st" returned appointment is "Social Contact"
#    And event source description for "4th" returned appointment is "Official appointment"
#
#  Scenario: Retrieve current day's scheduled appointments for an existing offender having no appointments on current day
#    When scheduled appointments for current day are requested for an offender with booking id "-2"
#    Then "0" appointments are returned
#
#  Scenario: Retrieve current day's scheduled appointments for an existing offender having one or more appointments on current day
#    When scheduled appointments for current day are requested for an offender with booking id "-3"
#    Then "2" appointments are returned
#
#  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, from a specified date
#    When scheduled appointments from "2017-05-01" are requested for an offender with booking id "-1"
#    Then "10" appointments are returned
#    And "10" appointments in total are available
#    And start time for "1st" returned appointment is "2017-05-10 14:30:00"
#    And event source description for "1st" returned appointment is "Official appointment"
#    And end time for "10th" returned appointment is "2017-12-12 15:30:00"
#    And event source description for "10th" returned appointment is "Social Contact"
#
#  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, to a specified date
#    When scheduled appointments to "2017-06-30" are requested for an offender with booking id "-1"
#    Then "7" appointments are returned
#    And start time for "1st" returned appointment is "2016-12-11 14:30:00"
#    And event source description for "7th" returned appointment is "Social Contact"
#    And end time for "4th" returned appointment is "2017-03-10 16:30:00"
#    And event source description for "4th" returned appointment is "Official appointment"
#
#  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, between specified dates
#    When scheduled appointments between "2017-03-10" and "2017-11-13" are requested for an offender with booking id "-1"
#    Then "10" appointments are returned
#    And "11" appointments in total are available
#    And start time for "1st" returned appointment is "2017-03-10 14:30:00"
#    And event source description for "1st" returned appointment is "Official appointment"
#    And end time for "10th" returned appointment is "2017-10-13 15:30:00"
#    And event source description for "10th" returned appointment is "Social Contact"
#
#  Scenario: Retrieve scheduled appointments for an existing offender having one or more appointments, sorted by descending appointment end date
#    When scheduled appointments, sorted by "endTime" in "descending" order, are requested for an offender with booking id "-1"
#    Then "10" appointments are returned
#    And "15" appointments in total are available
#    And start time for "1st" returned appointment is "2017-12-12 14:30:00"
#    And event source description for "1st" returned appointment is "Social Contact"
#    And end time for "10th" returned appointment is "2017-05-10 16:30:00"
#    And event source description for "10th" returned appointment is "Official appointment"
  