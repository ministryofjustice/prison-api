@global
Feature: Booking Alerts

  Acceptance Criteria:
  A logged in staff user can retrieve alerts for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve alerts for an offender booking
    When alerts are requested for an offender booking "<bookingId>"
    Then "<number>" alerts are returned
    And alerts codes match "<alert code list>"

    Examples:
       | bookingId | number | alert code list |
       | -1        | 3      | XA,HC,RSS       |
       | -2        | 1      | HA              |

  Scenario Outline: Retrieve alert for an offender booking
    When alert is requested for an offender booking "<bookingId>" and alert id "<alertId>"
    Then alert alertType is "<alertType>"
    And alert alertTypeDescription is "<alertTypeDescription>"
    And alert alertCode is "<alertCode>"
    And alert alertCodeDescription is "<alertCodeDescription>"
    And alert comment is "<comment>"
    And alert dateExpires is "<dateExpires>"
    And alert expired is "<expired>"

    Examples:
  | bookingId | alertId | alertType | alertTypeDescription | alertCode | alertCodeDescription      | comment      | dateExpires | expired |
  | -4        | 1       | R         | Risk                 | ROM       | OASys Serious Harm-Medium | Alert Text 4 |             | false   |
  | -8        | 1       | X         | Security             | XCU       | Controlled Unlock         | Alert Text 8 |             | false   |

  Scenario: The logged on staff user's caseload does not include the booking id
    When an alert with booking id in different caseload is requested
    Then resource not found response is received from alert API

  Scenario: The logged on staff user's caseload does not include the booking id
    When an alert list with booking id in different caseload is requested
    Then resource not found response is received from alert API

