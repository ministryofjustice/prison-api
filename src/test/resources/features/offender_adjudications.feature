 Feature: Offenders Adjudications

  AcceptanceCriteria:
    A logged in staff user can view an offender's adjudications

  Scenario: A staff user views the adjudications for an existing offender
    Given a user has a token name of "ELITE2_API_USER"
    When I view the adjudications of offender with offender display number of "A118HHH"
    Then the adjudication results are:
    | adjudicationNumber  |       reportDate      |  agencyId  |  offenceCodes |  findings  |
    |      -2             |    2017-02-23 00:01   |    LEI     |     51:2C     | NOT_PROVED |


  Scenario: A user fails to find adjudications as offender does not exist
    Given a user has a token name of "ELITE2_API_USER"
    When I view the adjudications of offender with offender display number of "Does Not Exist"
    Then resource not found response is received from adjudication API
