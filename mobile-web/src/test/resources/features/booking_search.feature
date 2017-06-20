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
    Then "10" booking records are returned
    And  "12" total booking records are available

  Scenario Outline: Search based on full offender last name
    When a booking search is made with full last "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name     | number | first name list | middle name list |
      | ANDERSON | 2      | ARTHUR,GILLIAN  | BORIS,EVE        |
      | anderson | 2      | ARTHUR,GILLIAN  | BORIS,EVE        |
      | AnDersOn | 2      | ARTHUR,GILLIAN  | BORIS,EVE        |
      | UNKNOWN  | 0      |                 |                  |
      |          | 0      |                 |                  |

  Scenario Outline: Search based on partial offender last name
    When a booking search is made with partial last "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name | number | first name list        | middle name list |
      | AND% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | and% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | AnD% | 3      | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        |
      | XX%  | 0      |                        |                  |
      |      | 0      |                        |                  |

  Scenario Outline: Search based on full offender first name
    When a booking search is made with full first "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender last names match "<last name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name    | number | last name list | middle name list |
      | DONALD  | 2      | DUCK,TRUMP     |                  |
      | donald  | 2      | DUCK,TRUMP     |                  |
      | DoNAld  | 2      | DUCK,TRUMP     |                  |
      | UNKNOWN | 0      |                |                  |
      |         | 0      |                |                  |

  Scenario Outline: Search based on partial offender first name
    When a booking search is made with partial first "<name>" of existing offender
    Then "<number>" booking records are returned
    And offender last names match "<last name list>"
    And offender middle names match "<middle name list>"

    Examples:
      | name | number | last name list   | middle name list |
      | CH%  | 2      | CHAPLIN,THOMPSON | JAMES,JAMES      |
      | ch%  | 2      | CHAPLIN,THOMPSON | JAMES,JAMES      |
      | Ch%  | 2      | CHAPLIN,THOMPSON | JAMES,JAMES      |
      | XX%  | 0      |                  |                  |
      |      | 0      |                  |                  |

  Scenario Outline: Search based on offender first name and last name
    When a booking search is made with "<first name>" and "<last name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender last names match "<last name list>"

    Examples:
      | first name | last name | number | first name list      | last name list      |
      | DONALD     | TRUMP     | 1      | DONALD               | TRUMP               |
      | CHARLES    | CHAPLIN   | 1      | CHARLES              | CHAPLIN             |
      | JOHN       | DOE       | 0      |                      |                     |
      | DA%        | SMITH     | 2      | DANIEL,DARIUS        | SMITH,SMITH         |
      | DANIEL     | SM%       | 2      | DANIEL,DANIEL        | SMITH,SMELLEY       |
      | DA%        | SM%       | 3      | DANIEL,DANIEL,DARIUS | SMITH,SMITH,SMELLEY |
      |            | SM%       | 0      |                      |                     |
      | DA%        |           | 0      |                      |                     |
      |            |           | 0      |                      |                     |

  Scenario Outline: Search based on offender first name or last name
    When a booking search is made with "<first name>" or "<last name>" of existing offender
    Then "<number>" booking records are returned
    And offender first names match "<first name list>"
    And offender last names match "<last name list>"

    Examples:
      | first name | last name | number | first name list            | last name list            |
      | DONALD     | CHAPLIN   | 3      | CHARLES,DONALD,DONALD      | CHAPLIN,DUCK,TRUMP        |
      | CHARLES    | TRUMP     | 2      | CHARLES,DONALD             | CHAPLIN,TRUMP             |
      | JOHN       | DOE       | 0      |                            |                           |
      | DA%        | SMITH     | 4      | DANIEL,DANIEL,DARIUS,GILES | SMITH,SMITH,SMITH,SMELLEY |
      | DANIEL     | SM%       | 4      | DANIEL,DANIEL,DARIUS,GILES | SMITH,SMITH,SMITH,SMELLEY |
      | DA%        | SM%       | 4      | DANIEL,DANIEL,DARIUS,GILES | SMITH,SMITH,SMITH,SMELLEY |
