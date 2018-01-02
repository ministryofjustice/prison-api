@global
Feature: Location and Location Group Events

    Acceptance Criteria
    A logged on staff user can retrieve schedules for groups of prisoners.

Background:
    Given a user has authenticated with the API

Scenario: location group caseload not accessible
    Given an existing agency and location group
    And agency does not belong to a caseload accessible to current user
    When schedules are requested for agency and location group
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Resource with id [ZZGHI] not found."

Scenario: location group does not exist
    Given an agency which belongs to a caseload accessible to current user
    And location group does not exist for the agency
    When schedules are requested for agency and location group
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Group 'doesnotexist' does not exist for agencyId 'LEI'."

Scenario: no location group scheduled events
    Given no offender has any scheduled events for current day
    When schedules are requested for agency and location group
    Then schedules response is an empty list

Scenario: location group scheduled events in order
    Given one or more offenders have scheduled events for current day
    And offenders are located in a location that belongs to requested agency and location group
    When schedules are requested for a valid agency and location group
    Then response is a list of offender's schedules for the current day with size 7
    And returned schedules are ordered as defined by requested location group
    And returned schedules are only for offenders located in locations that belong to requested agency and location group

Scenario: location group AM timeslot
    Given one or more offenders have scheduled events for current day
    And offenders are located in a location that belongs to requested agency and location group
    When schedules are requested for a valid agency and location group with 'timeSlot' = 'AM'
    Then response is a list of offender's schedules for the current day with size 5
    And start time of all returned schedules is before 12h00
    And returned schedules are ordered as defined by requested location group
    And returned schedules are only for offenders located in locations that belong to requested agency and location group

Scenario: location group PM timeslot
    Given one or more offenders have scheduled events for current day
    And offenders are located in a location that belongs to requested agency and location group
    When schedules are requested for a valid agency and location group with 'timeSlot' = 'PM'
    Then response is a list of offender's schedules for the current day with size 2
    And start time of all returned schedules is on or after 12h00
    And returned schedules are ordered as defined by requested location group
    And returned schedules are only for offenders located in locations that belong to requested agency and location group

###############################################################

Scenario: location caseload not accessible
    Given an existing agency and location
    And agency does not belong to a caseload accessible to current user
    When schedules are requested for agency and location
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Resource with id [<agencyId>] not found."

Scenario: location does not exist
    Given an existing agency
    And agency belongs to a caseload accessible to current user
    And location does not exist for the agency
    When schedules are requested for agency and location
    Then schedules response is HTTP 404 resource not found
    And schedules response error message is "Resource with id [<locationId>] not found."

Scenario: no location scheduled events
    Given the location within the agency has no scheduled events for current day
    When schedules are requested for the agency and location
    Then schedules response is an empty list

Scenario: location scheduled events in order
    Given one or more offenders are due to attend a scheduled event on the current day at a location within an agency
    When schedules are requested for the agency and location
    Then response is a list of offender's schedules for the current day
    And returned schedules are ordered in ascending alphabetical order by offender last name (the first name)
    And returned schedules are only for offenders due to attend a scheduled event on current day for requested agency and location

Scenario: location AM timeslot
    Given one or more offenders are due to attend a scheduled event on the current day at a location within an agency
    When schedules are requested for the agency and location
    And request includes the 'timeSlot' query parameter with a value of 'AM'
    Then response is a list of offender's schedules for the current day
    And start time of all returned schedules is before 12h00
    And returned schedules are ordered in ascending alphabetical order by offender last name (the first name)
    And returned schedules are only for offenders due to attend a scheduled event on current day for requested agency and location

Scenario: location PM timeslot
    Given one or more offenders are due to attend a scheduled event on the current day at a location within an agency
    When schedules are requested for the agency and location
    And request includes the 'timeSlot' query parameter with a value of 'PM'
    Then response is a list of offender's schedules for the current day
    And start time of all returned schedules is on or after 12h00
    And returned schedules are ordered in ascending alphabetical order by offender last name (the first name)
    And returned schedules are only for offenders due to attend a scheduled event on current day for requested agency and location
