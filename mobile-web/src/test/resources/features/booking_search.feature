@nomis
Feature: Booking Search

  Acceptance Criteria:
  A logged in staff user can search for bookings by providing a full offender last name (in upper, lower or mixed case).
  A logged in staff user can search for bookings by providing a partial offender last name (in upper, lower or mixed case).
  A logged in staff user can search for bookings by providing a full offender first name (in upper, lower or mixed case).
  A logged in staff user can search for bookings by providing a partial offender first name (in upper, lower or mixed case).
  A logged in staff user can search for bookings based on a matching first name and last name.
  A logged in staff user can search for bookings based on a matching first name or last name.

  Background:
    Given a user has authenticated with the API

  Scenario: Search all offenders
    When a booking search is made without any criteria
    Then all "9" offender records are returned

  Scenario Outline: Search based on full offender last name
    When a booking search is made with full last "<name>" of existing offender
    Then expected "<number>" of offender records are returned
    And offender first names match "<first name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name     | number | first name list | middle name list |
      | ANDERSON | 2      | ARTHUR,GILLIAN  | BORIS,EVE        |
      | anderson | 2      | ARTHUR,GILLIAN  | BORIS,EVE        |
      | AnDersOn | 2      | ARTHUR,GILLIAN  | BORIS,EVE        |

  Scenario Outline: Search based on partial offender last name
    When a booking search is made with partial last "<name>" of existing offender
    Then expected "<number>" of offender records are returned
    And offender first names match "<first name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name | number | first name list        | middle name list |
      | AND% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | and% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | AnD% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |

  Scenario Outline: Search based on full offender first name
    When a booking search is made with full first "<name>" of existing offender
    Then expected "<number>" of offender records are returned
    And offender last names match "<last name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name   | number | last name list | middle name list |
      | DONALD | 2      | DUCK,TRUMP     |                  |
      | donald | 2      | DUCK,TRUMP     |                  |
      | DoNAld | 2      | DUCK,TRUMP     |                  |

  Scenario Outline: Search based on partial offender first name
    When a booking search is made with partial first "<name>" of existing offender
    Then expected "<number>" of offender records are returned
    And offender last names match "<last name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name | number | last name list   | middle name list |
      | CH%  | 2      | CHAPLIN,THOMPSON | JAMES,JAMES      |
      | ch%  | 2      | CHAPLIN,THOMPSON | JAMES,JAMES      |
      | Ch%  | 2      | CHAPLIN,THOMPSON | JAMES,JAMES      |
