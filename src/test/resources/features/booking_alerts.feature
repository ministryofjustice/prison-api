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
       | -1        | 4      | XA,HC,RSS,XTACT |
       | -2        | 2      | HA,XTACT        |

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
    When alerts are requested for offender nos "A1234AA,A1234AF" and agency "LEI"
    Then "6" alerts are returned
    And alert details are returned as follows:
      | offenderNo | bookingId | alertId | alertType | alertTypeDescription | alertCode | alertCodeDescription    | comment               | dateCreated | dateExpires | expired | active | dateModified        |
      | A1234AF    | -6        | 1       | P         | MAPPP Case           | P1        | MAPPA Level 1 Case      | Alert Text 6          | today       |             | false   | true   | |
      | A1234AF    | -6        | 2       | X         | Security             | XTACT     | XTACT                   | Alert XTACT 6         | today       |             | false   | true   | 2006-12-10T03:52:25 |
      | A1234AA    | -1        | 1       | X         | Security             | XA        | Arsonist                | Alert Text 1-1        | 2020-06-01  |             | false   | true   | 2006-12-10T03:52:25 |
      | A1234AA    | -1        | 2       | H         | Self Harm            | HC        | Self Harm - Custody     | Alert Text 1-2        | 2020-06-01  |             | false   | true   | 2006-12-10T03:52:25 |
      | A1234AA    | -1        | 3       | R         | Risk                 | RSS       | Risk to Staff - Custody | Inactive Alert        | 2020-06-01  | 2020-06-01  | true    | false  | 2006-12-10T03:52:25 |
      | A1234AA    | -1        | 4       | X         | Security             | XTACT     | XTACT                   | Alert XTACT 1         | 2020-06-01  |             | false   | true   | 2006-12-10T03:52:25 |

  Scenario: Alerts are requested for multiple offender numbers without agency nor authorisation
    When alerts are requested for offender nos "A1234AA,A1234AF" and no agency
    Then access denied response is received from alert API

  Scenario: Alerts are requested for multiple offender numbers without agency but with system access
    When a user has a token name of "VIEW_PRISONER_DATA"
    And alerts are requested for offender nos "A1234AA,A1234AF" and no agency
    Then "6" alerts are returned
