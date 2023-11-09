Feature: Key worker details

  Acceptance Criteria:
  A logged in staff user can retrieve details of a Key worker within their caseload.

  Background:
    Given a user has authenticated with the API
    And a user has a token name of "KEY_WORKER"

#  Scenario: Request for key worker allocations for multiple staff Ids
#    When a key worker allocations request is made with staff ids "-5,-4" and agency "LEI"
#    Then the key worker has 5 allocations
#
#  Scenario: Request for key worker allocation history for multiple offender Nos
#    When a key worker allocation history request is made with nomis ids "A9876RS,A5576RS,A1176RS,A1234AP"
#    Then the key worker has 5 allocation history entries
