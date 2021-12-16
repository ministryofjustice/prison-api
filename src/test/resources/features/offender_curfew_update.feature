Feature: Updating Offender Curfew state

  Scenario Outline: Update the 'Checks Passed' flag for a booking's current (latest) curfew
    Given a user has a token name of "<tokenName>"
    When that user requests an update of the HDC status of the latest Offender Curfew for booking "<bookingId>" to "<checksPassed>" at "<checksPassedDate>"
    Then the response HTTP status should be "<httpStatus>"
    And the latest home detention curfew for booking "<bookingId>" should match "<checksPassed>", "<checksPassedDate>" if "<httpStatus>" is 204

    Examples:
      | tokenName              | bookingId | checksPassed | checksPassedDate | httpStatus |
      | SYSTEM_USER_READ_WRITE | -35       | true         | 2018-01-31       | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | 2018-01-31       | 204        |
      | SYSTEM_USER_READ_WRITE | -35       |              | 2018-01-31       | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         |                  | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | xxxxxxxxxx       | 400        |
      | SYSTEM_USER_READ_WRITE | 99999     | true         | 2018-01-31       | 404        |
      | PRISON_API_USER        | -35       | true         | 2018-01-31       | 403        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | 2018-02-31       | 400        |

  Scenario Outline:  Update the 'Approval status' for a booking's current (latest) curfew
    Given a user has a token name of "<tokenName>"
    When that user requests an update of the HDC approval status of the latest Offender Curfew for booking "<bookingId>" with "<checksPassed>" to "<approvalStatus>" and "<refusedReason>" at "<approvalStatusDate>"
    Then the response HTTP status should be "<httpStatus>"
    And the latest home detention curfew for booking "<bookingId>" should match "<approvalStatus>", "<refusedReason>", "<approvalStatusDate>" if "<httpStatus>" is 204

    Examples:
      | tokenName              | bookingId | checksPassed | approvalStatus | refusedReason | approvalStatusDate | httpStatus |
      | SYSTEM_USER_READ_WRITE | -35       | true         | APPROVED       |               | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | INELIGIBLE     | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | OPT_OUT        | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | OPT_OUT ACCO   | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | OPT_OUT OTH    | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | PP INVEST      | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | PP OUT RISK    | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | PRES UNSUIT    | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | REJECTED       | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | REJECTED       | ADDRESS       | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | INELIGIBLE     | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | OPT_OUT        | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | OPT_OUT ACCO   | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | OPT_OUT OTH    | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | PP INVEST      | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | PP OUT RISK    | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | PRES UNSUIT    | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | REJECTED       | BREACH        | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | false        | REJECTED       | ADDRESS       | 2018-01-31         | 204        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | APPROVED       | BREACH        | 2018-01-31         | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | REJECTED       | XXXXXXXX      | 2018-01-31         | 400        |
      | PRISON_API_USER        | -35       |              | APPROVED       |               | 2018-01-31         | 403        |
      | SYSTEM_USER_READ_WRITE | 99999     |              | APPROVED       |               | 2018-01-31         | 404        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | APPROVED       |               | 2018-02-31         | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | APPROVED       |               |                    | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         |                |               | 2018-01-31         | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | XXXXXXXX       | BREACH        | 2018-01-31         | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | INACTIVE       | BREACH        | 2018-01-31         | 400        |
      | SYSTEM_USER_READ_WRITE | -35       | true         | APPROVED       |               | xxxxxxxxxx         | 400        |
