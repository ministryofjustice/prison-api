Feature: Reference Domains

  Acceptance Criteria
  A logged on staff user can obtain:
    - a list of all alert types with alert codes (active and/or inactive).
    - a list of all case note sources.
    - a list of case note types (with sub-types) that have been used for creation of offender case notes.
    - a list of reference code items for a specified domain, with or without sub-codes.
    - a single reference code item for a specified domain and code, with or without sub-codes.
  Reference codes without sub-codes are excluded from response when reference codes with sub-codes have been requested.
  A '404 - Resource Not Found' response will be returned:
    - when reference codes, with or without sub-codes, are requested for a reference domain that does not exist.
    - when a reference code, with or without sub-codes, is requested for a reference domain that does not exist.
    - when a reference code, with or without sub-codes, is requested for a reference domain that does exist but for a code that does not.
  A '400 - Bad Request' response will be returned:
    - when a reference code is requested with sub-codes but no sub-codes are defined for the requested reference code.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve all alert types with alert codes
    When request submitted to retrieve all alert types with alert codes
    Then "14" reference code items are returned
    And domain for all returned items is "ALERT"
    And codes of returned items are "A,C,H,L,M,O,P,R,S,T,TEST,V,W,X"
    And there are one or more sub codes for every returned item
    And domain for all returned item sub-codes is "ALERT_CODE"

  Scenario: Retrieve all case note sources
    When request submitted to retrieve all case note sources
    Then "4" reference code items are returned
    And domain for all returned items is "NOTE_SOURCE"
    And codes of returned items are "AUTO,COMM,EXT,INST"
    And descriptions of returned items are "System,Community,External,Prison"
    And there is no parent domain for any returned item
    And there are no sub codes for any returned item

  Scenario: Retrieve used case note types with sub-types
    When request submitted to retrieve used case note types
    Then some reference code items are returned
    And domain for all returned items is "TASK_TYPE"
    And the returned items contain description "Accredited Programme"
    And the returned items contain description "Unpaid Work"
    And there are one or more sub codes for every returned item
    And item description "Accredited Programme" contains sub-code with description "Assessment"
    And item description "Accredited Programme" contains sub-code with description "Post Programme OM Session four"
    And item description "Court" contains sub-code with description "Post Sentence Interview"

  Scenario: Retrieve reference codes, without sub-codes, for a domain that does not exist
    When request submitted to retrieve all reference codes, without sub-codes, for domain "UNKNOWN"
    Then resource not found response is received from reference domains API
    And user message in resource not found response from reference domains API is "Reference domain [UNKNOWN] not found."

  Scenario: Retrieve reference codes, with sub-codes, for a domain that does not exist
    When request submitted to retrieve all reference codes, with sub-codes, for domain "UNKNOWN"
    Then resource not found response is received from reference domains API
    And user message in resource not found response from reference domains API is "Reference domain [UNKNOWN] not found."

  Scenario: Retrieve reference code, without sub-codes, when requested reference code domain does not exist
    When request submitted to retrieve reference code, without sub-codes, for domain "UNKNOWN" and code "NOT_A_CLUE"
    Then resource not found response is received from reference domains API
    And user message in resource not found response from reference domains API is "Reference domain [UNKNOWN] not found."

  Scenario: Retrieve reference code, with sub-codes, when requested reference code domain does not exist
    When request submitted to retrieve reference code, with sub-codes, for domain "UNKNOWN" and code "NOT_A_CLUE"
    Then resource not found response is received from reference domains API
    And user message in resource not found response from reference domains API is "Reference domain [UNKNOWN] not found."

  Scenario: Retrieve reference code, without sub-codes, when requested reference code domain exists but reference code does not
    When request submitted to retrieve reference code, without sub-codes, for domain "NOTE_SOURCE" and code "DOES_NOT_EXIST"
    Then resource not found response is received from reference domains API
    And user message in resource not found response from reference domains API is "Reference code for domain [NOTE_SOURCE] and code [DOES_NOT_EXIST] not found."

  Scenario: Retrieve reference code, with sub-codes, when requested reference code domain exists but reference code does not
    When request submitted to retrieve reference code, with sub-codes, for domain "MOVE_TYPE" and code "DOES_NOT_EXIST"
    Then resource not found response is received from reference domains API
    And user message in resource not found response from reference domains API is "Reference code for domain [MOVE_TYPE] and code [DOES_NOT_EXIST] not found."

  Scenario: Retrieve reference code, with sub-codes, when requested reference code exists but has no sub-codes
    When request submitted to retrieve reference code, with sub-codes, for domain "NOTE_SOURCE" and code "AUTO"
    Then bad request response is received from reference domains API and user message is "Reference code for domain [NOTE_SOURCE] and code [AUTO] does not have sub-codes."

  Scenario: Retrieve reference codes for a domain without sub-codes
    When request submitted to retrieve all reference codes, without sub-codes, for domain "SUB_AREA"
    Then "8" reference code items are returned
    And there is no parent domain for any returned item
    And there are no sub codes for any returned item
    And codes of returned items are "E,N,NE,NW,S,SE,SW,W"
    And code for "1st" returned item is "E"
    And description for "3rd" returned item is "North East"
    And code for "6th" returned item is "SE"
    And description for "8th" returned item is "West"

  Scenario: Retrieve reference codes for a domain with sub-codes
    When request submitted to retrieve all reference codes, with sub-codes, for domain "MOVE_TYPE"
    Then "5" reference code items are returned
    And there are one or more sub codes for every returned item
    And domain for all returned items is "MOVE_TYPE"
    And code for "1st" returned item is "ADM"
    And description for "2nd" returned item is "Court"
    And code for "3rd" returned item is "REL"
    And description for "4th" returned item is "Temporary Absence"
    And code for "5th" returned item is "TRN"
    And "1st" returned item has "34" sub-codes
    And "3rd" returned item has "37" sub-codes
    And "5th" returned item has "14" sub-codes
    And domain for all sub-codes of "1st" returned item is "MOVE_RSN"
    And domains for sub-codes of "4th" returned item are "MOVE_RSN,TAP_ABS_STYP,TAP_ABS_TYPE"
    And domain for all sub-codes of "5th" returned item is "MOVE_RSN"
    And code for "1st" sub-code of "1st" returned item is "24"
    And description for "17th" sub-code of "1st" returned item is "Intermittent Custody"
    And code for "34th" sub-code of "1st" returned item is "Z"
    And description for "1st" sub-code of "3rd" returned item is "Discharge Not for Release"
    And code for "20th" sub-code of "3rd" returned item is "DS"
    And description for "37th" sub-code of "3rd" returned item is "Abscond End of Custody Licence"
    And code for "1st" sub-code of "5th" returned item is "28"
    And description for "7th" sub-code of "5th" returned item is "Outside Jurisdiction"
    And code for "14th" sub-code of "5th" returned item is "TRN"

  Scenario Outline: Retrieve specific reference code, without sub-codes
    When request submitted to retrieve reference code, without sub-codes, for domain "<domain>" and code "<code>"
    And "domain" for returned item is "<domain>"
    And "code" for returned item is "<code>"
    And "description" for returned item is "<description>"
    And "activeFlag" for returned item is "<activeFlag>"
    And returned item has no sub-codes

    Examples:
      | domain    | code  | description                       | activeFlag |
      | ETHNICITY | B1    | Black/Black British: Caribbean    | Y          |
      | EVENTS    | CCASE | Court Case                        | N          |
      | IMPSBAND  | 17    | NonCustody-unconvicted            | Y          |

  Scenario Outline: Retrieve specific reference code, with sub-codes
    When request submitted to retrieve reference code, with sub-codes, for domain "<domain>" and code "<code>"
    And "domain" for returned item is "<domain>"
    And "code" for returned item is "<code>"
    And "description" for returned item is "<description>"
    And "activeFlag" for returned item is "<activeFlag>"
    And returned item has "<subCodeCount>" sub-codes

    Examples:
      | domain    | code  | description                       | activeFlag | subCodeCount |
      | ALERT     | C     | Child Communication Measures      | Y          | 4            |
      | CONTACTS  | S     | Social/Family                     | Y          | 38           |

  Scenario: Retrieve reason codes for an event type ordered by description
    When a request is submitted to retrieve all reason codes for event type "APP"
    Then the returned reason codes are as follows:
      | code | description      |
      | CABA | Bail             |
      | CHAP | Baptism          |
      | EDUC | Computers        |
      | MEDE | Dentist          |
      | KWS  | Key Work Session |
      | RES  | Resolve          |
