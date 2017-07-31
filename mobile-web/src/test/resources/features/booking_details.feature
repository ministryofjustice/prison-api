@global
Feature: Booking Details

  Acceptance Criteria:
  A logged in staff user can retrieve correct booking details for a provided offender booking id.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Request for specific offender booking record
    When an offender booking request is made with booking id "<bookingId>"
    Then booking number of offender booking returned is "<bookingNo>"
    And assigned officer id of offender booking returned is "<assignedOfficerId>"

    Examples:
      | bookingId | bookingNo | assignedOfficerId |
      | -1        | A00111    | -3                |
      | -8        | A00118    |                   |

  Scenario: Request for specific offender booking record that does not exist
    When an offender booking request is made with booking id "9999999999"
    Then http status 404 response is returned from booking details endpoint