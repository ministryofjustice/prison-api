@global
Feature: Case Note Retrieval and Filtering

  Acceptance Criteria:
  A logged in staff user can retrieve case notes for an existing offender booking
  A logged in staff user can filter retrieved case notes for an existing offender booking

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized

  Scenario Outline: Retrieve case notes
    When case notes are requested for offender booking "<bookingId>"
    Then "<number>" case notes are returned
    And case note types match "<case note types>"
    And case note sub types match "<case note sub types>"

    Examples:
      | bookingId | number | case note types     | case note sub types           |
      | -1        | 1      | CHAP                | FAMMAR                        |
      | -2        | 4      | COMMS,APP,ETE,COMMS | COM_IN,OUTCOME,ETERTO,COM_OUT |
      | -3        | 0      |                     |                               |

  Scenario Outline: Retrieve filtered case notes
    When case note type "<case note type>" filter applied
    And case note sub type "<case note sub type>" filter applied
    And date from "<date from>" filter applied
    And date to "<date to>" filter applied
    And filtered case notes are requested for offender booking "<bookingId>"
    Then "<number>" case notes are returned

    Examples:
      | bookingId | case note type | case note sub type | date from  | date to    | number |
      | -1        | CHAP           |                    |            |            | 1      |
      | -2        | COMMS          |                    |            |            | 2      |
      | -1        |                | FAMMAR             |            |            | 1      |
      | -2        |                | OUTCOME            |            |            | 1      |
      | -2        | ETE            | ETERTO             |            |            | 1      |
      | -2        | ETE            | OUTCOME            |            |            | 0      |
      | -2        |                |                    |            |            | 4      |
      | -2        |                |                    | 2017-04-06 |            | 3      |
      | -2        |                |                    | 2017-04-05 |            | 4      |
      | -2        |                |                    |            | 2017-04-10 | 1      |
      | -2        |                |                    |            | 2017-04-11 | 2      |
      | -2        |                |                    | 2017-04-06 | 2017-05-05 | 2      |
      | -2        |                |                    | 2017-04-05 | 2017-05-06 | 4      |
      | -2        | COMMS          |                    | 2017-04-05 | 2017-05-06 | 2      |
      | -2        | COMMS          | COM_IN             | 2017-04-05 | 2017-05-06 | 1      |
