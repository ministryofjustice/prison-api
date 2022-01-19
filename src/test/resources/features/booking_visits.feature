Feature: Booking Visits

  Acceptance Criteria
  A logged on staff user can retrieve scheduled visits for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve scheduled visits for an existing offender that is not in a caseload accessible to authenticated user
    When scheduled visits are requested for an offender with booking id "-16"
    Then resource not found response is received from booking visits API

  Scenario: Retrieve current day's scheduled visits for an existing offender that is not in a caseload accessible to authenticated user
    When scheduled visits for current day are requested for an offender with booking id "-16"
    Then resource not found response is received from booking visits API

  Scenario: Retrieve scheduled visits for an offender that does not exist
    When scheduled visits are requested for an offender with booking id "-99"
    Then resource not found response is received from booking visits API

  Scenario: Retrieve current day's scheduled visits for an offender that does not exist
    When scheduled visits for current day are requested for an offender with booking id "-99"
    Then resource not found response is received from booking visits API

  Scenario: Retrieve scheduled visits for an existing offender having no visits
    When scheduled visits are requested for an offender with booking id "-9"
    Then response from booking visits API is an empty list

  Scenario: Retrieve scheduled visits for an existing offender having one or more visits, using a fromDate later than toDate
    When scheduled visits between "2017-09-18" and "2017-09-12" are requested for an offender with booking id "-1"
    Then bad request response, with "Invalid date range: toDate is before fromDate." message, is received from booking visits API

  Scenario: Retrieve scheduled visits for an existing offender having one or more visits
    When scheduled visits are requested for an offender with booking id "-1"
    Then "10" visits are returned
    And "15" visits in total are available
    And booking id for all visits is "-1"
    And event class for all visits is "INT_MOV"
    And event status for all visits is present
    And event type for all visits is "VISIT"
    And event type description for all visits is "Visit"
    And event source for all visits is "VIS"
    And event sub type for "1st" returned visit is "VISIT"
    And event sub type for "4th" returned visit is "VISIT"
    And event sub type description for "2nd" returned visit is "Visits"
    And event sub type description for "5th" returned visit is "Visits"
    And event date for "3rd" returned visit is "2017-02-10"
    And event date for "5th" returned visit is "2017-04-10"
    And start time for "4th" returned visit is "2017-03-10 14:30:00"
    And start time for "2nd" returned visit is "2017-01-10 14:30:00"
    And end time for "1st" returned visit is "2016-12-11 15:30:00"
    And end time for "4th" returned visit is "2017-03-10 16:30:00"
    And event location for "1st" returned visit is "Visiting Room"
    And event location for "4th" returned visit is "Chapel"
    And event source code for "4th" returned visit is "OFFI"
    And event source code for "3rd" returned visit is "SCON"
    And event source description for "1st" returned visit is "Social Contact"
    And event source description for "4th" returned visit is "Official Visit"

  Scenario: Retrieve current day's scheduled visits for an existing offender having no visits on current day
    When scheduled visits for current day are requested for an offender with booking id "-2"
    Then "0" visits are returned

  Scenario: Retrieve current day's scheduled visits for an existing offender having one or more visits on current day
    When scheduled visits for current day are requested for an offender with booking id "-3"
    Then "3" visits are returned

  Scenario: Retrieve scheduled visits for an existing offender having one or more visits, from a specified date
    When scheduled visits from "2017-05-01" are requested for an offender with booking id "-1"
    Then "10" visits are returned
    And "10" visits in total are available
    And start time for "1st" returned visit is "2017-05-10 14:30:00"
    And event source description for "1st" returned visit is "Official Visit"
    And end time for "10th" returned visit is "2017-12-10 15:30:00"
    And event source description for "10th" returned visit is "Social Contact"

  Scenario: Retrieve scheduled visits for an existing offender having one or more visits, to a specified date
    When scheduled visits to "2017-06-30" are requested for an offender with booking id "-1"
    Then "7" visits are returned
    And start time for "1st" returned visit is "2016-12-11 14:30:00"
    And event source description for "7th" returned visit is "Social Contact"
    And end time for "4th" returned visit is "2017-03-10 16:30:00"
    And event source description for "4th" returned visit is "Official Visit"

  Scenario: Retrieve scheduled visits for an existing offender having one or more visits, between specified dates
    When scheduled visits between "2017-03-10" and "2017-11-13" are requested for an offender with booking id "-1"
    Then "10" visits are returned
    And "11" visits in total are available
    And start time for "1st" returned visit is "2017-03-10 14:30:00"
    And event source description for "1st" returned visit is "Official Visit"
    And end time for "10th" returned visit is "2017-10-13 15:30:00"
    And event source description for "10th" returned visit is "Social Contact"

  Scenario: Retrieve scheduled visits for an existing offender having one or more visits, sorted by descending visit end date
    When scheduled visits, sorted by "endTime" in "descending" order, are requested for an offender with booking id "-1"
    Then "10" visits are returned
    And "15" visits in total are available
    And start time for "1st" returned visit is "2017-12-10 14:30:00"
    And event source description for "1st" returned visit is "Social Contact"
    And end time for "10th" returned visit is "2017-05-10 16:30:00"
    And event source description for "10th" returned visit is "Official Visit"
