@global
Feature: Booking Scheduled Events

  Acceptance Criteria
  A logged on staff user can retrieve scheduled events for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve scheduled events for an offender that is not in a caseload accessible to authenticated user
    When today's scheduled events are requested for an offender with booking id -16
    Then resource not found response is received from booking events API

  Scenario: Retrieve scheduled events for an offender that does not exist
    When today's scheduled events are requested for an offender with booking id -99
    Then resource not found response is received from booking events API

  Scenario: Retrieve scheduled events for an existing offender having no events
    When today's scheduled events are requested for an offender with booking id -9
    Then response from booking events API is an empty list

  Scenario Outline: Retrieve today's scheduled events for an offender in correct time order
    When today's scheduled events are requested for an offender with booking id -3
    Then 6 events are returned
    And For index of <index>,
    Then the eventType is "<eventType>"
    And the eventLocation is "<eventLocation>"
    Examples:
      | index | eventType  | eventLocation      |
      | 0     | VISIT      | Chapel             |
      | 1     | VISIT      | Visiting Room      |
      | 2     | APP        | Medical Centre     |
      | 3     | APP        | Visiting Room      |
      | 4     | PRISON_ACT | Carpentry Workshop |
      | 5     | PRISON_ACT | Carpentry Workshop |

  Scenario Outline: Retrieve this week's scheduled events for an offender in correct time order
    When this week's scheduled events are requested for an offender with booking id -3
    Then 12 events are returned
     And For index of <index>,
    Then the eventType is "<eventType>"
    And the eventLocation is "<eventLocation>"
    Examples:
      | index | eventType  | eventLocation      | days-ahead-comment |
      | 0     | VISIT      | Chapel             | 0                  |
      | 1     | VISIT      | Visiting Room      | 0                  |
      | 2     | APP        | Medical Centre     | 0                  |
      | 3     | APP        | Visiting Room      | 0                  |
      | 4     | PRISON_ACT | Carpentry Workshop | 0                  |
      | 5     | PRISON_ACT | Carpentry Workshop | 0                  |
      | 6     | VISIT      | Carpentry Workshop | 1                  |
      | 7     | APP        | Visiting Room      | 1                  |
      | 8     | VISIT      | Medical Centre     | 2                  |
      | 9     | PRISON_ACT | Medical Centre     | 3                  |
      | 10    | VISIT      | Block H            | 4                  |
      | 11    | PRISON_ACT | Visiting Room      | 6                  |

  Scenario Outline: Retrieve next week's scheduled events for an offender in correct time order
    When next week's scheduled events are requested for an offender with booking id -3
    Then 5 events are returned
     And For index of <index>,
    Then the eventType is "<eventType>"
    And the eventLocation is "<eventLocation>"
    Examples:
      | index | eventType  | eventLocation      | days-ahead-comment |
      | 0     | APP        | Visiting Room      | 7                  |
      | 1     | VISIT      | Carpentry Workshop | 8                  |
      | 2     | PRISON_ACT | Medical Centre     | 9                  |
      | 3     | VISIT      | Classroom 1        | 10                 |
      | 4     | APP        | Visiting Room      | 12                 |
