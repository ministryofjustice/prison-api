Feature: Booking Aliases

  Acceptance Criteria:
  A logged in staff user can retrieve aliases for an offender booking.

  Scenario Outline: Retrieve aliases for an offender booking
    Given a user has logged in with username "renegade" and password "password"
    When aliases are requested for an offender booking "<booking id>"
    Then "<number>" aliases are returned
    And alias first names match "<alias first name list>"
    And alias last names match "<alias last name list>"

    Examples:
       | booking id | number | alias first name list | alias last name list |
       | -12        | 1      | DANNY                 | SMILEY               |
       | -9         | 2      | CHESNEY,CHARLEY       | THOMSON,THOMPSON     |

  Scenario: Aliases are requested for booking that does not exist
    Given a user has logged in with username "renegade" and password "password"
    When aliases are requested for an offender booking "-99"
    Then resource not found response is received from offender aliases API

  Scenario: Aliases are requested for booking that is not part of any of logged on staff user's caseloads
    Given a user has a token name of "RENEGADE_USER"
    When aliases are requested for an offender booking "-39"
    Then resource not found response is received from offender aliases API

  Scenario: Aliases are requested for booking that is not part of any of logged on staff user's caseloads but has global search
    Given a user has a token name of "VIEW_PRISONER_DATA"
    When aliases are requested for an offender booking "-39"
    Then alias first names match "CHESNEY"