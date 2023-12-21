Feature: Case Note Usage

  Acceptance Criteria:
  A logged in staff user can request usage data

  Background:
    Given a user has a token name of "VIEW_CASE_NOTES"
    And case note test harness initialized

  Scenario Outline: Get case note usage for a list of offenders and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of offender No "<offenderNo>" for case note type "<type>" and sub-type "<subType>"
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

  Scenario Outline: Get case note usage for an offender, specific staff number, agency and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of offender No "<offenderNo>" with staff Id "-1" for case note type "<type>" and sub-type "<subType>" and agencyId "<agencyId>"
    And case note usage response "numCaseNotes" is "<count>"
    And case note usage response "latestCaseNote" is "<latestNote>"
    And case note size is "<totalResults>"

    Examples:
      | offenderNo | type    | subType | fromDate   | toDate     | agencyId | count | totalResults | latestNote       |
      | A1234AA    | CHAP    | FAMMAR  |            | 2017-03-25 | LEI      | 1     |          1   | 2017-03-25T14:35 |
      | A1234AB    | APP     | OUTCOME | 2017-04-11 |            | LEI      | 1     |          1   | 2017-04-11T18:42 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 | LEI      | 6     |          1   | 2017-08-13T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 | BXI      | 1     |          1   | 2017-07-10T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2017-07-01 | 2019-01-01 | LEI      | 3     |          1   | 2017-08-13T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2016-01-01 | 2017-07-31 | LEI      | 4     |          1   | 2017-07-31T12:00 |
      | A1234AC    | OBSERVE | OBS_GEN | 2017-08-01 | 2017-08-31 |          | 2     |          1   | 2017-08-13T12:00 |
      | A1234AC    |         |         | 2016-01-01 | 2019-01-01 | LEI      | 6     |          1   | 2017-08-13T12:00 |
      |            |         |         | 2016-01-01 | 2019-01-01 | LEI      | 8     |          18  | 2017-05-13T12:00 |

  Scenario Outline: Get case note usage for a prison and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested for case note type "<type>" and sub-type "<subType>" and agencyId "<agencyId>"
    Then case note size is "<totalResults>"

    Examples:
      |  agencyId | type    | subType | fromDate   | toDate     | totalResults |
      |  LEI      |         |         | 2016-01-01 | 2019-01-01 |          19  |
      |  BXI      |         |         |            |            |          0   |
      |  LEI      | CHAP    |         | 2016-01-01 | 2019-01-01 |          1   |
      |  BXI      |         |         | 2016-01-01 | 2019-01-01 |          1   |
      |  LEI      | CHAP    | FAMMAR  |            | 2017-03-25 |          1   |
      |  LEI      | APP     | OUTCOME | 2017-04-11 |            |          1   |
      |  LEI      | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 |          2   |
      |  BXI      | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 |          1   |

  Scenario Outline: Get case note usage for a list of offenders, different staff number and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of offender No "<offenderNo>" with staff Id "-2" for case note type "<type>" and sub-type "<subType>"
    Then case note usage response "numCaseNotes" is "<count>"
    And case note usage response "latestCaseNote" is "<latestNote>"

    Examples:
      | offenderNo | type    | subType | fromDate   | toDate     | count | latestNote       |
      | A1234AB    | COMMS   | COM_OUT | 2017-04-11 |            | 1     | 2017-05-06T17:11 |

  Scenario Outline: Get case note usage for a list of staff Ids and date ranges
    When case note usage between "<fromDate>" and "<toDate>" is requested of staff ID "<staffId>" for case note type "<type>" and sub-type "<subType>"
    Then case note staff usage response "numCaseNotes" is "<count>"
    And case note staff usage response "latestCaseNote" is "<latestNote>"

    Examples:
      | staffId | type     | subType | fromDate   | toDate     | count | latestNote       |
      | -1       | CHAP    | FAMMAR  |            | 2017-03-25 | 1     | 2017-03-25T14:35 |
      | -2       |         |         | 2016-01-01 | 2017-07-31 | 1     | 2017-05-06T17:11 |
      | -1       | OBSERVE |         | 2016-01-01 | 2017-07-31 | 7     | 2017-07-31T12:00 |

  Scenario Outline: Get case note usage by booking id for a list of offenders and date ranges
    When case note usage by booking id between "<fromDate>" and "<toDate>" is requested of booking Id "<bookingId>" for case note type "<type>" and sub-type "<subType>"
    Then case note usage response "numCaseNotes" is "<count>"
    And case note usage response "latestCaseNote" is "<latestNote>"

    Examples:
      | bookingId | type    | subType | fromDate   | toDate     | count | latestNote       |
      | -1        | CHAP    | FAMMAR  |            | 2017-03-25 | 1     | 2017-03-25T14:35 |
      | -2        | APP     | OUTCOME | 2017-04-11 |            | 1     | 2017-04-11T18:42 |
      | -3        | OBSERVE | OBS_GEN | 2016-01-01 | 2017-08-13 | 8     | 2017-08-13T12:00 |
      | -3        | OBSERVE | OBS_GEN | 2017-07-01 | 2019-01-01 | 4     | 2017-08-13T12:00 |
      | -3        | OBSERVE | OBS_GEN | 2016-01-01 | 2017-07-31 | 6     | 2017-07-31T12:00 |
      | -3        | OBSERVE | OBS_GEN | 2017-08-01 | 2017-08-31 | 2     | 2017-08-13T12:00 |
