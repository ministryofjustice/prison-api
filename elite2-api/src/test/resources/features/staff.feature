@global
Feature: Staff Details and Roles

  Acceptance Criteria:
  A logged in user can:
   - find staff details for any valid staff id.
   - retrieve a paged list of staff members having a specified position/role in an agency, optionally filtering the list
     by staff member name.

  Background:
    Given a system client "ITAG" has authenticated with the API

  Scenario Outline: Find staff member using staff id
    When a staff member search is made using staff id "<staffId>"
    Then first name of staff details returned is "<firstName>"
    And last name of staff details returned is "<lastName>"
# Email removed (Note staff member can have more than one email address)
#    And email address of staff details returned is "<email>"

    Examples:
      | staffId | firstName | lastName |
      | -1      | Elite2    | User     |
      | -2      | API       | User     |
      | -3      | CA        | User     |
      | -5      | RO        | User     |
      | -6      | DM        | User     |

  Scenario: Find staff member using staff id that does not exist
    When a staff member search is made using staff id "-9999"
    Then resource not found response is received from staff API

  Scenario: Find all staff members having specified role at an agency that does not exist
    When request is submitted for staff members having role "KW" in agency "XYZ"
    Then resource not found response is received from staff API

  Scenario: Find all staff members having specified role at an agency that is not accessible to the user
    When request is submitted for staff members having role "KW" in agency "BMI"
    Then resource not found response is received from staff API

  Scenario Outline: Find all staff members having specified role at an agency
    When request is submitted for staff members having role "<role>" in agency "<agency>"
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"

    Examples:
      | agency | role | count | staff id list |
      | LEI    | KW   | 4     | -1,-4,-11, -12     |
      | SYI    | KW   | 1     | -9            |
      | WAI    | OS   | 0     |               |

  Scenario Outline: Find all staff members having specified position and role at an agency
    When request is submitted for staff members having position "<position>" and role "<role>" in agency "<agency>"
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"

    Examples:
      | agency | position | role | count | staff id list |
      | LEI    | PRO      | KW   | 1     | -1            |
      | BXI    | PRO      | KW   | 1     | -2            |

  Scenario Outline: Search for staff members having specified role at an agency
    When request is submitted for staff members having role "<role>" in agency "<agency>" with name filter "<name filter>" and staff id filter "<staff id>"
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"

    Examples:
      | agency | role | name filter | staff id | count | staff id list |
      | SYI    | KW   |             |          | 1     | -9            |
      | WAI    | OS   | Ronald      |          | 0     |               |
      | LEI    | KW   | USE         |          | 2     | -1,-4         |
      | LEI    | KW   | user        |          | 2     | -1,-4         |
      | LEI    | KW   | Uses        |          | 0     |               |
      | LEI    | KW   |             | -1       | 1     | -1            |
      | LEI    | KW   |             | -999     | 0     |               |

  Scenario Outline: Search for staff members having specified position and role at an agency
    When request is submitted for staff members having position "<position>" and role "<role>" in agency "<agency>" with name filter "<name filter>" and staff id filter "<staff id>"
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"
    #staff -10 is inactive

    Examples:
      | agency | position | role | name filter | staff id | count | staff id list |
      | SYI    | AO       | KW   |             |          | 1     | -9            |
      | WAI    | PRO      | OS   | Ronald      |          | 0     |               |
      | LEI    | PRO      | KW   | USE         |          | 1     | -1            |
      | LEI    | AO       | KW   | user        |          | 1     | -4            |
      | LEI    | AO       | KW   | Uses        |          | 0     |               |
      | LEI    | AO       | KW   |             | -999     | 0     |               |

  Scenario Outline: Search for 'Active Only' staff members having specified position and role at an agency
    When request is submitted for staff members having position "<position>" and role "<role>" in agency "<agency>" with name filter "<name filter>" and staff id filter "<staff id>" and include inactive staff members
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"
    #staff - -10 is inactive

    Examples:
      | agency | position | role | name filter | staff id | count | staff id list |
      | BXI    | AO       | KW   | Api         |          | 1     | -2            |
      | SYI    | AO       | KW   |             |          | 1     | -9            |

  Scenario Outline: List all active job roles for staff member at an agency
      When request is submitted using "<staffId>" and "<agencyId>"
      Then a role containing "<role>" "<roleDescription>" is returned without duplicates
   Examples:
      | staffId |agencyId  |role | roleDescription     |
      | -2      |LEI       |OS    | Offender Supervisor|
      | -1      |LEI       |KW    | Key Worker         |
      | -2      |BXI       |KW    | Key Worker         |



