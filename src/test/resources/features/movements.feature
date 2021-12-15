Feature: Movement

  Scenario: Retrieve a list of recent movements

    Acceptance Criteria:
    A batch system user can retrieve a list of offenders with recent movements

    Given a user has a token name of "GLOBAL_SEARCH"
    When a request is made to retrieve recent movements
    Then a correct list of records are returned

  Scenario Outline: Retrieve a list of recent movements for offenders
    Given a user has a token name of "VIEW_PRISONER_DATA"
    When a make a request for recent movements for "A6676RS" and "Z0021ZZ"
    Then the records should contain a entry for "<movementType>" "<fromDescription>" "<toDescription>" "<reasonDescription>" "<movementTime>"
    Examples:
      | movementType| fromDescription    | toDescription | reasonDescription                | movementTime |
      | TRN         |  Birmingham        |  Moorland     | Normal Transfer                  | 12:00:00     |
      | REL         |  Leeds             |  Outside      | Abscond End of Custody Licence   | 00:00:00     |

  Scenario: Get the establishment roll count for a prison

    Acceptance Criteria:
    A logged in user can retrieve a prison's establishment roll count

    Given a user has authenticated with the API
    When a request is made to retrieve the establishment roll count for an agency
    Then a valid list of roll count records are returned

  Scenario: Get the establishment unassigned roll count for a prison

    Acceptance Criteria:
    A logged in user can retrieve a prison's establishment unassigned roll count

    Given a user has authenticated with the API
    When a request is made to retrieve the establishment unassigned roll count for an agency
    Then a valid list of unassigned roll count records are returned

   Scenario:  Get brief information for offenders 'out today'.

     Given a user has authenticated with the API
     When a request is made to retrieve the 'offenders out' for agency "LEI" for "2000-02-12"
     Then the following rows should be returned:
     | firstName | lastName  | offenderNo | dateOfBirth | timeOut   | reasonDescription |
     | Nick      | Talbot    | Z0018ZZ    | 1970-01-01  | 12:00     | Normal Transfer   |


  Scenario Outline: Retrieve a list of en-route offenders
    Given a user has authenticated with the API
    When a request is made for en-route offenders for agency "LEI" on movement date "2017-10-12"
    Then the records should contain a entry for "<offenderNo>" "<lastName>" "<fromAgencyDescription>" "<toAgencyDescription>" "<reasonDescription>" "<movementTime>"
    Examples:
      | offenderNo  | fromAgencyDescription | toAgencyDescription  | movementTime   | reasonDescription    | lastName  |
      | A1183AD     |  Birmingham           | Leeds                |  15:00:00      | Normal Transfer      | DENTON    |
      | A1183SH     |  Birmingham           | Leeds                |  13:00:00      | Normal Transfer      | HEMP      |

  Scenario: Get brief information for offenders 'in today'.

    Given a user has authenticated with the API
    When a request is made to retrieve the 'offenders in' for agency "LEI" on date "2017-10-12"
    Then information about 'offenders in' is returned as follows:
    | offenderNo | bookingId | dateOfBirth | firstName | middleName | lastName | fromAgencyDescription | toAgencyDescription | fromAgencyId | toAgencyId   |  movementTime | movementDateTime  | location    |
    | A6676RS    |       -29 | 1945-01-10  | Neil      |            | Bradley  | Birmingham            | Leeds               | BMI          | LEI          | 10:45         |  2017-10-12T10:45 | Landing H/1 |


Scenario: Get brief information about offenders 'in today' specifically dealing with temporary absences
    Given a user has authenticated with the API
    When a request is made to retrieve the 'offenders in' for agency "LEI" on date "2018-01-01"
    Then information about 'offenders in' is returned as follows:
      | offenderNo | bookingId | dateOfBirth | firstName |  lastName | middleName |  toAgencyDescription  | toAgencyId |  movementTime   |   movementDateTime  | location    | fromCity |
      | A1181FF    |       -47 | 1980-01-02  | Janis     | Drp       |            | Leeds                 | LEI        | 00:00           |   2018-01-01T00:00 |             | Wadhurst |

  Scenario Outline: Get brief information about most recent movements, specifically dealing with temporary absences
    Given a user has a token name of "VIEW_PRISONER_DATA"
    When a make a request for recent movements for "A1181FF" and "A6676RS" for all movement types
    Then the records should contain a entry for "<offenderNo>" "<movementType>" "<fromDescription>" "<toDescription>" "<reasonDescription>" "<movementTime>" "<fromCity>" "<toCity>"
  Examples:
    |offenderNo | movementType | fromDescription    | toDescription | reasonDescription     | movementTime | fromCity  | toCity   |
    | A1181FF   | TAP          |                    |  Leeds        | Funerals And Deaths   | 00:00        |  Wadhurst |          |
    | A6676RS   | TAP          |   Leeds            |               | Funerals And Deaths   | 00:00        |           | Wadhurst |

  Scenario: Get offender in reception
    Given a user has authenticated with the API
    When a request is made to retrieve 'offenders in reception' for agency "MDI"
    Then information about 'offenders in reception' is returned as follows:
      | bookingId | offenderNo | dateOfBirth   | firstName | lastName|
      | -46       | A1181DD    | 1980-01-02    |  Amy      | Dude    |


  Scenario: Get a days movement count for a prison

  Acceptance Criteria:
  A logged in user can get the count of prisoners in and out on a specific day

    Given a user has authenticated with the API
    When a request is made to retrieve the movement counts for an "MDI" on "2000-08-16"
    Then "2" offenders are out today and "2" are in

  Scenario: Get information around an offender arriving and leaving multiple times on the same day

    Given a user has authenticated with the API

    When a request is made to retrieve the 'offenders in' for agency "MDI" on date "2000-08-16"
    Then information about 'offenders in' is returned as follows:
      | offenderNo | bookingId | dateOfBirth | firstName | middleName | lastName | fromAgencyDescription | toAgencyDescription | fromAgencyId   | toAgencyId    |  movementTime | movementDateTime  |location    |
      | A1181FF    |       -47 | 1980-01-02  | Janis     |            | Drp      | Outside               | Moorland            | OUT            | MDI           | 00:00         | 2000-08-16T00:00  |            |
      | A1181FF    |       -47 | 1980-01-02  | Janis     |            | Drp      | Court 1               | Moorland            | COURT1         | MDI           | 00:00         | 2000-08-16T00:00  |            |

    When a request is made to retrieve the 'offenders out' for agency "MDI" for "2000-08-16"
    Then the following rows should be returned:
        | firstName | lastName  | offenderNo | dateOfBirth   | timeOut   | reasonDescription |
        | Janis     | Drp       | A1181FF    | 1980-01-02    | 00:00     | Normal Transfer   |
        | Janis     | Drp       | A1181FF    | 1980-01-02    | 00:00     | Normal Transfer   |

    When a request is made to retrieve the 'offenders in' for agency "LEI" on date "2000-08-16"
    Then information about 'offenders in' is returned as follows:
      | offenderNo | bookingId | dateOfBirth | firstName | middleName | lastName | fromAgencyDescription   | toAgencyDescription | fromAgencyId   | toAgencyId   |  movementTime | movementDateTime     | location    |
      | A1181FF    |       -47 | 1980-01-02  | Janis     |            | Drp      | Moorland                | Leeds               | MDI            | LEI          | 00:00         |   2000-08-16T00:00   |             |

  Scenario Outline: Get the details of the external movements between two times for a list of agencies

    Given a user has a token name of "GLOBAL_SEARCH"
    When a request is made to retrieve events involving agencies "<agency1>" and "<agency2>" between "<fromTime>" and "<toTime>"
    Then the response should contain "<movementCount>" movements
    And the response should contain "<courtCount>" court events
    And the response should contain "<transferCount>" transfer events
    And the response should contain "<releaseCount>" release events
    And the response code should be "<responseCode>"
    And the presence of an error response is "<errorResponsePresent>"
    Examples:
      | agency1 | agency2  | fromTime            | toTime              | movementCount | courtCount | transferCount | releaseCount | responseCode | errorResponsePresent |
      | LEI     |          | 2019-05-01T11:00:00 | 2019-05-01T18:00:00 | 2             |    1       |      1        |     0        |  200         |  false               |
      | MDI     | LEI      | 2019-05-01T00:00:00 | 2019-05-01T00:00:00 | 0             |    0       |      0        |     1        |  200         |  false               |
      | LEI     | MDI      | 2019-05-01T11:00:00 | 2019-05-01T18:00:00 | 3             |    1       |      1        |     1        |  200         |  false               |
      | LEI     | MDI      | 2019-05-01T17:00:00 | 2019-05-01T11:00:00 | 0             |    0       |      0        |     0        |  400         |  true                |
      | INVAL   | INVAL    | 2019-05-01T11:00:00 | 2019-05-01T18:00:00 | 0             |    0       |      0        |     0        |  200         |  false               |
      | LEI     | LEI      | 2019-05-01TXX:XX:XX | 2019-05-01TXX:XX:XX | 0             |    0       |      0        |     0        |  400         |  true                |
      |         |          | 2019-05-01T11:00:00 | 2019-05-01T17:00:00 | 0             |    0       |      0        |     0        |  400         |  true                |
