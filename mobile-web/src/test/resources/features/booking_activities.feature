@nomis
Feature: Booking Activities

  Acceptance Criteria
  A logged on staff user can retrieve scheduled activities for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve scheduled activities for an existing offender that is not in a caseload accessible to authenticated user.
    When scheduled activities are requested for an offender with booking id "-16"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve scheduled activities for an existing offender that does not have any scheduled activities.
    When scheduled activities are requested for an offender with booking id "-9"
    Then response is an empty list

  Scenario: Retrieve scheduled activities for an existing offender that has one or more scheduled activities.
    When scheduled activities are requested for an offender with booking id "-1"
    Then "10" activities are returned
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
    And event source code for "3rd" returned activitiy is "CC1"
    And event source code for "8th" returned activitiy is "SUBS"
    And event source description for "5th" returned activity is "Chapel Cleaner"
    And event source description for "6th" returned activity is "Substance misuse course"
