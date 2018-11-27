Feature: Finances

  Acceptance Criteria
  A logged on staff user can obtain an offender's account balances.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve balances
    When an account with booking id -1 is requested
    Then the returned account cash is 1.24
    And the returned account spends is 2.50
    And the returned account savings is 200.50
    And the returned account currency is GBP

  Scenario: Booking id does not exist
    When an account with booking id -99 is requested
    Then resource not found response is received from finance API

  Scenario: The logged on staff user's caseload does not include the booking id
    When an account with booking id -16 is requested
    Then resource not found response is received from finance API

  Scenario: Request balances for offender that has no finance records
    When an account with booking id -32 is requested
    Then the returned account cash is 0.00
    And the returned account spends is 0.00
    And the returned account savings is 0.00
    And the returned account currency is GBP
