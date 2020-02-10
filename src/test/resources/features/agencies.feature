@wip
Feature: Agencies

  Acceptance Criteria
  A logged on staff user can obtain:
    - a list of all active agencies.
    - details for a specified active agency.
    - a list of all active locations associated with an active agency.
    - a list of all active locations associated with an active agency that can be used for a specified type of event.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve all agencies
    When a request is submitted to retrieve all agencies
    Then "11" agency records are returned
    And "11" total agency records are available
    Then the returned agencies are as follows:
      | agencyId | agencyType | description  |
      | ABDRCT   | CRT        | Court 2      |
      | BMI      | INST       | BIRMINGHAM   |
      | BXI      | INST       | BRIXTON      |
      | COURT1   | CRT        | Court 1      |
      | LEI      | INST       | LEEDS        |
      | MDI      | INST       | MOORLAND     |
      | MUL      | INST       | MUL          |
      | RNI      | INST       | RANBY (HMP) |
      | SYI      | INST       | SHREWSBURY   |
      | TRO      | INST       | TROOM        |
      | WAI      | INST       | THE WEARE    |

  Scenario: Retrieve agency by caseload for single agency
    When a request is submitted to retrieve all agencies by caseload "LEI"
    Then "1" agency records are returned
    Then the returned agencies are as follows:
      | agencyId | agencyType | description  |
      | LEI      | INST       | Leeds        |

  Scenario: Retrieve agency by caseload for multi agency
    When a request is submitted to retrieve all agencies by caseload "MUL"
    Then "2" agency records are returned
    Then the returned agencies are as follows:
      | agencyId | agencyType | description  |
      | BXI      | INST       | Brixton      |
      | LEI      | INST       | Leeds        |

  Scenario Outline: Retrieve agency details
    When a request is submitted to retrieve agency "<agencyId>"
    Then the returned agency agencyId is "<agencyId>"
    And the returned agency agencyType is "<agencyType>"
    And the returned agency description is "<description>"
    Examples:
      | agencyId | agencyType | description |
      | LEI      | INST       | Leeds       |
      | WAI      | INST       | The Weare   |

  Scenario: Retrieve agency details when excluding inactive agencies
    When a request is submitted to retrieve agency "ZZGHI" when "excluding" inactive
    Then the agency is not found

  Scenario: Retrieve agency details doesn't return inactive agencies by default
    When a request is submitted to retrieve agency "ZZGHI"
    Then the agency is not found

  Scenario: Retrieve agency details when including inactive agencies
    When a request is submitted to retrieve agency "ZZGHI" when "including" inactive
    Then the agency is found

  Scenario: Retrieve all locations for an agency
    When a request is submitted to retrieve location codes for agency "LEI"
    Then "137" location records are returned for agency

  Scenario: Retrieve locations, for an agency, that can be used for appointments
    When a request is submitted to retrieve location codes for agency "LEI" and event type "APP"
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |

  Scenario: Retrieve locations, for an agency, that can be used for 'APP' events, in descending order of description
    When a request is submitted to retrieve location codes for agency "LEI" and event type "APP" sorted by "userDescription" in "descending" order
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |

  Scenario: Retrieve locations, for an agency, that can be used for any events
    When a request is submitted to retrieve location codes for agency "LEI" for any events
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |

  Scenario: Retrieve locations, for an agency, that are booked for offenders on the given date
    When a request is submitted to retrieve locations for agency "LEI" for booked events on date "2017-09-15"
    Then the returned agency locations are as follows:
      | locationId | description        | userDescription    |
      | -26        | Carpentry Workshop | Carpentry Workshop |
      | -25        | Chapel             | Chapel             |
      | -27        | Classroom 1        | Classroom 1        |
      | -29        | Medical Centre     | Medical Centre     |

  Scenario: Retrieve locations, for an agency, that are booked for offenders on the given date with timeslot
    When a request is submitted to retrieve locations for agency "LEI" for booked events on "2017-09-15" and timeslot "AM"
    Then the returned agency locations are as follows:
      | locationId | description    | userDescription    |
      | -25        | Chapel         | Chapel             |

  Scenario: Retrieve locations, for an agency, that are booked for offenders on the given date (appointment event_id=-15)
    When a request is submitted to retrieve locations for agency "LEI" for booked events on date "2017-12-25"
    Then the returned agency locations are as follows:
      | locationId | description    | userDescription    |
      | -25        | Chapel         | Chapel             |

  Scenario: Retrieve locations, for an agency, that are booked for offenders on the given date (offender_visit_id=-14)
    When a request is submitted to retrieve locations for agency "LEI" for booked events on date "2017-03-10"
    Then the returned agency locations are as follows:
      | locationId | description    | userDescription    |
      | -25        | Chapel         | Chapel             |

  Scenario Outline: Retrieve whereabouts config for an agency
    When a request is submitted to retrieve whereabouts config for agency "<agencyId>"
    Then the returned enabled flag is "<enabled>"
    Examples:
      | agencyId | enabled |
      | LEI      | true    |
      | HLI      | true    |
      | WAI      | false   |
      | SYI      | false   |

  Scenario: Retrieve IEP levels for an agency
    When a request is submitted to retrieve IEP levels for agency "LEI"
    Then the returned IEP levels are as follows:
      | iepLevel | iepDescription |
      | ENT      | Entry          |
      | BAS      | Basic          |
      | STD      | Standard       |
      | ENH      | Enhanced       |
