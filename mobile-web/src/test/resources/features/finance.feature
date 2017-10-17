@global
Feature: Finances

  Acceptance Criteria
  A logged on staff user can obtain an offender's account balances.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve balances
    When an account with booking id -1 is requested
    Then the returned cash is 1.24
    And the returned spends is 2.50
    And the returned savings is 200.50

  Scenario: Booking id does not exist
    When an account with nonexistent booking id is requested
    Then resource not found response is received from finance API

  Scenario: The logged on staff user's caseload does not include the booking id
    When an account with booking id in different caseload is requested
    Then resource not found response is received from finance API
