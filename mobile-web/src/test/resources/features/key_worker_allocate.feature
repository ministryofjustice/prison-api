@global @nomis
Feature: Allocate Offender to a keyworker

  Acceptance Criteria:
  A logged in staff user can allocate a keyworker to an offender
  on the staff user's caseload

  Background:
    Given a user has authenticated with the API

  @allocate-database-cleanup
  Scenario: Manually override keyworker of an auto-allocated offender
    When offender booking "-33" is allocated to staff user id "-5" with reason "override" and type "M"
    Then the allocation is successfully created

  @allocate-database-cleanup
  Scenario: Auto-allocate an unallocated offender
    When offender booking "-34" is allocated to staff user id "-5" with reason "autoallocate" and type "A"
    Then the allocation is successfully created

  Scenario: Offender doesnt exist
    When offender booking "-999" is allocated to staff user id "-2" with reason "autoallocate" and type "A"
    Then the allocation returns a 404 resource not found with message 'Resource with id [-999] not found.'

  Scenario: Offender not in caseload
    When offender booking "-16" is allocated to staff user id "-2" with reason "autoallocate" and type "A"
    Then the allocation returns a 404 resource not found with message 'Resource with id [-16] not found.'

  Scenario: Staff user doesnt exist
    When offender booking "-1" is allocated to staff user id "-999" with reason "autoallocate" and type "A"
    Then the allocation returns a 404 resource not found with message 'Key worker with id -999 not available for offender -1'

  Scenario: Staff user in wrong prison
    When offender booking "-1" is allocated to staff user id "-4" with reason "autoallocate" and type "A"
    Then the allocation returns a 404 resource not found with message 'Key worker with id -4 not available for offender -1'

  Scenario: Invalid reason code
    When offender booking "-1" is allocated to staff user id "-2" with reason "strnglength13" and type "M"
    Then the allocation returns a 401 bad request with message 'Value is too long: max length is 12'

  Scenario: Invalid type
    When offender booking "-1" is allocated to staff user id "-2" with reason "autoallocate" and type "X"
    Then the allocation returns a 401 bad request with message 'Value contains invalid characters: must match '[AM]''

# PK allows multiple allocations with different start times - maybe this check isnt needed - pending clarification
  @broken
  Scenario: Allocation already exists
    When offender booking "-1" is allocated to staff user id "-5" with reason "repeated" and type "M"
    Then the allocation returns a 401 bad request with message 'Allocation already exists'
