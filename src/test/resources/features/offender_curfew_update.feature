@wip
Feature: Updating Offender Curfew state

  Scenario Outline: Update the 'Checks Passed' flag for a booking's current (latest) curfew
    Given a user has a token name of "<tokenName>"
    When that user requests an update of the HDC status of the latest Offender Curfew for booking "<bookingId>" to "<checksPassed>" at "<checksPassedDate>"
    Then the response HTTP status should be "<httpStatus>"
    And the latest home detention curfew for booking "<bookingId>" should match "<checksPassed>", "<checksPassedDate>" if "<httpStatus>" is 200

    Examples:
      | tokenName              | bookingId | checksPassed | checksPassedDate | httpStatus |
      | SYSTEM_USER_READ_WRITE | -35       | true         | 2018-01-31       | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | 2018-01-31       | 200        |
      | SYSTEM_USER_READ_WRITE | -35       |              | 2018-01-31       | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         |                  | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | x            | 2018-01-31       | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | xxxxxxxxxx       | 400        |
      | SYSTEM_USER_READ_WRITE | 99999     | true         | 2018-01-31       | 404        |
      | ELITE2_API_USER        | -35       | true         | 2018-01-31       | 403        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | 2018-02-31       | 400        |

  Scenario Outline:  Update the 'Approval status' for a booking's current (latest) curfew
    Given a user has a token name of "<tokenName>"
    When that user requests an update of the HDC approval status of the latest Offender Curfew for booking "<bookingId>" to "<approvalStatus>" and "<refusedReason>" at "<approvalStatusDate>"
    Then the response HTTP status should be "<httpStatus>"
    And the latest home detention curfew for booking "<bookingId>" should match "<approvalStatus>", "<refusedReason>", "<approvalStatusDate>" if "<httpStatus>" is 200

    Examples:
      | tokenName              | bookingId | approvalStatus | refusedReason | approvalStatusDate | httpStatus |
      | SYSTEM_USER_READ_WRITE | -35       | APPROVED       |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | INELIGIBLE     |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | OPT_OUT        |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | OPT_OUT ACCO   |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | OPT_OUT OTH    |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | PP INVEST      |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | PP OUT RISK    |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | PRES UNSUIT    |               | 2018-01-31         | 200        |
      | SYSTEM_USER_READ_WRITE | -35       | REJECTED       |               | 2018-01-31         | 200        |
      | ELITE2_API_USER        | -35       | APPROVED       |               | 2018-01-31         | 403        |
      | SYSTEM_USER_READ_WRITE | 99999     | APPROVED       |               | 2018-01-31         | 404        |
      | SYSTEM_USER_READ_WRITE | -35       | APPROVED       |               | 2018-02-31         | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | APPROVED       |               |                    | 400        |
      | SYSTEM_USER_READ_WRITE | -35       |                |               | 2018-01-31         | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | INACTIVE       |               | 2018-01-31         | 404        |
      | SYSTEM_USER_READ_WRITE | -35       | XXXXXXXX       |               | 2018-01-31         | 404        |
      | SYSTEM_USER_READ_WRITE | -35       | APPROVED       |               | xxxxxxxxxx         | 400        |