Feature: Locations

  Acceptance Criteria:
  A logged in staff user can find all locations available to them.
  A logged in staff user can retrieve details of a specific location available to them.
  A logged in staff user can retrieve a list of all offenders associated with a specific location available to them.
  A logged in staff user can retrieve the names of all location groups associated with an agency.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve a specific location record
    When a request is made to retrieve location with locationId of "<locationId>"
    Then "<number>" location records are returned
    And location type is "<type>"
    And description is "<description>"

    Examples:
      | locationId | number | type | description |
      | -1         | 1      | WING | Block A     |
      | -2         | 1      | LAND | Landing A/1 |
      | -3         | 1      | CELL | A-1-1       |

  Scenario: Request for specific location record that does not exist
    When a request is made to retrieve location with locationId of "-9999"
    Then resource not found response is received from locations API

  Scenario Outline: Retrieve a list/group of locations
    When a request is made at agency "<agency>" to retrieve the list named "<name>"
    Then location ids are "<locationIds>"
    And locations are "<locations>"

    Examples:
      | agency | name                | locationIds                                                                                                                                                                                         | locations                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
      | RNI    | House block 7       | 507011,507012,507031,507032,507033                                                                                                                                                                  | RNI-HB7-1-001,RNI-HB7-1-002,RNI-HB7-2-021,RNI-HB7-2-022,RNI-HB7-2-023                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
      | MDI    | Houseblock 1_A-Wing | -41,-42,-43,-44,-45,-46,-47,-48,-49,-50,-51,-52,-101,-102,-103,-104,-105,-106,-107,-108,-109,-110,-111,-112,-141,-142,-143,-144,-145,-146,-147,-148,-149,-150,-151,-152                             | MDI-1-1-001,MDI-1-1-002,MDI-1-1-003,MDI-1-1-004,MDI-1-1-005,MDI-1-1-006,MDI-1-1-007,MDI-1-1-008,MDI-1-1-009,MDI-1-1-010,MDI-1-1-011,MDI-1-1-012,MDI-1-2-001,MDI-1-2-002,MDI-1-2-003,MDI-1-2-004,MDI-1-2-005,MDI-1-2-006,MDI-1-2-007,MDI-1-2-008,MDI-1-2-009,MDI-1-2-010,MDI-1-2-011,MDI-1-2-012,MDI-1-3-001,MDI-1-3-002,MDI-1-3-003,MDI-1-3-004,MDI-1-3-005,MDI-1-3-006,MDI-1-3-007,MDI-1-3-008,MDI-1-3-009,MDI-1-3-010,MDI-1-3-011,MDI-1-3-012                                                                         |
      | MDI    | Houseblock 1_B-Wing | -53,-54,-55,-56,-57,-58,-59,-60,-61,-62,-63,-64,-65,-66,-113,-114,-115,-116,-117,-118,-119,-120,-121,-122,-123,-124,-125,-126,-153,-154,-155,-156,-157,-158,-159,-160,-161,-162,-163,-164,-165,-166 | MDI-1-1-013,MDI-1-1-014,MDI-1-1-015,MDI-1-1-016,MDI-1-1-017,MDI-1-1-018,MDI-1-1-019,MDI-1-1-020,MDI-1-1-021,MDI-1-1-022,MDI-1-1-023,MDI-1-1-024,MDI-1-1-025,MDI-1-1-026,MDI-1-2-013,MDI-1-2-014,MDI-1-2-015,MDI-1-2-016,MDI-1-2-017,MDI-1-2-018,MDI-1-2-019,MDI-1-2-020,MDI-1-2-021,MDI-1-2-022,MDI-1-2-023,MDI-1-2-024,MDI-1-2-025,MDI-1-2-026,MDI-1-3-013,MDI-1-3-014,MDI-1-3-015,MDI-1-3-016,MDI-1-3-017,MDI-1-3-018,MDI-1-3-019,MDI-1-3-020,MDI-1-3-021,MDI-1-3-022,MDI-1-3-023,MDI-1-3-024,MDI-1-3-025,MDI-1-3-026 |
      | MDI    | Houseblock 1_C-Wing | -67,-68,-69,-70,-71,-72,-73,-74,-75,-76,-77,-78,-127,-128,-129,-130,-131,-132,-133,-134,-135,-136,-137,-138,-167,-168,-169,-170,-171,-172,-173,-174,-175,-176,-177,-178,                            | MDI-1-1-027,MDI-1-1-028,MDI-1-1-029,MDI-1-1-030,MDI-1-1-031,MDI-1-1-032,MDI-1-1-033,MDI-1-1-034,MDI-1-1-035,MDI-1-1-036,MDI-1-1-037,MDI-1-1-038,MDI-1-2-027,MDI-1-2-028,MDI-1-2-029,MDI-1-2-030,MDI-1-2-031,MDI-1-2-032,MDI-1-2-033,MDI-1-2-034,MDI-1-2-035,MDI-1-2-036,MDI-1-2-037,MDI-1-2-038,MDI-1-3-027,MDI-1-3-028,MDI-1-3-029,MDI-1-3-030,MDI-1-3-031,MDI-1-3-032,MDI-1-3-033,MDI-1-3-034,MDI-1-3-035,MDI-1-3-036,MDI-1-3-037,MDI-1-3-038                                                                         |

  Scenario: Request for list/group that does not exist
    When a request is made at agency "RNI" to retrieve the list named "nonexistant"
    Then resource not found response is received from locations API

  Scenario Outline: Retrieve a list of inmates at a specific agency location
    When a request is made at agency "<agencyCode>" to retrieve a list of inmates
    Then there are "<countInmates>" offenders returned

    Examples:
    | agencyCode | countInmates |
    | LEI        |  26          |
    | BMI        |  0           |

  Scenario Outline: Retrieve a list of inmates at specific agency to check convicted status
    When a request is made at agency "<agencyCode>" to retrieve a list of inmates
    Then there are "<countInmates>" offenders returned with the convicted status "<convictedStatus>"

    Examples:
      | agencyCode | countInmates | convictedStatus |
      | LEI        | 8            | Convicted       |
      | LEI        | 3            | Remand          |

    Scenario Outline: Retrieve a list of inmates queried by convicted status
      When a request is made at agency "<agencyCode>" to retrieve a list of inmates with a convicted status of "<convictedStatus>"
      Then there are "<countInmates>" offenders returned

      Examples:
      | agencyCode | convictedStatus | countInmates |
      | LEI        | Convicted       | 8            |
      | LEI        | Remand          | 3            |
