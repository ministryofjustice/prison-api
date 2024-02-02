Feature: Offenders Adjudications

  AcceptanceCriteria:
  A logged in staff user can view an offender's adjudications related to the latest booking

  Scenario: A staff user views the adjudications for an existing offender
    Given a user has a token name of "PRISON_API_USER"
    When I view the adjudications of offender with offender display number of "A1181HH"
    Then the adjudication results are:
      | adjudicationNumber | reportDate       | agencyId | offenceCodes | findings      |
      | -7                 | 2019-08-25T00:03 | MDI      | 51:2D,51:2D  | PROVED,PROVED |
      | -2                 | 2017-02-23T00:01 | LEI      | 51:2C        | NOT_PROVEN    |
    And the associated offences for this offender are: "51:2C,51:2D"
    And the associated agencies for this offender are: "LEI,MDI"

  Scenario: A staff user views many adjudications for an existing offender
    Given a user has a token name of "VIEW_PRISONER_DATA"
    When I view the adjudications of offender with offender display number of "A1181GG"
    Then the adjudication results are:
      | adjudicationNumber | reportDate       | agencyId | offenceCodes | findings       |
      | -3002              | 2019-10-25T00:05 | BXI      | 51:8D        | null           |
      | -3001              | 2019-09-25T00:04 | LEI      | 51:8D        | null           |
      | -3                 | 2019-08-25T00:03 | MDI      | 51:2D        | PROVED         |
    And the associated offences for this offender are: "51:2D, 51:8D"
    And the associated agencies for this offender are: "MDI, LEI, BXI"

  Scenario: A staff user views adjudications for an offender at a single prison
    Given a user has a token name of "VIEW_PRISONER_DATA"
    When I view the adjudications of offender with offender display number of "A1181GG" at "LEI" with charge of type: "86"
    Then the adjudication results are:
      | adjudicationNumber | reportDate       | agencyId | offenceCodes | findings      |
      | -3001              | 2019-09-25T00:04 | LEI      | 51:8D        | null          |
    And the associated offences for this offender are: "51:2D, 51:8D"
    And the associated agencies for this offender are: "MDI, LEI, BXI"

  Scenario: A staff user cannot view adjudications for an offender on a caseload they don't have access to.
    Given a user has a token name of "PRISON_API_USER"
    When I view the adjudications of offender with offender display number of "A1181GG"
    Then resource not found response is received from adjudication API

  Scenario: A user fails to find adjudications as offender does not exist
    Given a user has a token name of "PRISON_API_USER"
    When I view the adjudications of offender with offender display number of "Does Not Exist"
    Then resource not found response is received from adjudication API

  Scenario: A staff user views adjudication details for an existing offender
    Given a user has a token name of "VIEW_PRISONER_DATA"
    When I view the adjudication details of offender display number of "A1181HH" with a adjudication number of "-7"
    Then the adjudication details are found

  Scenario: A user fails to find adjudication details as offender does not exist
    Given a user has a token name of "PRISON_API_USER"
    When I view the adjudication details of offender display number of "Does not exist" with a adjudication number of "-7"
    Then resource not found response is received from adjudication API
