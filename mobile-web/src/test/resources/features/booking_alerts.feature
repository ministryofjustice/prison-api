@global
Feature: Booking Aliases

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

# (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE)
#VALUES (now(), -1, -1, 1, 'X', 'XA', 'ACTIVE', 'N', null, 'Alert Text 1-1', 'LEI', 'INST');
#VALUES (now(), -1, -1, 2, 'H', 'HC', 'ACTIVE', 'N', null, 'Alert Text 1-2', 'LEI', 'INST');
#VALUES (now(), -1, -1, 3, 'R', 'RSS', 'INACTIVE', 'N', now(), 'Inactive Alert', 'LEI', 'INST');
#VALUES (now(), -2, -2, 1, 'H', 'HA', 'ACTIVE', 'N', null, 'Alert Text 2', 'LEI', 'INST');
#VALUES (now(), -3, -3, 1, 'S', 'SR', 'ACTIVE', 'N', null, 'Alert Text 3', 'LEI', 'INST');
#VALUES (now(), -4, -4, 1, 'R', 'ROM', 'ACTIVE', 'N', null, 'Alert Text 4', 'LEI', 'INST');
#VALUES (now(), -5, -5, 1, 'V', 'V46', 'ACTIVE', 'N', null, 'Alert Text 5', 'LEI', 'INST');
#VALUES (now(), -6, -6, 1, 'P', 'P1', 'ACTIVE', 'N', null, 'Alert Text 6', 'LEI', 'INST');
#VALUES (now(), -7, -7, 1, 'V', 'VOP', 'ACTIVE', 'N', null, 'Alert Text 7', 'LEI', 'INST');
#VALUES (now(), -8, -8, 1, 'X', 'XCU', 'ACTIVE', 'N', null, 'Alert Text 8', 'LEI', 'INST');
#VALUES (now(), -9, -9, 1, 'C', 'C1', 'ACTIVE', 'N', null, 'Alert Text 9', 'LEI', 'INST');
#VALUES (now(), -10, -10, 1, 'O', 'OIOM', 'INACTIVE', 'N', now(), 'Alert Text 10', 'LEI', 'INST');

  Scenario Outline: Retrieve alert for an offender booking
    When alert is requested for an offender booking "<bookingId>" and alert id "<alertId>"
    Then alert alertId is "<alertId>"
    And alert alertType is "<alertType>"
    And alert alertTypeDescription is "<alertTypeDescription>"
    And alert alertCode is "<alertCode>"
    And alert alertCodeDescription is "<alertCodeDescription>"
    And alert comment is "<comment>"
    And alert dateCreated is "<dateCreated>"
    And alert dateExpires is "<dateExpires>"
    And alert expired is "<expired>"

    Examples:
  | bookingId | alertId | alertType | alertTypeDescription | alertCode | alertCodeDescription      | comment      | dateCreated | dateExpires | expired |
  | -4        | -4      | R         | Risk                 | ROM       | OASys Serious Harm-Medium | Alert Text 4 | 2017-07-09  |             | false |
  | -8        | -8      | X         | Security             | XCU       | Controlled Unlock         | Alert Text 8 | 2017-07-10  |             | false |

