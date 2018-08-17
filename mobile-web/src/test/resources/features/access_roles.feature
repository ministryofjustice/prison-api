@nomis
Feature: Access role maintenance

  Acceptance Criteria:
  A priviledged user can create and update access roles (access roles are used by the new nomis world only)

  Scenario: A trusted client can create a new access role
    Given a trusted client that can maintain access roles has authenticated with the API
    When an access role creation request is made with role code "R2"
    Then access role is successfully created

  Scenario: A trusted client can update an existing access role
    Given a trusted client that can maintain access roles has authenticated with the API
    When an update access role request is made with role code "WING_OFF" and role name "new name"
    Then access role is successfully updated

  Scenario: A client without MAINTAIN_ACCESS_ROLES role is unable to create an access role
    Given user "ro_user" with password "password" has authenticated with the API
    When an access role creation request is made with role code "R2"
    Then the create access role request is rejected

  Scenario: A client without MAINTAIN_ACCESS_ROLES role is unable to update an access role
    Given user "ro_user" with password "password" has authenticated with the API
    When an update access role request is made with role code "WING_OFF" and role name "new name"
    Then the update access role request is rejected
