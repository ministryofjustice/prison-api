Feature: Booking Adjudications

  Acceptance Criteria:
  A logged in staff user can retrieve correct booking details for a provided offender booking id.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Request for Adjudications information about an offender
    When adjudication summary with booking id <bookingId>, award cutoff date "2016-09-01" and adjudication cutoff date "2016-09-01" is requested
    Then the adjudication count is <count>
    And the award sanctionCode is "<sanctionCode>"
    And the award sanctionCodeDescription is "<sanctionCodeDescription>"
    And the award months is "<months>"
    And the award days is "<days>"
    And the award limit is "<limit>"
    And the award comment is "<comment>"
    And the award effectiveDate is "<effectiveDate>"

    Examples:
      | bookingId | count | sanctionCode | sanctionCodeDescription | months | days | limit | comment           | effectiveDate |
      | -2        | 1     | ADA          | Additional Days Added   |        |  11  |       |                   | 2016-11-09    |
      | -1        | 1     | ADA          | Additional Days Added   |        |      |       | Some Comment Text | 2016-10-17    |

  Scenario: Offender has no awards, no data
    When adjudication summary with booking id -4 is requested
    Then There are no awards

  Scenario: Offender has no awards, data expired
    When adjudication summary with booking id -7, award cutoff date "2013-01-01" and adjudication cutoff date "2013-01-01" is requested
    Then There are no awards

  Scenario: Offender has more than 1 award
    When adjudication summary with booking id -8, award cutoff date "2017-08-20" and adjudication cutoff date "2017-08-20" is requested
    Then the award result list is as follows:
      | sanctionCode | sanctionCodeDescription  | months | days | limit | comment | effectiveDate |
      | FORFEIT      | Forfeiture of Privileges |        | 17   |       | loc     | 2017-11-13    |
      | CC           | Cellular Confinement     |        | 7    |       |         | 2017-11-13    |
      | STOP_PCT     | Stoppage of Earnings (%) |        | 21   | 50.00 |         | 2017-11-13    |
      | FORFEIT      | Forfeiture of Privileges | 2      | 19   |       | tv      | 2017-11-13    |

  Scenario Outline: Ensure older awards expire
    When adjudication summary with booking id -5, award cutoff date "<from>" and adjudication cutoff date "<from>" is requested
    Then there are <number> awards
    And the adjudication count is <count>
   
    Examples:
      | from       | number | count |
      | 2017-09-13 | 7      | 3     |
      | 2017-11-07 | 6      | 3     |
      | 2017-11-15 | 5      | 3     |
      | 2017-11-28 | 4      | 3     |
      | 2017-12-04 | 3      | 2     |
      | 2017-12-07 | 2      | 1     |
      | 2017-12-14 | 1      | 1     |
      | 2017-12-15 | 0      | 0     |

  Scenario: Different cutoff dates
    When adjudication summary with booking id -5, award cutoff date "2017-12-07" and adjudication cutoff date "2017-11-07" is requested
    Then there are 2 awards
    And the adjudication count is 3

  Scenario: Offender does not exist or different caseload
    When adjudication summary with booking id -16 is requested
    Then resource not found response is received from adjudication API
