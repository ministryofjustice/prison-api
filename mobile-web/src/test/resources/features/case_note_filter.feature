@global @wip
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
      | bookingId | number | case note types                                                 | case note sub types                                             |
      | -1        | 1      | CHAP                                                            | FAMMAR                                                          |
      | -2        | 4      | COMMS,APP,ETE,COMMS                                             | COM_IN,OUTCOME,ETERTO,COM_OUT                                   |
      | -3        | 8      | OBSERVE,OBSERVE,OBSERVE,OBSERVE,OBSERVE,OBSERVE,OBSERVE,OBSERVE | OBS_GEN,OBS_GEN,OBS_GEN,OBS_GEN,OBS_GEN,OBS_GEN,OBS_GEN,OBS_GEN |
      | -31       | 0      |                                                                 |                                                                 |

  Scenario Outline: Retrieve filtered case notes
    When case note type "<case note type>" filter applied
    And case note sub type "<case note sub type>" filter applied
    And date from "<date from>" filter applied
    And date to "<date to>" filter applied
    And pagination with limit "<limit>" and offset "<offset>" applied
    And filtered case notes are requested for offender booking "<bookingId>"
    Then "<number>" case notes are returned
    And "<total>" case notes are available

    Examples:
      | bookingId | case note type | case note sub type | date from  | date to    | limit | offset | number | total |
      | -1        | CHAP           |                    |            |            |       |        | 1      | 1     |
      | -2        | COMMS          |                    |            |            |       |        | 2      | 2     |
      | -1        |                | FAMMAR             |            |            |       |        | 1      | 1     |
      | -2        |                | OUTCOME            |            |            |       |        | 1      | 1     |
      | -2        | ETE            | ETERTO             |            |            |       |        | 1      | 1     |
      | -2        | ETE            | OUTCOME            |            |            |       |        | 0      | 0     |
      | -2        |                |                    |            |            |       |        | 4      | 4     |
      | -2        |                |                    | 2017-04-06 |            |       |        | 3      | 3     |
      | -2        |                |                    | 2017-04-05 |            |       |        | 4      | 4     |
      | -2        |                |                    |            | 2017-04-10 |       |        | 1      | 1     |
      | -2        |                |                    |            | 2017-04-11 |       |        | 2      | 2     |
      | -2        |                |                    | 2017-04-06 | 2017-05-05 |       |        | 2      | 2     |
      | -2        |                |                    | 2017-04-05 | 2017-05-06 |       |        | 4      | 4     |
      | -2        | COMMS          |                    | 2017-04-05 | 2017-05-06 |       |        | 2      | 2     |
      | -2        | COMMS          | COM_IN             | 2017-04-05 | 2017-05-06 |       |        | 1      | 1     |
      | -3        |                |                    |            |            | 2     | 0      | 2      | 8     |
      | -3        |                |                    |            |            | 4     | 2      | 4      | 8     |
      | -3        |                |                    |            |            | 10    | 5      | 3      | 8     |
      | -3        |                |                    | 2017-07-01 |            | 10    | 0      | 4      | 4     |
      | -3        |                |                    |            | 2017-06-30 | 2     | 0      | 2      | 4     |
      | -3        |                |                    | 2017-06-01 | 2017-08-31 | 3     | 3      | 3      | 6     |

  Scenario: A specific case note is requested for booking that is not part of any of logged on staff user's caseloads
    When a case note is requested for offender booking "-16"
    Then resource not found response is received from casenotes API

  Scenario: A specific case note is requested for booking that does not exist
    When a case note is requested for offender booking "-99"
    Then resource not found response is received from casenotes API

  Scenario: Case note list is requested for booking that is not part of any of logged on staff user's caseloads
    When case notes are requested for offender booking "-16"
    Then resource not found response is received from casenotes API

  Scenario: Case note list is requested for booking that does not exist
    When case notes are requested for offender booking "-99"
    Then resource not found response is received from casenotes API
