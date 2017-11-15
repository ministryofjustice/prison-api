@global
Feature: Reference Domains

  Acceptance Criteria
  A logged on staff user can obtain a list of case note types to select from.

  Background:
    Given a user has authenticated with the API

  @broken
  Scenario: Retrieve types without subtypes
    When all types are requested
    Then all types are returned

  @broken
  Scenario: Retrieve types with subtypes
    When all types with subtypes are requested
    Then all types with subtypes are returned

  @broken
  Scenario: Retrieve alert types
    When all alert types are requested
    Then all alert types are returned

  Scenario: Retrieve sources
    When all sources are requested
    Then all sources are returned

  Scenario Outline: Retrieve specific type
    When type with code "<code>" is requested
    Then the type returned code is "<code>"
    And the type returned description is "<description>"
    And the type returned activeFlag is "<activeFlag>"
    And the type returned domain is "<domain>"
    Examples:
      | code | description    | domain    | activeFlag | parentCode |
      | RR   | Release Report | TASK_TYPE |     Y      |            |
      
  Scenario Outline: Retrieve specific subtype
    When subtype with code "<code>" and subCode "<subCode>" is requested
    Then the subtype returned code is "<subCode>"
    And the subtype returned description is "<description>"
    And the subtype returned activeFlag is "<activeFlag>"
    And the subtype returned domain is "<domain>"
    And the subtype returned parentCode is "<parentCode>"
    Examples:
      | code | subCode | description                    | domain       | activeFlag | parentCode |
      | ACP  | CPS     | Core Programme Session         | TASK_SUBTYPE |     Y      |   ACP      |
      | ACP  | POS4    | Post Programme OM Session four | TASK_SUBTYPE |     Y      |   ACP      |

  Scenario Outline: Retrieve specific subtype list
    When subtype list with code "<code>" is requested
    Then the list size is "<listSize>"
    And the list domain is "<domain>"
    And the list first code is "<firstCode>"
    And the list first description is "<firstDescription>"
    Examples:
      | code   | listSize | domain       | firstCode    | firstDescription          |
      | VICTIM | 2        | TASK_SUBTYPE | INFO_COMPLET | CR Victim Complete test   |

  Scenario Outline: Retrieve specific alert type
    When alert type with code "<code>" is requested
    Then the alert type returned code is "<code>"
    And the alert type returned description is "<description>"
    And the alert type returned activeFlag is "<activeFlag>"
    And the alert type returned domain is "<domain>"
    And the alert type returned parentCode is "<parentCode>"
    Examples:
      | code | description | domain | activeFlag | parentCode |
      | X    | Security    | ALERT  |     Y      |            |
      | R    | Risk        | ALERT  |     Y      |            |
      | L    | Care Leaver | ALERT  |     Y      |    L       |

  Scenario Outline: Retrieve specific alert type code
    When alert code with code "<code>" and subCode "<subCode>" is requested
    Then the alert code returned code is "<subCode>"
    And the alert code returned description is "<description>"
    And the alert code returned domain is "<domain>"
    And the alert code returned activeFlag is "<activeFlag>"
    And the alert code returned parentCode is "<parentCode>"
    Examples:
      | code | subCode | description                           | domain     | activeFlag | parentCode |
      | C    | C3      | L3 Monitored Contact written or phone | ALERT_CODE |     Y      |      C     |
      | R    | RTP     | Risk to transgender people            | ALERT_CODE |     Y      |      R     |

  Scenario Outline: Retrieve specific alert code list
    When alert code list with code "<code>" is requested
    Then the list size is "<listSize>"
    And the list domain is "<domain>"
    And the list first code is "<firstCode>"
    And the list first description is "<firstDescription>"
    Examples:
      | code | listSize | domain    | firstCode | firstDescription                      |
      |  L   | 4        | ALERT_CODE| LPQAA     | Qualifies for Assistance and Advice   |
      |  A   | 1        | ALERT_CODE| AS        | Social Care                           |
      |  C   | 4        | ALERT_CODE| C4        | L4 No Restrictions (named child only) |

  Scenario Outline: Retrieve specific source
    When source with code "<code>" is requested
    Then the source returned code is "<code>"
    And the source returned description is "<description>"
    And the source returned activeFlag is "<activeFlag>"
    And the source returned domain is "<domain>"
    Examples:
      | code | description | domain      | activeFlag |
      | EXT  | External    | NOTE_SOURCE |     Y      |
      | COMM | Community   | NOTE_SOURCE |     Y      |