Feature: Authentication

  Acceptance Criteria:
  A staff user can login to the API

  Scenario: Login to the API
    When API authentication is attempted with the following credentials:
    | username  | ITAG_USER  |
    | password  | password  |
    Then a valid JWT token is generated
    And current user details match the following:
      | username  | ITAG_USER  |
      | firstName | API  |
      | lastName  | USER  |
