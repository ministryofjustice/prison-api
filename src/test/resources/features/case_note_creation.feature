Feature: Case Note Creation, Update and Count

  Acceptance Criteria:
  A logged in staff user can create and update case notes for an existing offender booking.
  A logged in staff user can request count of case notes of a specific type/subType combination for an existing offender booking.
  A logged in staff user can request count of case notes of a specific type/subType combination within a date range for an existing offender booking.

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized

  Scenario: Create a case note 1
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | OBSERVE                                     |
      | subType            | OBS_GEN                                     |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note is successfully created
    And correct case note source is used

  Scenario: Create a case note 2
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | OBSERVE                                     |
      | subType            | OBS_GEN                                     |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note is successfully created
    And correct case note source is used

  Scenario: Create a case note with invalid type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | doesnotexist                                |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "CaseNote (type,subtype)=(doesnotexist,OSE) does not exist" occurs

  Scenario: Create a case note with valid type but invalid subType
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | GEN                                         |
      | subType            | doesnotexist                                |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "CaseNote (type,subtype)=(GEN,doesnotexist) does not exist" occurs

  Scenario: Create a case note with invalid combination of type and sub-type for any caseload
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | DRR                                         |
      | subType            | HIS                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "CaseNote (type,subtype)=(DRR,HIS) does not exist" occurs

  Scenario: Create a case note with a type and sub-type combination that is valid for different caseload but not current caseload
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | REC                                         |
      | subType            | RECRP                                       |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
  Then case note validation error "CaseNote (type,subtype)=(REC,RECRP) does not exist" occurs

  Scenario: Create a case note with invalid type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | toolongtoolongtoolong                    |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value is too long: max length is 12|CaseNote (type,subtype)=(toolongtoolongtoolong,OSE) does not exist|

  Scenario: Create a case note with invalid sub-type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | GEN                                         |
      | subType            | toolongtoolongtoolong                     |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value is too long: max length is 12|CaseNote (type,subtype)=(GEN,toolongtoolongtoolong) does not exist|

  Scenario: Create a case note with blank type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               |                                             |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value cannot be blank|CaseNote (type,subtype)=(,OSE) does not exist|

  Scenario: Create a case note with blank sub-type
    When a case note is created for booking:
      | bookingId          | -32                                         |
      | type               | GEN                                         |
      | subType            |                                             |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value cannot be blank|CaseNote (type,subtype)=(GEN,) does not exist|

  Scenario: Attempt to create case note for offender is not part of any of logged on staff user's caseloads
    When attempt is made to create case note for booking:
      | bookingId          | -16                                         |
      | type               | GEN                                         |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then resource not found response is received from casenotes API

  Scenario: Attempt to create case note for offender that does not exist
    When attempt is made to create case note for booking:
      | bookingId          | -99                                         |
      | type               | GEN                                         |
      | subType            | OSE                                         |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then resource not found response is received from casenotes API

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
      | -1        | CHAP    | FAMMAR  | 2000-01-01 | 2020-01-01 | 1     |
      | -2        | CHAP    | FAMMAR  | 2000-01-01 | 2020-01-01 | 0     |
      | -2        | APP     | OUTCOME | 2000-01-01 | 2020-01-01 | 1     |
      | -3        | CHAP    | FAMMAR  | 2000-01-01 | 2020-01-01 | 0     |
      | -3        | OBSERVE | OBS_GEN | 2000-01-01 | 2020-01-01 | 8     |
      | -3        | OBSERVE | OBS_GEN | 2017-05-01 | 2020-01-01 | 8     |
      | -3        | OBSERVE | OBS_GEN | 2000-01-01 | 2017-05-01 | 0     |
      | -3        | OBSERVE | OBS_GEN | 2017-07-01 | 2020-01-01 | 4     |
      | -3        | OBSERVE | OBS_GEN | 2000-01-01 | 2017-07-31 | 6     |
      | -3        | OBSERVE | OBS_GEN | 2017-08-01 | 2017-08-31 | 2     |
      | -3        | OBSERVE | OBS_GEN | 2017-08-01 |            | 2     |
      | -3        | OBSERVE | OBS_GEN |            | 2017-08-31 | 6     |

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
