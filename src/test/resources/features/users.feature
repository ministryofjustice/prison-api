Feature: User Details and Roles

  Acceptance Criteria:
  A logged in user can find their roles.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: As a logged in user I can find out all my roles
    Given a user has a token name of "<token>"
    When a user role request is made for all roles
    Then the roles returned are "<roles>"

  Examples:
  | token            | roles                                                                          |
  | NORMAL_USER      | BXI_WING_OFF,LEI_WING_OFF,MDI_WING_OFF,NWEB_ACCESS_ROLE_ADMIN,NWEB_KW_ADMIN,NWEB_OMIC_ADMIN,SYI_WING_OFF,WAI_WING_OFF,NWEB_MAINTAIN_ACCESS_ROLES,NWEB_MAINTAIN_ACCESS_ROLES_ADMIN |
  | API_TEST_USER    | MUL_WING_OFF,NWEB_KW_ADMIN,NWEB_OMIC_ADMIN                                                     |

  Scenario Outline: As a logged in user I can find out just my api roles
    Given a user has a token name of "<token>"
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | token               | roles                |
      | NORMAL_USER         | KW_ADMIN,OMIC_ADMIN,ACCESS_ROLE_ADMIN,MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN  |
      | API_TEST_USER       | KW_ADMIN,OMIC_ADMIN  |
      | NO_CASELOAD_USER    | VIEW_PRISONER_DATA,LICENCE_RO |

