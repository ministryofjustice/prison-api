Feature: Agencies

  Acceptance Criteria
  A logged on staff user can obtain:
    - a list of all active agencies.
    - details for a specified active agency.
    - a list of all active locations associated with an active agency.
    - a list of all active locations associated with an active agency that can be used for a specified type of event.

  Background:
    Given a user has authenticated with the API

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
    Then "139" location records are returned for agency

  Scenario: Retrieve locations, for an agency, that can be used for appointments
    When a request is submitted to retrieve location codes for agency "LEI" and event type "APP"
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |

  Scenario: Retrieve locations, for an agency, that can be used for occurrences
    When a request is submitted to retrieve location codes for agency "LEI" and event type "OCCUR"
    Then the returned agency locations are as follows:
      | locationId | description        | userDescription    | locationPrefix         | locationUsage |
      | 14433      | RES-AWING          | A Wing             | LEI-RES-AWING          | OCCUR         |
      | 13411      | OTHER-ABEX         | A/b Exercise Yard  | LEI-OTHER-ABEX         | OCCUR         |
      | 14439      | RES-BWING          | B Wing             | LEI-RES-BWING          | OCCUR         |
      | 14444      | RES-CWING          | C Wing             | LEI-RES-CWING          | OCCUR         |
      | 14448      | RES-DWING          | D Wing             | LEI-RES-DWING          | OCCUR         |
      | 13392      | EDUC               | Education          | LEI-EDUC               | OCCUR         |
      | 13402      | GYM                | Gym                | LEI-GYM                | OCCUR         |
      | 14452      | RES-IWING          | I Wing             | LEI-RES-IWING          | OCCUR         |
      | 1901       | OTHER-OTHERCELL    | Other Cell         | LEI-OTHER-OTHERCELL    | OCCUR         |
      | 1900       | OTHER-PRISONERSCEL | Prisoner's Cell    | LEI-OTHER-PRISONERSCEL | OCCUR         |

  Scenario: Retrieve locations, for an agency, that can be used for 'APP' events, in descending order of description
    When a request is submitted to retrieve location codes for agency "LEI" and event type "APP" sorted by "userDescription" in "descending" order
    Then the returned agency locations are as follows:
      | locationId | description | userDescription    | locationPrefix | locationUsage |
      | -29        | MED         | Medical Centre     | LEI-MED        | APP           |
      | -25        | CHAP        | Chapel             | LEI-CHAP       | APP           |
      | -26        | CARP        | Carpentry Workshop | LEI-CARP       | APP           |
      | -27        | CRM1        | Classroom 1        | LEI-CRM1       | APP           |

  Scenario: Retrieve locations, for an agency, that can be used for any events
    When a request is submitted to retrieve location codes for agency "LEI" for any events
    Then the returned agency locations are as follows:
      | locationId | description        | userDescription    | locationPrefix         | locationUsage |
      | 14433      | RES-AWING          | A Wing             | LEI-RES-AWING          | OCCUR         |
      | 13411      | OTHER-ABEX         | A/b Exercise Yard  | LEI-OTHER-ABEX         | OCCUR         |
      | 14439      | RES-BWING          | B Wing             | LEI-RES-BWING          | OCCUR         |
      | 14444      | RES-CWING          | C Wing             | LEI-RES-CWING          | OCCUR         |
      | -27        | CRM1               | Classroom 1        | LEI-CRM1               | APP           |
      | -26        | CARP               | Carpentry Workshop | LEI-CARP               | APP           |
      | -25        | CHAP               | Chapel             | LEI-CHAP               | APP           |
      | 14448      | RES-DWING          | D Wing             | LEI-RES-DWING          | OCCUR         |
      | 13392      | EDUC               | Education          | LEI-EDUC               | OCCUR         |
      | 13402      | GYM                | Gym                | LEI-GYM                | OCCUR         |
      | 14452      | RES-IWING          | I Wing             | LEI-RES-IWING          | OCCUR         |
      | -29        | MED                | Medical Centre     | LEI-MED                | APP           |
      | 1901       | OTHER-OTHERCELL    | Other Cell         | LEI-OTHER-OTHERCELL    | OCCUR         |
      | 1900       | OTHER-PRISONERSCEL | Prisoner's Cell    | LEI-OTHER-PRISONERSCEL | OCCUR         |

  Scenario: Retrieve IEP levels for an agency
    When a request is submitted to retrieve IEP levels for agency "LEI"
    Then the returned IEP levels are as follows:
      | iepLevel | iepDescription | sequence | defaultLevel |
      | BAS      | Basic          | 1        | false        |
      | ENT      | Entry          | 2        | true         |
      | STD      | Standard       | 3        | false        |
      | ENH      | Enhanced       | 4        | false        |
