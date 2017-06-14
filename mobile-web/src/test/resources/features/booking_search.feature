Feature: Booking Search

  Acceptance Criteria:
  A logged in staff user can search for bookings by providing a full offender last name.
  A logged in staff user can search for bookings by providing a partial offender last name.
  A logged in staff user can search for bookings by providing a full offender first name.
  A logged in staff user can search for bookings by providing a partial offender first name.
  A logged in staff user can search for bookings by providing a list of offender last names.
  A logged in staff user can search for bookings by providing a list of offender first names.
  A logged in staff user can search for bookings based on a matching first name and last name.
  A logged in staff user can search for bookings based on a matching first name or last name.

  Background:
    Given a user has authenticated with the API

  Scenario: Search all offenders
    When a booking search is made without any criteria
    Then all offender records are returned

  Scenario Outline: Search based on full offender last name
    When a booking search is made with full "<last name>" of existing offender
    Then expected "<number>" of offender records are returned
    And offender first names match "<first name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | last name | number | first name list | middle name list |
      | ANDERSON  | 2      | ARTHUR,GILLIAN  | BORIS,EVE        |

  Scenario Outline: Search based on partial offender last name
    When a booking search is made with partial "<last name>" of existing offender
    Then expected "<number>" of offender records are returned

    Examples:
      | last name | number |
      | AND%      | 3      |
