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

  Scenario: An alert requested for booking that is not part of any of logged on staff user's caseloads
    When alert is requested for an offender booking "-16" and alert id "-1"
    Then resource not found response is received from alert API

  Scenario: Alerts are requested for booking that is not part of any of logged on staff user's caseloads
    When alerts are requested for an offender booking "-16"
    Then resource not found response is received from alert API

  Scenario: An alert is requested for booking that does not exist
    When alert is requested for an offender booking "-99" and alert id "-1"
    Then resource not found response is received from alert API

  Scenario: Alerts are requested for booking that does not exist
    When alerts are requested for an offender booking "-99"
    Then resource not found response is received from alert API

  Scenario: Alerts are requested for multiple offender numbers
    When alerts are requested for offender nos "A1234AA,A1234AF"
    Then "4" alerts are returned
    And alert details are returned as follows:
      | offenderNo | bookingId | alertId | alertType | alertTypeDescription | alertCode | alertCodeDescription    | comment        | dateCreated | dateExpires | expired | active |
      | A1234AF    | -6        | 1       | P         | MAPPP Case           | P1        | MAPPA Level 1 Case      | Alert Text 6   | today       |             | false   | true   |
      | A1234AA    | -1        | 1       | X         | Security             | XA        | Arsonist                | Alert Text 1-1 | today       |             | false   | true   |
      | A1234AA    | -1        | 2       | H         | Self Harm            | HC        | Self Harm - Custody     | Alert Text 1-2 | today       |             | false   | true   |
      | A1234AA    | -1        | 3       | R         | Risk                 | RSS       | Risk to Staff - Custody | Inactive Alert | today       | today       | true    | false  |
