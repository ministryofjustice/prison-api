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

  Scenario Outline: Search based on full offender last name
    Given a user has authenticated with the API
    When a booking search is made with full "<last name>" of existing offender
    Then expected "<number>" of offender records are returned

    Examples:
      | last name | number |
      | ANDERSON  | 1      |

  Scenario: Search based on partial offender last name
    Given PENDING a user has authenticated with the API
    When a booking search is made with partial last name of existing offender
    Then expected "<number>" of offender records are returned
