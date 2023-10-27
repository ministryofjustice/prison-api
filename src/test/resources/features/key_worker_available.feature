Feature: Available Key workers

  Acceptance Criteria:
  A logged in staff user can retrieve a list of Key workers that are available for allocation.

  Background:
    Given a user has authenticated with the API

  Scenario: Request for available key workers for specified agency
    When an available key worker request is made with agency id "LEI"
    Then a list of "4" key workers are returned
