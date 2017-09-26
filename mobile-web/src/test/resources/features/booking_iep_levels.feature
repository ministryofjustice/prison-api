@global
Feature: Booking Incentives & Earned Privileges

  Acceptence Criteria
  A logged on staff user can retrieve an IEP summary for an offender booking:
    - with IEP details.
    - without IEP details.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve IEP summary for an offender (without IEP details).
    When an IEP summary only is requested for an offender with booking id "<bookingId>"
    Then IEP summary is returned with IEP level of "<iepLevel>"
    And IEP summary contains "<iepDetailCount>" detail records
    And IEP days since review is correct for IEP date of "<iepDate>"

    Examples:
      | bookingId | iepLevel | iepDetailCount | iepDate    |
      | -1        | STD      | 0              | 2017-08-15 |
      | -2        | BAS      | 0              | 2017-09-06 |

  Scenario Outline: Retrieve IEP summary for an offender (with IEP details).
    When an IEP summary, with details, is requested for an offender with booking id "<bookingId>"
    Then IEP summary is returned with IEP level of "<iepLevel>"
    And IEP summary contains "<iepDetailCount>" detail records
    And IEP days since review is correct for IEP date of "<iepDate>"

    Examples:
      | bookingId | iepLevel | iepDetailCount | iepDate    |
      | -1        | STD      | 1              | 2017-08-15 |
      | -2        | BAS      | 2              | 2017-09-06 |

  Scenario: Retrieve IEP summary for an existing offender that does not have any IEP detail records.
    When an IEP summary, with details, is requested for an offender with booking id "-9"
    Then resource not found response is received from bookings IEP summary API