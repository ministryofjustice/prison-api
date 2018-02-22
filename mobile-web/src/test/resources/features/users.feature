@global
Feature: Users and Staff

  Acceptance Criteria:
  A logged in user can find staff details for any valid staff id

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Find staff member using staff id
    When a staff member search is made using staff id "<staffId>"
    Then first name of staff details returned is "<firstName>"
    And last name of staff details returned is "<lastName>"
    And email address of staff details returned is "<email>"

    Examples:
      | staffId | firstName | lastName | email                      |
      | -1      | Elite2    | User     | elite2-api-user@syscon.net |
      | -2      | API       | User     | itaguser@syscon.net        |

  Scenario: Find staff member using staff id that does not exist
    When a staff member search is made using staff id "-9999"
    Then resource not found response is received from users API

  @nomis
  Scenario Outline: As a logged in user I can find out all my roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made for all roles
    Then the roles returned are "<roles>"

  Examples:
  | username            | roles                                                                            |
  | itag_user           | BXI_WING_OFF,LEI_WING_OFF,MDI_WING_OFF,NWEB_LICENCE_CA,SYI_WING_OFF,WAI_WING_OFF |
  | api_test_user       | MUL_WING_OFF,NWEB_LICENCE_RO                                                     |

  @nomis
  Scenario Outline: As a logged in user I can find out just my api roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | username            | roles      |
      | itag_user           | LICENCE_CA |
      | api_test_user       | LICENCE_RO |

  @elite
  Scenario Outline: As a logged in user I can find out my roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | username            | roles               |
      | itag_user           | WING_OFF,LICENCE_CA |
      | api_test_user       | WING_OFF,LICENCE_RO |