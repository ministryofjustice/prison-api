Feature: Staff Details and Roles

  Acceptance Criteria:
  A logged in user can:
   - find staff details for any valid staff id.
   - retrieve a paged list of staff members having a specified position in an agency, optionally filtering the list
     by staff member name.
   - find a list of email addresses associated with an existing staff id.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Find staff member using staff id
    When a staff member search is made using staff id "<staffId>"
    Then first name of staff details returned is "<firstName>"
    And last name of staff details returned is "<lastName>"
    And gender of staff details returned is "<gender>"
    And date of birth of staff details returned is "<dob>"
# Email removed (Note staff member can have more than one email address)
#    And email address of staff details returned is "<email>"

    Examples:
      | staffId | firstName | lastName | gender |   dob      |
      | -1      | PRISON    | USER     |   F    | 1970-01-01 |
      | -2      | API       | USER     |   M    | 1970-02-01 |
      | -3      | CA        | USER     |   M    | 1970-03-01 |
      | -5      | RO        | USER     |   M    | 1970-05-01 |
      | -6      | DM        | USER     |   M    | 1970-06-01 |

  Scenario: Find staff member using staff id that does not exist
    When a staff member search is made using staff id "-9999"
    Then resource not found response is received from staff API

  Scenario Outline: List all active job roles for staff member at an agency
      When request is submitted using "<staffId>" and "<agencyId>"
      Then a role containing "<role>" "<roleDescription>" is returned without duplicates
   Examples:
      | staffId |agencyId  |role  | roleDescription     |
      | -2      |LEI       |OS    | Offender Supervisor|
      | -1      |LEI       |KW    | Key Worker         |
      | -2      |BXI       |KW    | Key Worker         |

  Scenario Outline: Retrieve a list of email addresses for a specified staff member
     When request is submitted for email addresses associated with staff id "<staffId>"
     Then "<count>" email address records are returned
     And response code matches "<responseCode>"
     And response body is "<presentOrEmpty>"
    Examples:
    | staffId | count | responseCode | presentOrEmpty |
    | -1      | 1     | 200          | present        |
    | -2      | 2     | 200          | present        |
    | -7      | 0     | 204          | empty          |
    |99999    | 0     | 404          | present        |