Feature: Booking Activities

  Acceptance Criteria
  A logged on staff user can retrieve scheduled activities for an offender booking.

  Background:
    Given a user has authenticated with the API and has the pay role

  Scenario: Retrieve scheduled activities for an existing offender that is not in a caseload accessible to authenticated user
    When scheduled activities are requested for an offender with booking id "-16"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve scheduled activities for an offender that does not exist
    When scheduled activities are requested for an offender with booking id "-99"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve scheduled activities for an existing offender having no activities
    When scheduled activities are requested for an offender with booking id "-9"
    Then response from booking activities API is an empty list

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities
    When scheduled activities are requested for an offender with booking id "-1"
    Then "10" activities are returned
    And "16" activities in total are available
    And booking id for all activities is "-1"
    And event class for all activities is "INT_MOV"
    And event status for all activities is "SCH"
    And event type for all activities is "PRISON_ACT"
    And event type description for all activities is "Prison Activities"
    And event source for all activities is "PA"
    And event sub type for "1st" returned activity is "CHAP"
    And event sub type for "9th" returned activity is "EDUC"
    And event sub type description for "2nd" returned activity is "Education"
    And event sub type description for "10th" returned activity is "Chaplaincy"
    And event date for "3rd" returned activity is "2017-09-12"
    And event date for "9th" returned activity is "2017-09-14"
    And start time for "4th" returned activity is "2017-09-12 13:05:00"
    And start time for "8th" returned activity is "2017-09-14 09:30:00"
    And end time for "6th" returned activity is "2017-09-13 11:30:00"
    And end time for "7th" returned activity is "2017-09-13 15:00:00"
    And event location for "1st" returned activity is "Chapel"
    And event location for "10th" returned activity is "Chapel"
    And event source code for "3rd" returned activity is "CC1"
    And event source code for "9th" returned activity is "SUBS"
    And event source description for "6th" returned activity is "Chapel Cleaner"
    And event source description for "7th" returned activity is "Substance misuse course"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, some of which they have attended
    When scheduled activities are requested for an offender with booking id "-3"
    Then "10" activities are returned
    And "18" activities in total are available
    And start time for "1st" returned activity is "2017-09-11 13:00:00"
    And event status for "1st" returned activity is "COMP"
    And event status for "2nd" returned activity is "SCH"
    And event status for "3rd" returned activity is "EXP"
    And event status for "5th" returned activity is "SCH"
    And event status for "6th" returned activity is "CANC"
    And event status for "8th" returned activity is "SCH"
    And event status for "9th" returned activity is "SCH"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, some of which they are excluded from - by day
      CRS_SCH_ID=-25 on Friday 2017-09-29 is omitted
    When scheduled activities are requested for an offender with booking id "-4"
    Then "4" activities are returned

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, some of which they are excluded from - by day and slot
    When scheduled activities are requested for an offender with booking id "-5"
    Then "10" activities are returned

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, from a specified date
    When scheduled activities from "2017-09-12" are requested for an offender with booking id "-1"
    Then "10" activities are returned
    And "14" activities in total are available
    And start time for "1st" returned activity is "2017-09-12 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "10th" returned activity is "2017-09-18 11:30:00"
    And event source description for "10th" returned activity is "Chapel Cleaner"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, to a specified date
    When scheduled activities to "2017-09-12" are requested for an offender with booking id "-1"
    Then "5" activities are returned
    And start time for "1st" returned activity is "2017-09-11 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "5th" returned activity is "2017-09-12 15:00:00"
    And event source description for "5th" returned activity is "Substance misuse course"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, between specified dates
    When scheduled activities between "2017-09-12" and "2017-09-18" are requested for an offender with booking id "-1"
    Then "10" activities are returned
    And start time for "1st" returned activity is "2017-09-12 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "10th" returned activity is "2017-09-18 11:30:00"
    And event source description for "10th" returned activity is "Chapel Cleaner"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, sorted by descending activity end date
    When scheduled activities, sorted by "endTime" in "descending" order, are requested for an offender with booking id "-1"
    Then "10" activities are returned
    And "16" activities in total are available
    And start time for "1st" returned activity is "2017-09-22 09:30:00"
    And event source description for "1st" returned activity is "Chapel Cleaner"
    And end time for "10th" returned activity is "2017-09-13 15:00:00"
    And event source description for "10th" returned activity is "Substance misuse course"

  Scenario: Retrieve scheduled activities for an existing offender having one or more activities, using a fromDate later than toDate
    When scheduled activities between "2017-09-18" and "2017-09-12" are requested for an offender with booking id "-1"
    Then bad request response, with "Invalid date range: toDate is before fromDate." message, is received from booking activities API

# Pay is Nomis-only for now due to the "offender id to booking id mapping sql" using nomis-specific booking_seq column
  # step not implemented so marked as broken
  @broken
  Scenario: Pay an activity and reject double payment
        Offender id A1234AC has 2 activities scheduled on 2017-09-12 PM with eventId -6 and -7
    When a request is made to update attendance for offender id "A1234AC" and activity "-6" with outcome "ATT", performance "STANDARD" and comment "blah"
    Then the booking activities request is successful
    And the saved attendance details can be retrieved correctly
    When a request is made to update attendance for offender id "A1234AC" and activity "-7" with outcome "ATT", performance "STANDARD" and comment "blah"
    Then the booking activity is rejected as offender has already been paid for "Substance misuse course"

  Scenario: Pay an activity with invalid outcome dropdown
    When a request is made to update attendance for offender id "A1234AC" and activity "-2" with outcome "invalid", performance "GOOD" and comment "blah"
    Then bad request response, with "Event outcome value invalid does not exist" message, is received from booking activities API

  Scenario: Pay an activity with invalid performance dropdown
    When a request is made to update attendance for offender id "A1234AC" and activity "-2" with outcome "UNBEH", performance "invalid" and comment "blah"
    Then bad request response, with "Performance value invalid does not exist" message, is received from booking activities API

  Scenario: Pay an activity with incorrect ids
    When a request is made to update attendance for offender id "A1234AB" and activity "-2" with outcome "ATT", performance "STANDARD" and comment "blah"
    Then resource not found response is received from booking activities API

  Scenario: Pay an activity with unauthorised booking id
    When a request is made to update attendance for offender id "A1234AP" and activity "-2" with outcome "ATT", performance "STANDARD" and comment "blah"
    Then resource not found response is received from booking activities API

@broken
  Scenario: Pay an activity and reject double payment Booking id -3 has 2 activities scheduled on 2017-09-12 PM with eventId -6 and -7
    When a request is made to update attendance for booking id "-3" and activity "-7" with outcome "ATT", performance "STANDARD" and comment "blah"
    Then the booking activity is rejected as offender has already been paid for "Substance misuse course"
