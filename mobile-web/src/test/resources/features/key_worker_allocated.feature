@nomis
Feature: Keyworker Allocations

  Acceptance Criteria:
    A logged in staff user can retrieve a list of Key worker allocations for an agency that is accessible to them.
    The list can be filtered by:
      date range (toDate is defaulted to system date if not present, fromDate will be excluded from query if not present)
      allocation type (A - auto or M - manually allocated)

  Background:
    Given a user has authenticated with the API

  Scenario: Request for allocated offenders for specified agency, auto allocated only
    When an allocated offender request is made with agency "LEI", type "A", from date "2017-01-01" and to date "2017-06-01"
    Then a list of "1" allocated offenders are returned
    And I look at allocated response row "1"
    And allocated first name matches "HARRY"
    And allocated last name matches "SARLY"
    And allocated agencyId matches "LEI"
    And allocated allocation type matches "A"
    And allocated internal location matches "H-1"
    And allocated assigned date matches "2017-05-01 11:14:00"

  Scenario: Request for allocated offenders without date range
    When an allocated offender request is made with agency "LEI", type "M", from date "" and to date ""
    Then a list of "10" allocated offenders are returned
    And the list is sorted by offender name asc

  Scenario: Request with future to Date
    When an allocated offender request is made with agency "LEI", type "A", from date "" and to date "2029-05-05"
    Then a bad request response is received with message "Invalid date range: toDate cannot be in the future."
