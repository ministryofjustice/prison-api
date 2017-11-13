@global
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

  Scenario Outline: Valid request for case note count
    When case note count is requested for offender booking "<bookingId>" for case note type "<type>" and sub-type "<subType>"
    Then case note count response "count" is "<count>"
    And case note count response "bookingId" is "<bookingId>"
    And case note count response "type" is "<type>"
    And case note count response "subType" is "<subType>"
    And case note count response "fromDate" is "<fromDate>"
    And case note count response "toDate" is "<toDate>"

    Examples:
      | bookingId | type    | subType | fromDate | toDate | count |
      | -1        | CHAP    | FAMMAR  |          |        | 1     |
      | -2        | CHAP    | FAMMAR  |          |        | 0     |
      | -2        | APP     | OUTCOME |          |        | 1     |
      | -3        | CHAP    | FAMMAR  |          |        | 0     |
      | -3        | OBSERVE | OBS_GEN |          |        | 8     |
