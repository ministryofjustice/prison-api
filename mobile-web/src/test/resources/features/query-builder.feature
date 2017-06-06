Feature:

  Acceptance Criteria:
  A staff user can login to the application

  Scenario: Login to the api
    When I call the login endpoint with the following credentials:
      | username  | ITAG_USER |
      | password  | password  |
    Then I receive a JWT token response
    And I when I lookup my details I get the following data:
      | username  | ITAG_USER |
      | firstName | API       |
      | lastName  | USER      |
