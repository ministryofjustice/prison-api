@nomis @wip
Feature: Booking Activities

  Acceptance Criteria
  A logged on staff user can retrieve scheduled activities for an offender booking.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve scheduled activities for an existing offender that is not in a caseload accessible to authenticated user.
    When scheduled activities are requested for an offender with booking id "-16"
    Then resource not found response is received from booking activities API

  Scenario: Retrieve scheduled activities for an existing offender that does not have any scheduled activities.
    When scheduled activities are requested for an offender with booking id "-9"
    Then response is an empty list