Feature: Offenders

  AcceptanceCriteria:
    A logged in staff user can view the addresses of offenders

  Scenario: A staff user views the address for an existing offender
    Given a user has a token name of "ELITE2_API_USER"
    When I view the addresses of offender with offender display number of "A1234AI"
    Then the address results are:
    | primary  | noFixedAddress | flat      | premise          | street          | town         | postalCode | county             | country   | comment     | startDate     |
    |  true    |     true       |           |                  |                 |              |            |                    |  England  |             | 2017-03-01    |
    |  false   |     false      | Flat 1    |   Brook Hamlets  | Mayfield Drive  |  Sheffield   |    B5      | South Yorkshire    |  England  |             | 2015-10-01    |
    |  false   |     false      |           |   9              | Abbydale Road   |  Sheffield   |            | South Yorkshire    |  England  | A Comment   | 2014-07-01    |
    |  false   |     true       |           |                  |                 |              |            |                    |  England  |             | 2014-07-01    |


  Scenario: A staff user fails to find addresses as offender does not exist
    Given a user has a token name of "ELITE2_API_USER"
    When I view the addresses of offender with offender display number of "Does Not Exist"
    Then resource not found response is received from offender API


  Scenario: A staff user attempts to view the addresses for an existing offender they are not authorised to see.
    Given a user has a token name of "NO_CASELOAD_USER"
    When I view the addresses of offender with offender display number of "A1234AI"
    Then resource not found response is received from offender API
