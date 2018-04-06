@global
Feature: Staff Details and Roles

  Acceptance Criteria:
  A logged in user can:
   - find staff details for any valid staff id.
   - retrieve a paged list of staff members having a specified position/role in an agency, optionally filtering the list
     by staff member name.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Find staff member using staff id
    When a staff member search is made using staff id "<staffId>"
    Then first name of staff details returned is "<firstName>"
    And last name of staff details returned is "<lastName>"
# Email removed (Note staff member can have more than one email address)
#    And email address of staff details returned is "<email>"

    Examples:
      | staffId | firstName | lastName | email                      |
      | -1      | Elite2    | User     | elite2-api-user@syscon.net |
      | -2      | API       | User     | itaguser@syscon.net        |

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
      | LEI    | KW   | 2     | -1,-5         |
      | SYI    | KW   | 1     | -9            |
      | WAI    | OS   | 0     |               |

  Scenario Outline: Find all staff members having specified position and role at an agency
    When request is submitted for staff members having position "<position>" and role "<role>" in agency "<agency>"
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"

    Examples:
      | agency | position | role | count | staff id list |
      | LEI    | PRO      | KW   | 1     | -1            |
      | SYI    | PRO      | KW   | 0     |               |

  Scenario Outline: Search for staff members having specified role at an agency
    When request is submitted for staff members having role "<role>" in agency "<agency>" with name filter "<name filter>" and staff id filter "<staff id>"
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"

    Examples:
      | agency | role | name filter | staff id | count | staff id list |
      | LEI    | KW   | Another     |          | 1     | -5            |
      | SYI    | KW   |             |          | 1     | -9            |
      | WAI    | OS   | Ronald      |          | 0     |               |
      | LEI    | KW   | USE         |          | 2     | -1,-5         |
      | LEI    | KW   | user        |          | 2     | -1,-5         |
      | LEI    | KW   | Uses        |          | 0     |               |
      | LEI    | KW   |             | -1       | 1     | -1            |
      | LEI    | KW   |             | -999     | 0     |               |

  Scenario Outline: Search for staff members having specified position and role at an agency
    When request is submitted for staff members having position "<position>" and role "<role>" in agency "<agency>" with name filter "<name filter>" and staff id filter "<staff id>"
    Then "<count>" staff detail records are returned
    And staff ids match "<staff id list>"

    Examples:
      | agency | position | role | name filter | staff id | count | staff id list |
      | LEI    | AO       | KW   | Another     |          | 1     | -5            |
      | SYI    | AO       | KW   |             |          | 1     | -9            |
      | WAI    | PRO      | OS   | Ronald      |          | 0     |               |
      | LEI    | PRO      | KW   | USE         |          | 1     | -1            |
      | LEI    | AO       | KW   | user        |          | 1     | -5            |
      | LEI    | AO       | KW   | Uses        |          | 0     |               |
      | LEI    | AO       | KW   |             | -5       | 1     | -5            |
      | LEI    | AO       | KW   |             | -999     | 0     |               |
