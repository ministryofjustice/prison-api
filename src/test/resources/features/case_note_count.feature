Feature: Case Note Count

  Acceptance Criteria:
  A logged in staff user can request count of case notes of a specific type/subType combination for an existing offender booking.
  A logged in staff user can request count of case notes of a specific type/subType combination within a date range for an existing offender booking.

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized

  Scenario: Case note count is requested for booking that does not exist
    When case note count is requested for offender booking "-99" for case note type "CHAP" and sub-type "FAMMAR"
    Then resource not found response is received from casenotes API

  Scenario: Case note count is requested for booking that is not part of any of logged on staff user's caseloads
    When case note count is requested for offender booking "-16" for case note type "CHAP" and sub-type "FAMMAR"
    Then resource not found response is received from casenotes API

  Scenario: Case note count is requested for existing booking, using a fromDate later than toDate
    When case note count between "2017-09-18" and "2017-09-12" is requested for offender booking "-1" for case note type "CHAP" and sub-type "FAMMAR"
    Then bad request response, with "Invalid date range: toDate is before fromDate." message, is received from casenotes API

  Scenario Outline: Valid request for case note count
    When case note count between "<fromDate>" and "<toDate>" is requested for offender booking "<bookingId>" for case note type "<type>" and sub-type "<subType>"
    Then case note count response "count" is "<count>"
    And case note count response "bookingId" is "<bookingId>"
    And case note count response "type" is "<type>"
    And case note count response "subType" is "<subType>"
    And case note count response "fromDate" is "<fromDate>"
    And case note count response "toDate" is "<toDate>"

    Examples:
      | bookingId | type    | subType | fromDate   | toDate     | count |
      | -1        | CHAP    | FAMMAR  |            |            | 1     |
      | -2        | CHAP    | FAMMAR  |            |            | 0     |
      | -2        | APP     | OUTCOME |            |            | 1     |
      | -3        | CHAP    | FAMMAR  |            |            | 0     |
      | -3        | OBSERVE | OBS_GEN |            |            | 8     |
      | -3        | OBSERVE | OBS_GEN | 2017-05-01 |            | 8     |
      | -3        | OBSERVE | OBS_GEN |            | 2017-05-01 | 0     |
      | -3        | OBSERVE | OBS_GEN | 2017-07-01 |            | 4     |
      | -3        | OBSERVE | OBS_GEN |            | 2017-07-31 | 6     |
      | -3        | OBSERVE | OBS_GEN | 2017-08-01 | 2017-08-31 | 2     |

  Scenario Outline: Get case note usage for a list of offenders and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of offender No "<offenderNo>" for case note type "<type>"  and sub-type "<subType>"
    Then case note usage response "numCaseNotes" is "<count>"
    And case note usage response "latestCaseNote" is "<latestNote>"

    Examples:
      | offenderNo | type    | subType | fromDate   | toDate     | count | latestNote       |
      | A1234AA    | CHAP    | FAMMAR  |            | 2017-03-25 | 1     | 2017-03-25T14:35 |
      | A1234AB    | APP     | OUTCOME | 2017-04-11 |            | 1     | 2017-04-11T18:42 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 | 8     | 2017-08-13T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2017-07-01 | 2019-01-01 | 4     | 2017-08-13T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-07-31 | 6     | 2017-07-31T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2017-08-01 | 2017-08-31 | 2     | 2017-08-13T12:00 |

  Scenario Outline: Get case note usage for a list of offenders, specific staff number and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of offender No "<offenderNo>" with staff Id "-1" for case note type "<type>"  and sub-type "<subType>" and agencyId "<agencyId>"
    Then case note usage response "numCaseNotes" is "<count>"
    And case note usage response "latestCaseNote" is "<latestNote>"

    Examples:
      | offenderNo | type    | subType | fromDate   | toDate     | agencyId | count | latestNote       |
      | A1234AA    | CHAP    | FAMMAR  |            | 2017-03-25 | LEI      | 1     | 2017-03-25T14:35 |
      | A1234AB    | APP     | OUTCOME | 2017-04-11 |            | LEI      | 1     | 2017-04-11T18:42 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 | LEI      | 6     | 2017-08-13T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 | BXI      | 1     | 2017-07-10T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2017-07-01 | 2019-01-01 | LEI      | 3     | 2017-08-13T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-07-31 | LEI      | 4     | 2017-07-31T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2017-08-01 | 2017-08-31 |          | 2     | 2017-08-13T12:00 |
      | A1234AC    |         |         | 2016-01-01 | 2019-01-01 | LEI      | 6     | 2017-08-13T12:00 |

  Scenario Outline: Get case note usage for a list of offenders, different staff number and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of offender No "<offenderNo>" with staff Id "-2" for case note type "<type>"  and sub-type "<subType>"
    Then case note usage response "numCaseNotes" is "<count>"
    And case note usage response "latestCaseNote" is "<latestNote>"

    Examples:
      | offenderNo | type    | subType | fromDate   | toDate     | count | latestNote       |
      | A1234AB    | COMMS   | COM_OUT | 2017-04-11 |            | 1     | 2017-05-06T17:11 |

  Scenario Outline: Get case note usage for a list of staff Is and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of staff ID "<staffId>" for case note type "<type>"  and sub-type "<subType>"
    Then case note staff usage response "numCaseNotes" is "<count>"
    And case note staff usage response "latestCaseNote" is "<latestNote>"

    Examples:
      | staffId | type     | subType | fromDate   | toDate     | count | latestNote       |
      | -1       | CHAP    | FAMMAR  |            | 2017-03-25 | 1     | 2017-03-25T14:35 |
      | -2       |         |         | 2016-01-01 | 2017-07-31 | 1     | 2017-05-06T17:11 |
      | -1       | OBSERVE |         | 2016-01-01 | 2017-07-31 | 7     | 2017-07-31T12:00 |
