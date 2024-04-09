Feature: Booking Scheduled Events

  Acceptance Criteria
  A logged on staff user can retrieve scheduled events for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve scheduled events for an existing offender having no events
    When today's scheduled events are requested for an offender with booking id -9
    Then response from booking events API is an empty list

  Scenario: Retrieve today's scheduled events for an offender in correct time order
    When today's scheduled events are requested for an offender with booking id -3
    Then events are returned as follows:
      | eventType  | eventLocation      |
      | PRISON_ACT | Carpentry Workshop |
      | VISIT      | Chapel             |
      | VISIT      | Visiting Room      |
      | APP        | Medical Centre     |
      | APP        | Visiting Room      |
      | PRISON_ACT | Carpentry Workshop |
      | PRISON_ACT | Carpentry Workshop |
      | VISIT      |Carpentry Workshop  |

  Scenario: Retrieve this week's scheduled events for an offender in correct time order
    When this week's scheduled events are requested for an offender with booking id -3
    Then events are returned as follows:
# First group is same as today
      | eventType  | eventLocation      |
      | PRISON_ACT | Carpentry Workshop |
      | VISIT      | Chapel             |
      | VISIT      | Visiting Room      |
      | APP        | Medical Centre     |
      | APP        | Visiting Room      |
      | PRISON_ACT | Carpentry Workshop |
      | PRISON_ACT | Carpentry Workshop |
      | VISIT      | Carpentry Workshop |
      | APP        | Visiting Room      |
      | VISIT      | Carpentry Workshop |
      | VISIT      | Medical Centre    |
      | PRISON_ACT | Medical Centre     |
      | VISIT      | LEI-H              |
      | PRISON_ACT | Visiting Room      |
