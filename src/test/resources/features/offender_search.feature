Feature: Offender Search V2

  Acceptance Criteria:
  A logged in staff user can search for offenders across all their caseloads

  Background:
    Given a user has authenticated with the API

  Scenario: Search all offenders across all allowed locations
    When an offender search is made without prisoner name or ID and across "LEI" location
    Then "10" offender records are returned
    And  "27" total offender records are available

  Scenario Outline: Search based on keywords
    When an offender search is made with keywords "<keywords>" in location "<location>"
    Then "<number>" offender records are returned
    And the offender first names match "<first name list>"
    And the offender middle names match "<middle name list>"
    And location name match "<living unit list>"

    Examples:
      | keywords             | location | number | first name list         | middle name list | living unit list   |
      | ANDERSON             | LEI      | 2      | ARTHUR,GILLIAN          | BORIS,EVE        | A-1-1,H-1-5        |
      | ARTHUR               | LEI      | 1      | ARTHUR                  | BORIS            | A-1-1              |
      | MATTHEWS             | LEI      | 1      | DONALD                  | JEFFREY          | A-1-10             |
      | anderson             | LEI      | 2      | ARTHUR,GILLIAN          | BORIS,EVE        | A-1-1,H-1-5        |
      | AnDersOn             | LEI      | 2      | ARTHUR,GILLIAN          | BORIS,EVE        | A-1-1,H-1-5        |
      | UNKNOWN              | LEI      | 0      |                         |                  |                    |
      | DONALD MATTHEWS      | LEI      | 1      | DONALD                  | JEFFREY          | A-1-10             |
      | A1234AB              | LEI      | 1      | GILLIAN                 | EVE              | H-1-5              |
      | ANDERSON, GILLIAN    | LEI      | 1      | GILLIAN                 | EVE              | H-1-5              |
      | ANDERSON GILLIAN     | LEI      | 1      | GILLIAN                 | EVE              | H-1-5              |

  Scenario Outline: Search all offenders across a specified locations
    When an offender search is made for location "<location>"
    Then "<number>" total offender records are available

    Examples:
      | location  | number |
      | LEI-A     | 11     |
      | LEI-H     | 14     |
      | BXI       | 0      |
      | LEI       | 27     |
      | XXX       | 0      |

  Scenario Outline: Search based on keywords and locations
    When an offender search is made with keywords "<keywords>" in location "<location>"
    Then "<number>" offender records are returned
    And the offender first names match "<first name list>"
    And location name match "<living unit list>"

    Examples:
      | keywords | location | number | first name list         | living unit list          |
      | ANDERSON | LEI-A    | 1      | ARTHUR                  | A-1-1                     |
      | ARTHUR   | LEI-A    | 1      | ARTHUR                  | A-1-1                     |
      | JONES    | LEI-A-1  | 1      | HARRY                   | A-1-4                     |
      | D SMITH  | LEI      | 2      | DANIEL,DARIUS           | A-1-6,A-1-7               |
      | SMITH D  | LEI      | 2      | DANIEL,DARIUS           | A-1-6,A-1-7               |
      | SMITH,D  | LEI      | 2      | DANIEL,DARIUS           | A-1-6,A-1-7               |
      | SMITH DAR| LEI      | 1      | DARIUS                  | A-1-7                     |
      | DAN SMITH| LEI      | 1      | DANIEL                  | A-1-6                     |
      | MATTHEWS | LEI-A-1  | 1      | DONALD                  | A-1-10                    |
      | ANDERSON | LEI-H    | 1      | GILLIAN                 | H-1-5                     |
      | anderson | LEI-RECP | 0      |                         |                           |
      | AN       | LEI      | 3      | ANTHONY,ARTHUR,GILLIAN  | A-1-1,A-1-2,H-1-5         |
      | G AN     | LEI      | 1      | GILLIAN                 | H-1-5                     |
      | AN A     | LEI      | 2      | ANTHONY,ARTHUR          | A-1-1,A-1-2               |
      | A1234AB  | LEI-H    | 1      | GILLIAN                 | H-1-5                     |

  Scenario Outline: Search all offenders across a specified locations and keywords
    When an offender search is made with keywords "<keywords>" in location "<location>"
    Then "<number>" total offender records are available

    Examples:
      | keywords           | location  | number |
      | A1234AB            | LEI-H     | 1      |
      | ANDERSON           | LEI-H     | 1      |
      | ANDERSON, GILLIAN  | LEI-H     | 1      |
      | ANDERSON GILLIAN   | LEI-H     | 1      |

  Scenario Outline: Search based on alerts with category
    When an offender search is made filtering by alerts "<alerts>" in location "<location>"
    Then "<number>" total offender records are available
    And the offender last names match "<last name list>"
    And the offender alerts match "<alert lists>"
    And the offender categories match "<categories>"

    Examples:
      | alerts | location | number | last name list  | alert lists       | categories |
      | SR     | LEI      | 1      | BATES           | SR,XTACT          | X          |
      | V46,P1 | LEI      | 2      | ANDREWS,MATTHEWS| V46,P1,XTACT,XTACT| C,Z        |
      | XA     | LEI      | 1      | ANDERSON        | XA,HC,XTACT       | LOW        |
      | RSS    | LEI      | 0      |                 |                   |            |

  Scenario Outline: Search based on date of birth ranges
    When an offender search is made in location "<location>" filtering between DOB between "<fromDob>" and "<toDob>"
    Then "<number>" total offender records are available
    And DOB match "<date of birth>"

    Examples:
      | location | fromDob    | toDob       |  number | date of birth                                                                |
      | LEI      | 1970-01-01 | 1972-01-01  |  5      | 1970-01-01,1970-01-01,1970-03-01,1970-12-30,1972-01-01                       |
      | LEI      | 1972-01-02 |             |  7      | 1974-01-01,1977-01-02,1979-12-31,1980-01-02,1986-06-01,1998-08-28,1999-10-27 |
      | LEI      |            | 1945-01-09  |  1      | 1945-01-09                                                                   |
