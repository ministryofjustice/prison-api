Feature: Movement

  Scenario: Retrieve a list of recent movements

    Acceptance Criteria:
    A batch system user can retrieve a list of offenders with recent movements

    Given a user has a token name of "GLOBAL_SEARCH"
    When a request is made to retrieve recent movements
    Then a correct list of records are returned

  Scenario Outline: Retrieve a list of recent movements for offenders
    Given a user has a token name of "GLOBAL_SEARCH"
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

  Scenario: Get a days movement count for a prison

    Acceptance Criteria:
    A logged in user can get the count of prisoners in and out on a specific day

    Given a user has authenticated with the API
    When a request is made to retrieve the movement counts for an agency on "2017-08-16"
    Then "2" offenders are out today and "0" are in

   Scenario:  Get brief information for offenders 'out today'.

     Given a user has authenticated with the API
     When a request is made to retrieve the 'offenders out' for agency "LEI" for "2000-02-12"
     Then the following rows should be returned:
     | firstName | lastName  | offenderNo | dateOfBirth | timeOut   | reasonDescription |
     | Nick      | Talbot    | Z0018ZZ    | 1970-01-01  | 12:00     | Normal transfer   |


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
    | offenderNo | dateOfBirth | firstName | middleName | lastName | fromAgencyDescription | movementTime | location    |
    | A6676RS    | 1945-01-10  | Neil      |            | Bradley  | Birmingham            | 10:45        | Landing H/1 |


  Scenario: Get offender in reception
      Given a user has authenticated with the API
      When a request is made to retrieve 'offenders in reception' for agency "MDI"
      Then information about 'offenders in reception' is returned as follows:
      | bookingId | offenderNo | dateOfBirth   | firstName | lastName|
      | -46       | A118DDD    | 1980-01-02    |  Amy      | Dude    |