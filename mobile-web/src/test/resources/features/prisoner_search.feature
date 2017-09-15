@nomis
Feature: Prisoner Search

  Acceptance Criteria:
  A logged in staff user can search for prisoners across the entire prison system

  Scenario: Cannot perform global search without correct role
    Given a user has logged in with username "itag_user" and password "password"
    When a search is made for prisoners
    Then access is denied

  Scenario: Search prisoners within a date of birth range
    Given a user has logged in with username "hpa_user" and password "password"
    When a search is made for prisoners with DOB on or after 1970-01-01 for range 0 -> 2
    Then "2" prisoner records are returned
    And  "4" total prisoner records are available

  Scenario Outline: Search prisoners within a dates of birth range not allowing more than 10 years
    Given a user has logged in with username "hpa_user" and password "password"
    When a search is made for prisoners with DOB between "<dobFrom>" and "<dobTo>" for range 0 -> 100
    Then "<numberResults>" prisoner records are returned
    And the prisoners dob matches "<DOB>"

  Examples:
  | dobFrom    | dobTo      | numberResults | DOB                                                    |
  | 1970-01-01 | 1971-01-01 | 2             | 1970-01-01,1970-03-01                                  |
  | 1970-01-01 | 1980-01-01 | 4             | 1970-01-01,1970-03-01,1977-01-02,1979-12-31            |
  | 1970-01-01 | 1980-01-02 | 4             | 1970-01-01,1970-03-01,1977-01-02,1979-12-31            |
  | 1969-12-30 |            | 4             | 1969-12-30,1970-01-01,1970-03-01,1977-01-02            |
  | 1965-01-01 | 1970-01-02 | 3             | 1968-01-01,1969-12-30,1970-01-01                       |
  | 1970-01-01 |            | 4             | 1970-01-01,1970-03-01,1977-01-02,1979-12-31            |
  | 1990-01-01 | 2000-01-01 | 4             | 1995-08-21,1998-08-28,1998-11-01,1999-10-27            |
  | 1995-12-31 | 2000-01-01 | 3             | 1998-08-28,1998-11-01,1999-10-27                       |
  |            | 2000-01-01 | 4             | 1995-08-21,1998-08-28,1998-11-01,1999-10-27            |


  Scenario Outline: Search for prisoners by names
    Given a user has logged in with username "hpa_user" and password "password"
    When a search is made for prisoners with first name "<search-firstName>", middle names "<search-middleNames>" and last name "<search-lastName>"
    Then "<numberResults>" prisoner records are returned
    And the prisoners first names match "<firstNames>"
    And the prisoners middle names match "<middleNames>"
    And the prisoners last names match "<lastNames>"

    Examples:
      | search-firstName | search-middleNames | search-lastName | numberResults | firstNames          | middleNames    | lastNames              |
      |                  |                    | ANDERSON        | 2             |  ARTHUR,GILLIAN     | BORIS,EVE      | ANDERSON,ANDERSON      |
      |                  | JEFFREY ROBERT     |                 | 1             |  DONALD             | JEFFREY ROBERT | DUCK                   |
      | CHESNEY          |                    |                 | 1             |  CHESNEY            |                | THOMSON                |
      |                  |                    | WILLIS          | 0             |                     |                |                        |

  Scenario Outline: Search prisoners for a specified Date of Birth
    Given a user has logged in with username "hpa_user" and password "password"
    When a search is made for prisoners with date of birth of "<dob>"
    Then "<numberResults>" prisoner records are returned
    And the prisoners last names match "<lastNames>"

    Examples:
      | dob        | numberResults | lastNames        |
      | 1970-01-01 | 1             |  CHAPLIN         |
      | 1969-12-30 | 1             |  ANDERSON        |
      | 1999-10-27 | 1             |  BATES           |
      | 1959-10-28 | 0             |                  |

  Scenario Outline: Search prisoners for a CRO or PNC number
    Given a user has logged in with username "hpa_user" and password "password"
    When a search is made for prisoners with PNC number of "<pnc>" and/or CRO number of "<cro>"
    Then "<numberResults>" prisoner records are returned
    And the prisoners last names match "<lastNames>"

    Examples:
      | pnc        | cro        | numberResults | lastNames        |
      | PNC112233  |            | 1             | CHAPLIN          |
      | PNC112234  |            | 0             |                  |
      |            | CRO112233  | 1             | BATES            |
      |            | CRO112234  | 0             |                  |

