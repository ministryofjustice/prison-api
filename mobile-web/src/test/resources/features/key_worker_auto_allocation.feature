@nomis
Feature: Automatically allocate one or more unallocated offenders to one or more available Key workers.

  Acceptance Criteria:
  A logged in staff user can initiate auto-allocation of unallocated offenders to available staff members at a specified
  agency that is accessible to them.

  Background:
    Given a user has authenticated with the API

  Scenario: Logged in staff member initiates auto-allocation for an agency that does not exist
    When auto-allocation initiated for agency "ALI"
    Then the allocation returns a 404 resource not found with message 'Resource with id [ALI] not found.'

  Scenario: Logged in staff member initiates auto-allocation for an agency that is not accessible to them
    When auto-allocation initiated for agency "BMI"
    Then the allocation returns a 404 resource not found with message 'Resource with id [BMI] not found.'

  Scenario: Auto-allocation initiated for valid agency which has no unallocated offenders
    When auto-allocation initiated for agency "MDI"
    Then auto-allocation requests responds with "0" allocations processed

  Scenario: Auto-allocation initiated for valid agency with an unallocated offender but no available Key workers
    When auto-allocation initiated for agency "BXI"
    Then auto-allocation request fails with a resource conflict error with message "No Key workers available for allocation."

  Scenario: Auto-allocation initiated for valid agency with an unallocated offender and an available Key worker
    When auto-allocation initiated for agency "WAI"
    Then auto-allocation requests responds with "1" allocations processed
    And there are 0 unallocated offenders for agency "WAI"

  Scenario: Auto-allocation initiated for valid agency with some unallocated offenders and an available Key worker with insufficent capacity to be allocated all offenders
    When auto-allocation initiated for agency "SYI"
    Then auto-allocation request fails with a resource conflict error with message "All available Key workers are at full capacity."
    And there are 1 unallocated offenders for agency "SYI"