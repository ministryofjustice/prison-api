@nomis
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
    And alias ethnicities match "<alias ethnicity list>"

    Examples:
       | booking id | number | alias first name list | alias last name list | alias ethnicity list                          |
       | -9999      | 0      |                       |                      |                                               |
       | -12        | 1      | DANNY                 | SMILEY               | White: Irish                                  |
       | -9         | 2      | CHESNEY,CHARLEY       | THOMPSON,THOMSON     | White: British,Mixed: White and Black African |