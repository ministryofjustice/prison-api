@wip
Feature: Offenders Adjudications

  AcceptanceCriteria:
  A logged in staff user can view an offender's adjudications


  Scenario: A staff user views the adjudications for an existing offender
    Given a user has a token name of "ELITE2_API_USER"
    When I view the adjudications of offender with offender display number of "A118HHH"
    Then the adjudication results are:
      | adjudicationNumber | reportDate       | agencyId | offenceCodes | findings   |
      | -2                 | 2017-02-23 00:01 | LEI      | 51:2C        | NOT_PROVED |
    And the associated offences for this offender are: "51:2C"
    And the associated agencies for this offender are: "LEI"
    
  Scenario: A staff user views many adjudications for an existing offender
    Given a user has a token name of "SYSTEM_USER_READ_WRITE"
    When I view the adjudications of offender with offender display number of "A118GGG"
    Then the adjudication results are:
      | adjudicationNumber | reportDate       | agencyId | offenceCodes | findings       |
      | -3                 | 2019-08-25 00:03 | MDI      | 51:2D        | PROVED         |
      | -5                 | 2019-01-25 00:02 | LEI      | 51:8D        | PROVED         |
      | -1                 | 2017-02-23 00:01 | LEI      | 51:1N,51:2B  | null,DISMISSED |
    And the associated offences for this offender are: "51:2D, 51:8D, 51:1N, 51:2B"
    And the associated agencies for this offender are: "MDI, LEI"

  Scenario: A staff user views many adjudications for an existing offender
    Given a user has a token name of "SYSTEM_USER_READ_WRITE"
    When I view the adjudications of offender with offender display number of "A118GGG" at "LEI" with charge of type: "86"
    Then the adjudication results are:
      | adjudicationNumber | reportDate       | agencyId | offenceCodes | findings       |
      | -5                 | 2019-01-25 00:02 | LEI      | 51:8D        | PROVED         |
    And the associated offences for this offender are: "51:2D, 51:8D, 51:1N, 51:2B"
    And the associated agencies for this offender are: "MDI, LEI"


  Scenario: A staff user cannot view adjudications for an offender on a caseload they don't have access to.
    Given a user has a token name of "ELITE2_API_USER"
    When I view the adjudications of offender with offender display number of "A118GGG"
    Then resource not found response is received from adjudication API

  Scenario: A user fails to find adjudications as offender does not exist
    Given a user has a token name of "ELITE2_API_USER"
    When I view the adjudications of offender with offender display number of "Does Not Exist"
    Then resource not found response is received from adjudication API
