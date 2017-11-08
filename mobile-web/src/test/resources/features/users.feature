Feature: Users and Staff

  Acceptance Criteria:
  A logged in user can find staff details for any valid staff id

  Background:
    Given a user has authenticated with the API

  @global
  Scenario Outline: Find staff member using staff id
    When a staff member search is made using staff id "<staffId>"
    Then first name of staff details returned is "<firstName>"
    And last name of staff details returned is "<lastName>"
    And email address of staff details returned is "<email>"

    Examples:
      | staffId | firstName | lastName | email                      |
      | -1      | Elite2    | User     | elite2-api-user@syscon.net |
      | -2      | API       | User     | itaguser@syscon.net        |

  @global
  Scenario: Find staff member using staff id that does not exist
    When a staff member search is made using staff id "-9999"
    Then resource not found response is received from users API

  @nomis
  Scenario Outline: As a logged in user I can find out my roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

  Examples:
  | username            | roles                                  |
  | itag_user           | BXI_WING_OFF,LEI_WING_OFF,WAI_WING_OFF |
  | elite2_api_user     | LEI_CENTRAL_ADMIN                      |
  | hpa_user            | LEI_GLOBAL_SEARCH,LEI_WING_OFF         |
  | api_test_user       | MUL_WING_OFF                           |

  @elite
  Scenario Outline: As a logged in user I can find out my roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | username            | roles                  |
      | itag_user           | WING_OFF               |
      | elite2_api_user     | CENTRAL_ADMIN          |
      | hpa_user            | GLOBAL_SEARCH,WING_OFF |
      | api_test_user       | WING_OFF               |