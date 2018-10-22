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
  | username            | roles                                                                          |
  | itag_user           | BXI_WING_OFF,LEI_WING_OFF,MDI_WING_OFF,NWEB_ACCESS_ROLE_ADMIN,NWEB_KW_ADMIN,NWEB_OMIC_ADMIN,SYI_WING_OFF,WAI_WING_OFF,NWEB_MAINTAIN_ACCESS_ROLES,NWEB_MAINTAIN_ACCESS_ROLES_ADMIN |
  | api_test_user       | MUL_WING_OFF,NWEB_KW_ADMIN,NWEB_OMIC_ADMIN                                                     |

  @nomis
  Scenario Outline: As a logged in user I can find out just my api roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | username            | roles                |
      | itag_user           | KW_ADMIN,OMIC_ADMIN,ACCESS_ROLE_ADMIN,MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN  |
      | api_test_user       | KW_ADMIN,OMIC_ADMIN  |
      | ca_user             | LICENCE_CA           |
      | ro_user             | LICENCE_RO           |
      | dm_user             | LICENCE_DM           |

  @elite
  Scenario Outline: As a logged in user I can find out my roles
    Given user "<username>" with password "password" has authenticated with the API
    When a user role request is made
    Then the roles returned are "<roles>"

    Examples:
      | username            | roles                        |
      | itag_user           | WING_OFF,KW_ADMIN,OMIC_ADMIN,MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN |
      | api_test_user       | WING_OFF                     |

  @nomis
  Scenario Outline: As a logged in user I can find out which users have a given role at a particular caseload
    Given a user has authenticated with the API
    When a request for users having role "<role>" at caseload "<caseload>" is made
    Then the matching "<usernames>" are returned

    Examples:
    | role     | caseload | usernames                          |
    | WING_OFF | LEI      | ELITE2_API_USER,ITAG_USER,JBRIEN,NONWEB,RENEGADE |
    | WING_OFF | MUL      | API_TEST_USER                      |
    | WING_OFF | XXXXXX   |                                    |
    | XXXXX    | LEI      |                                    |
    | KW_ADMIN | NWEB     | API_TEST_USER,ITAG_USER            |

  @nomis
  Scenario: A trusted client can make api-role assignments to users.
    Given a trusted client that can maintain access roles has authenticated with the API
    When the client assigns api-role "KW_ADMIN" to user "API_TEST_USER"
    Then user "API_TEST_USER" has been assigned api-role "KW_ADMIN"

  @nomis
  Scenario: A trusted client can make the same api-role assignments to a user more than once
    Given a trusted client that can maintain access roles has authenticated with the API
    And the client assigns api-role "KW_ADMIN" to user "API_TEST_USER"
    When the client assigns api-role "KW_ADMIN" to user "API_TEST_USER"
    Then user "API_TEST_USER" has been assigned api-role "KW_ADMIN"

  @nomis
  Scenario: A trusted client can make access role assignments to users.
    Given a trusted client that can maintain access roles has authenticated with the API
    When the client assigns access role "ACCESS_ROLE_GENERAL" to user "JBRIEN" for caseload "LEI"
    Then user "JBRIEN" has been assigned access role "ACCESS_ROLE_GENERAL" for caseload "LEI"

  @nomis
  Scenario: A trusted client is prevented from assigning an access role for a user without caseload access.
    Given a trusted client that can maintain access roles has authenticated with the API
    When the client assigns access role "ACCESS_ROLE_GENERAL" to user "JBRIEN" for caseload "MDI"
    Then resource not found response is received from users API

  @nomis
  Scenario: A trusted client can remove a role assginment
    Given a trusted client that can maintain access roles has authenticated with the API
    And the client assigns api-role "KW_ADMIN" to user "API_TEST_USER"
    When the client removes role "KW_ADMIN" from user "API_TEST_USER" at caseload "NWEB"
    Then user "API_TEST_USER" does not have role "KW_ADMIN" at caseload "NWEB"

  @nomis
  Scenario: A list of staff users by caseload can be retrieved
    Given a user has authenticated with the API
    When a request for users with caseload "LEI" is made
    Then a list of users is returned with usernames "ELITE2_API_USER,ITAG_USER,JBRIEN,NONWEB,RENEGADE,CA_USER,DM_USER"

  @nomis
  Scenario: A list of staff users can be retrieved
    Given a user has authenticated with the API
    When a request for users is made
    Then a list of users is returned with usernames "ELITE2_API_USER,ITAG_USER,JBRIEN,NONWEB,RENEGADE,CA_USER,DM_USER,EXOFF5,API_TEST_USER,RO_USER"

  @nomis
  Scenario: A list of staff users by caseload and namefilter can be retrieved
    Given a user has authenticated with the API
    When a request for users with caseload "LEI" and namefilter "User" and role "" is made
    Then a list of users is returned with usernames "ELITE2_API_USER,ITAG_USER,CA_USER,DM_USER"

  @nomis
  Scenario: A list of staff users by caseload and namefilter and access role can be retrieved
    Given a user has authenticated with the API
    When a request for users with caseload "LEI" and namefilter "User" and role "OMIC_ADMIN" is made
    Then a list of users is returned with usernames "ITAG_USER"

  @nomis
  Scenario: A list of local administrator staff users by caseload and namefilter can be retrieved
    Given a user has authenticated with the API
    When a request for local administrator users with caseload "LEI" and namefilter "User" and role "" is made
    Then a list of users is returned with usernames "ITAG_USER,CA_USER"

  @nomis
  Scenario: A list of local administrator staff users by caseload and namefilter and access role can be retrieved
    Given a user has authenticated with the API
    When a request for local administrator users with caseload "LEI" and namefilter "User" and role "OMIC_ADMIN" is made
    Then a list of users is returned with usernames "ITAG_USER"

  @nomis
  Scenario: A list of staff roles for a user and caseload can be retrieved
    Given a user has authenticated with the API
    When a request for roles for user "ITAG_USER" with caseload "NWEB" is made
    Then a list of roles is returned with role codes "KW_ADMIN,OMIC_ADMIN,MAINTAIN_ACCESS_ROLES_ADMIN,MAINTAIN_ACCESS_ROLES"
