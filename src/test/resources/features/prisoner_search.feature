Feature: Prisoner Search

  Acceptance Criteria:
  A logged in staff user can search for prisoners across the entire prison system

  Scenario: Can perform global search with GLOBAL_SEARCH role
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with DOB on or after 1970-01-01 for range 0 -> 15
    Then "14" prisoner records are returned

  Scenario: Search prisoners within a date of birth range with SYSTEM role
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with DOB on or after 1970-01-01 for range 0 -> 2
    Then "2" prisoner records are returned
    And  "14" total prisoner records are available

  Scenario Outline: Search prisoners within a dates of birth range not allowing more than 10 years
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with DOB between "<dobFrom>" and "<dobTo>" for range 0 -> 100
    Then "<numberResults>" prisoner records are returned
    And the prisoners dob matches "<DOB>"

    Examples:
    | dobFrom    | dobTo      | numberResults | DOB                                                                                                                      |
    | 1970-01-01 | 1971-01-01 | 4             | 1970-01-01,1970-01-01,1970-03-01,1970-12-30                                                                              |
    | 1970-01-01 | 1980-01-01 | 14            | 1970-01-01,1970-01-01,1970-03-01,1970-12-30,1971-01-15,1972-01-01,1972-01-01,1974-01-01,1974-10-29,1975-12-25,1977-01-02,1977-07-07,1977-11-14,1979-12-31|
    | 1970-01-01 | 1980-01-02 | 14            | 1970-01-01,1970-01-01,1970-03-01,1970-12-30,1971-01-15,1972-01-01,1972-01-01,1974-01-01,1974-10-29,1975-12-25,1977-01-02,1977-07-07,1977-11-14,1979-12-31|
    | 1969-12-30 |            | 14            | 1969-12-30,1970-01-01,1970-01-01,1970-03-01,1970-12-30,1971-01-15,1972-01-01,1972-01-01,1974-01-01,1974-10-29,1975-12-25,1977-01-02,1977-07-07,1977-11-14|
    | 1965-01-01 | 1970-01-02 | 7             | 1966-01-01,1968-01-01,1968-01-01,1968-03-23,1969-12-30,1970-01-01,1970-01-01                                             |
    | 1970-01-01 |            | 14            | 1970-01-01,1970-01-01,1970-03-01,1971-01-15,1970-12-30,1972-01-01,1972-01-01,1974-01-01,1974-10-29,1975-12-25,1977-01-02,1977-07-07,1977-11-14,1979-12-31|
    | 1990-01-01 | 2000-01-01 | 4             | 1990-12-30,1991-06-04,1998-08-28,1999-10-27                                                        |
    | 1995-12-31 | 2000-01-01 | 2             | 1998-08-28,1999-10-27                                                                                         |
    |            | 2000-01-01 | 4             | 1990-12-30,1991-06-04,1998-08-28,1999-10-27                                                        |


  Scenario Outline: Search for prisoners by names, without partial name matching
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with first name "<firstName>", middle names "<middleNames>" and last name "<lastName>"
    Then "<numberResults>" prisoner records are returned
    And prisoner offender numbers match "<offenderNos>"
    And prisoner internal location match "<internalLocation>"

    Examples:
      | firstName | middleNames    | lastName  | numberResults | offenderNos     | internalLocation      |
      |           |                | ANDERSON  | 2             | A1234AA,A1234AB | LEI-A-1-1,LEI-H-1-5   |
      |           | EVE            | ANDERSON  | 1             | A1234AB         | LEI-H-1-5             |
      |           | JAMES          |           | 2             | A1234AD,A1234AI | LEI-A-1,LEI-A-1-5     |
      | CHESTER   | JAMES          |           | 1             | A1234AI         | LEI-A-1-5             |
      |           | JEFFREY ROBERT |           | 1             | A1234AE         | LEI-A-1-10            |
      | DANIEL    |                |           | 2             | A1234AJ,A1234AL | LEI-A-1-6,LEI-AABCW-1 |
      | DANIEL    | JOSEPH         |           | 1             | A1234AJ         | LEI-A-1-6             |
      |           |                | WILLIS    | 0             |                 |                       |
      |           |                | AND       | 0             |                 |                       |
      | CHES      |                |           | 0             |                 |                       |
      |           | JEFF           |           | 0             |                 |                       |
      |           |                | O'VAUGHAN | 1             | A1181MV         |                       |


  Scenario Outline: Search for prisoners by names, with partial name matching
    Given a user has a token name of "GLOBAL_SEARCH"
    When a partial name search is made for prisoners with first name "<firstName>", middle names "<middleNames>" and last name "<lastName>"
    Then "<numberResults>" prisoner records are returned
    And prisoner offender numbers match "<offenderNos>"
    And the prisoners first names match "<foundFirstNames>"
    And the prisoners middle names match "<foundMiddleNames>"
    And the prisoners last names match "<foundLastNames>"

    Examples:
      | firstName | middleNames    | lastName | numberResults | offenderNos             | foundFirstNames        | foundMiddleNames | foundLastNames            |
      |           |                | AND      | 3             | A1234AA,A1234AB,A1234AF | ARTHUR,GILLIAN,ANTHONY | BORIS,EVE        | ANDERSON,ANDERSON,ANDREWS |
      | CHES      |                |          | 1             | A1234AI                 | CHESTER                | JAMES            | THOMPSON                  |
      |           | JEFF           |          | 1             | A1234AE                 | DONALD                 | JEFFREY ROBERT   | MATTHEWS                  |

  Scenario Outline: Search prisoners for a specified Date of Birth
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with date of birth of "<dob>"
    Then "<numberResults>" prisoner records are returned
    And the prisoners last names match "<lastNames>"

    Examples:
      | dob        | numberResults | lastNames      |
      | 1970-01-01 | 2             | CHAPLIN,TALBOT |
      | 1969-12-30 | 1             | ANDERSON       |
      | 1999-10-27 | 1             | BATES          |
      | 1959-10-28 | 0             |                |

  Scenario Outline: Search for prisoners with specified offender number
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with an offender number of "<offenderNo>"
    Then "<numberResults>" prisoner records are returned
    And the prisoners last names match "<lastNames>"

    Examples:
      | offenderNo | numberResults | lastNames |
      | A1234AC    | 1             | BATES     |
      | A1476AE    | 0             |           |
      | A1181MV    | 1             | O'VAUGHAN |

  Scenario Outline: Search for prisoners with specified offender numbers using simple endpoint
     Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with offender numbers of "<offenderNos>" using simple endpoint
    Then "<numberResults>" prisoner records are returned
    And the prisoners last names match "<lastNames>"

    Examples:
      | offenderNos        | numberResults | lastNames       |
      | A1181MV,A1234AC    | 2             | O'VAUGHAN,BATES |

  Scenario: Search for prisoners without GLOBAL_SEARCH role
    Given a user has a token name of "NORMAL_USER"
    When a search is made for prisoners with an offender number of "<offenderNo>" expecting failure
    Then access is denied

  Scenario Outline: Search prisoners with a CRO number
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with CRO number of "<cro>"
    Then "<numberResults>" prisoner records are returned
    And the prisoners last names match "<lastNames>"

    Examples:
      | cro        | numberResults | lastNames |
      | CRO112233  | 1             | BATES     |
      | CRO112234  | 1             | BATES     |

  Scenario Outline: Search prisoners with a valid PNC number
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with PNC number of "<pnc>"
    Then "<numberResults>" prisoner records are returned
    And the prisoners last names match "<lastNames>"

    Examples:
      | pnc           | numberResults | lastNames |
      | 1998/1234567L | 1             | CHAPLIN   |
      | 1998/1234567D | 0             |           |
      | 98/1234567L   | 1             | CHAPLIN   |
      | 1898/1234567L | 0             |           |
      | 14/12345F     | 1             | ANDREWS   |
      | 2014/12345F   | 1             | ANDREWS   |
      | 1914/12345F   | 1             | ANDREWS   |

  Scenario: Search prisoners with an invalid PNC number
    Given a user has a token name of "GLOBAL_SEARCH"
    When an invalid search is made for prisoners with PNC number of "234/EE45FX"
    Then bad request response is received from prisoner search API

  Scenario: Search for OUT location prisoners
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with location of "OUT" and lastname of "ELBOW"
    Then "1" prisoner records are returned

  Scenario: Search for IN location prisoners
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with location of "IN" and lastname of "ELBOW"
    Then "0" prisoner records are returned

  Scenario: Search for ALL location prisoners
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with location of "ALL" and lastname of "ELBOW"
    Then "1" prisoner records are returned

  Scenario: Search for prisoners with invalid gender code
    Given a user has a token name of "GLOBAL_SEARCH"
    When a search is made for prisoners with invalid gender code of "ABC" and lastname of "SARLY"
    Then a bad request response is received from the prisoner search api with message "Gender filter value ABC not recognised."
