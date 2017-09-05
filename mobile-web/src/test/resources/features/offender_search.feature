@global
Feature: Offender Search V2

  Acceptance Criteria:
  A logged in staff user can search for offenders across all their caseloads

  Background:
    Given a user has authenticated with the API

  Scenario: Search all offenders across all allowed locations
    When an offender search is made without prisoner name or ID and across all locations
    Then "10" offender records are returned
    And  "15" total offender records are available

  Scenario Outline: Search based on keywords
    When an offender search is made with keywords "<keywords>" of existing offender
    Then "<number>" offender records are returned
    And the offender first names match "<first name list>"
    And the offender middle names match "<middle name list>"
    And location name match "<living unit list>"

    Examples:
      | keywords | number | first name list         | middle name list | living unit list        |
      | ANDERSON | 2      | ARTHUR,GILLIAN          | BORIS,EVE        | LEI-A-1-1,LEI-H-1-5     |
      | DUCK     | 1      | DONALD                  |                  | LEI-A-1-10              |
      | anderson | 2      | ARTHUR,GILLIAN          | BORIS,EVE        | LEI-A-1-1,LEI-H-1-5     |
      | AnDersOn | 2      | ARTHUR,GILLIAN          | BORIS,EVE        | LEI-A-1-1,LEI-H-1-5     |
      | UNKNOWN  | 0      |                         |                  |                         |
      | CHESNEY  | 3      | CHARLEY,CHESTER,CHESNEY | JAMES            | LEI-H,LEI-A-1-5,LEI-H-1 |


  Scenario Outline: Search all offenders across a specified locations
    When an offender search is made for location "<location>"
    Then "<number>" total offender records are available

    Examples:
      | location  | number |
      | LEI-A     | 12     |
      | LEI-H     | 3      |
      | BXI       | 0      |
      | LEI       | 15     |
      | XXX       | 0      |

  Scenario Outline: Search based on keywords and locations
    When an offender search is made with keywords "<keywords>" in location "<location>"
    Then "<number>" offender records are returned
    And the offender first names match "<first name list>"
    And location name match "<living unit list>"

    Examples:
      | keywords | location | number | first name list         | living unit list              |
      | ANDERSON | LEI-A    | 1      | ARTHUR                  | LEI-A-1-1                     |
      | DUCK     | LEI-A-1  | 1      | DONALD                  | LEI-A-1-10                    |
      | ANDERSON | LEI-H    | 1      | GILLIAN                 | LEI-H-1-5                     |
      | anderson | LEI-RECP | 0      |                         |                               |
      | AN       | LEI      | 3      | ANTHONY,ARTHUR,GILLIAN  | LEI-A-1-1,LEI-A-1-2,LEI-H-1-5 |