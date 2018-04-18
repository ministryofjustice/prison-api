@global
Feature: User Details and Roles

  Acceptance Criteria:
  A logged in user can find their roles.

  Background:
    Given a user has authenticated with the API

  @nomis
  Scenario Outline: As a logged in user I can find out all my roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made for all roles
    Then the roles returned are "<roles>"

  Examples:
  | username            | roles                                                                                          |
  | itag_user           | BXI_WING_OFF,LEI_WING_OFF,MDI_WING_OFF,NWEB_LICENCE_CA,NWEB_KW_ADMIN,SYI_WING_OFF,WAI_WING_OFF |
  | api_test_user       | MUL_WING_OFF,NWEB_LICENCE_RO                                                                   |

  @nomis
  Scenario Outline: As a logged in user I can find out just my api roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | username            | roles               |
      | itag_user           | LICENCE_CA,KW_ADMIN |
      | api_test_user       | LICENCE_RO          |

  @elite @wip
  Scenario Outline: As a logged in user I can find out my roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | username            | roles                        |
      | itag_user           | WING_OFF,LICENCE_CA,KW_ADMIN |
      | api_test_user       | WING_OFF,LICENCE_RO          |