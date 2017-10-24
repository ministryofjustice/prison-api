@global
Feature: Booking Aliases

  Acceptance Criteria:
  A logged in staff user can retrieve aliases for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve aliases for an offender booking
    When aliases are requested for an offender booking "<booking id>"
    Then "<number>" aliases are returned
    And alias first names match "<alias first name list>"
    And alias last names match "<alias last name list>"

    Examples:
       | booking id | number | alias first name list           | alias last name list               |
       | -9999      | 0      |                                 |                                    |
       | -12        | 1      | DANNY                           | SMILEY                             |
       | -9         | 5      | CHARLEY,MARK,PAUL,SANJAY,TREVOR | BASIS,DEMUNK,SIMONS,SMITH,THOMPSON |