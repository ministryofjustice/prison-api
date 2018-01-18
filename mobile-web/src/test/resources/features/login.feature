@global
Feature: Authentication

  Acceptance Criteria:
  A staff user can login to the API
  A staff user can refresh their JWT

  Scenario: As a staff user I can login and be returned by JWT
    Given API authentication is attempted with the following credentials:
      | username  | itag_user  |
      | password  | password   |
    Then a valid JWT token is generated

  Scenario: User login detail match logged in user
    Given API authentication is attempted with the following credentials:
      | username  | itag_user  |
      | password  | password   |
    Then current user details match the following:
      | username  | itag_user  |
      | firstName | API        |
      | lastName  | User       |

  Scenario: Authentication fails for an invalid username and password
    Given API authentication is attempted with the following credentials:
      | username  | itag_user  |
      | password  | wrongpass  |
    Then authentication denied is returned

  Scenario: As a user I can login and refresh the token
    Given API authentication is attempted with the following credentials:
      | username  | itag_user  |
      | password  | password   |
    And token refresh is attempted
    Then a new token is generated successfully
    And token timeout is valid

  Scenario: JWT expires after configured period of time
    Given API authentication is attempted with the following credentials:
      | username  | itag_user  |
      | password  | password   |
    When I wait until the token as expired
    And a user role request is made after token has expired
    Then authentication denied is returned

  Scenario: JWT expires after configured period of time but I can refresh
    Given API authentication is attempted with the following credentials:
      | username  | itag_user  |
      | password  | password   |
    When I wait until the token as expired
    And token refresh is attempted
    Then a new token is generated successfully
    And token timeout is valid

  Scenario: JWT refresh expires after configured period of time and I can no longer refresh
    Given API authentication is attempted with the following credentials:
      | username  | itag_user  |
      | password  | password   |
    When I wait until the refresh token as expired
    And token refresh is attempted
    Then authentication denied is returned