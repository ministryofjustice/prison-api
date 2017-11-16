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

  Scenario Outline: Retrieve today's scheduled events for an offender
    When today's scheduled events are requested for an offender with booking id -1
    Then "<number>" events are returned
    And the start time order is "<startTimes>"
    Examples:
      | number | startTimes     |  
      | 0      | SMITH13        |
      | 1      | ROBERTSON      |

  Scenario Outline: Retrieve this week's scheduled events for an offender
    When this week's scheduled events are requested for an offender with booking id -1
    Then "<number>" events are returned
    And the start time order is "<startTimes>"
    Examples:
      | number | startTimes     |  
      | 0      | SMITH13        |
      | 1      | ROBERTSON      |

  Scenario Outline: Retrieve next week's scheduled events for an offender
    When next week's scheduled events are requested for an offender with booking id -1
    Then "<number>" events are returned
    And the start time order is "<startTimes>"
    Examples:
      | number | startTimes     |  
      | 0      | SMITH13        |
      | 1      | ROBERTSON      |
