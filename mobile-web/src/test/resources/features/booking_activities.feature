@global
Feature: Booking Activities

  Acceptance Criteria
  A logged on staff user can retrieve scheduled activities for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve scheduled activities for an existing offender that is not in a caseload accessible to authenticated user
    When scheduled activities are requested for an offender with booking id "-16"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve current day's scheduled activities for an existing offender that is not in a caseload accessible to authenticated user
    When scheduled activities for current day are requested for an offender with booking id "-16"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve scheduled activities for an offender that does not exist
    When scheduled activities are requested for an offender with booking id "-99"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve current day's scheduled activities for an offender that does not exist
    When scheduled activities for current day are requested for an offender with booking id "-99"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve scheduled activities for an existing offender having no activities
    When scheduled activities are requested for an offender with booking id "-9"
    Then response from booking activities API is an empty list

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities
    When scheduled activities are requested for an offender with booking id "-1"
    Then "10" activities are returned
    And "15" activities in total are available
    And booking id for all activities is "-1"
    And event class for all activities is "INT_MOV"
    And event status for all activities is "SCH"
    And event type for all activities is "PRISON_ACT"
    And event type description for all activities is "Prison Activities"
    And event source for all activities is "PA"
    And event sub type for "1st" returned activity is "CHAP"
    And event sub type for "8th" returned activity is "EDUC"
    And event sub type description for "2nd" returned activity is "Education"
    And event sub type description for "9th" returned activity is "Chaplaincy"
    And event date for "3rd" returned activity is "2017-09-12"
    And event date for "8th" returned activity is "2017-09-14"
    And start time for "4th" returned activity is "2017-09-12 13:00:00"
    And start time for "7th" returned activity is "2017-09-14 09:30:00"
    And end time for "5th" returned activity is "2017-09-13 11:30:00"
    And end time for "6th" returned activity is "2017-09-13 15:00:00"
    And event location for "1st" returned activity is "Chapel"
    And event location for "10th" returned activity is "Classroom 1"
    And event source code for "3rd" returned activity is "CC1"
    And event source code for "8th" returned activity is "SUBS"
    And event source description for "5th" returned activity is "Chapel Cleaner"
    And event source description for "6th" returned activity is "Substance misuse course"

  Scenario: Retrieve current day's scheduled activities for an existing offender having no activities on current day
    When scheduled activities for current day are requested for an offender with booking id "-1"
    Then "0" activities are returned

  Scenario: Retrieve current day's scheduled activities for an existing offender having one or more activities on current day
    When scheduled activities for current day are requested for an offender with booking id "-2"
    Then "2" activities are returned

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, some of which they have attended
    When scheduled activities are requested for an offender with booking id "-3"
    Then "6" activities are returned

  @nomis
  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, some of which they are excluded from - by day
    When scheduled activities are requested for an offender with booking id "-4"
    Then "4" activities are returned

  @nomis
  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, some of which they are excluded from - by day and slot
    When scheduled activities are requested for an offender with booking id "-5"
    Then "8" activities are returned

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, from a specified date
    When scheduled activities from "2017-09-12" are requested for an offender with booking id "-1"
    Then "10" activities are returned
    And "13" activities in total are available
    And start time for "1st" returned activity is "2017-09-12 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "10th" returned activity is "2017-09-19 11:30:00"
    And event source description for "10th" returned activity is "Chapel Cleaner"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, to a specified date
    When scheduled activities to "2017-09-12" are requested for an offender with booking id "-1"
    Then "4" activities are returned
    And start time for "1st" returned activity is "2017-09-11 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "4th" returned activity is "2017-09-12 15:00:00"
    And event source description for "4th" returned activity is "Substance misuse course"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, between specified dates
    When scheduled activities between "2017-09-12" and "2017-09-18" are requested for an offender with booking id "-1"
    Then "9" activities are returned
    And start time for "1st" returned activity is "2017-09-12 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "9th" returned activity is "2017-09-18 11:30:00"
    And event source description for "9th" returned activity is "Chapel Cleaner"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, sorted by descending activity end date
    When scheduled activities, sorted by "endTime" in "descending" order, are requested for an offender with booking id "-1"
    Then "10" activities are returned
    And "15" activities in total are available
    And start time for "1st" returned activity is "2017-09-22 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "10th" returned activity is "2017-09-13 15:00:00"
    And event source description for "10th" returned activity is "Substance misuse course"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, using a fromDate later than toDate
    When scheduled activities between "2017-09-18" and "2017-09-12" are requested for an offender with booking id "-1"
    Then bad request response, with "Invalid date range: toDate is before fromDate." message, is received from booking activities API
