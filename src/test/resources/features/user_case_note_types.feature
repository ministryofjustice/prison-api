Feature: User Case Note Types

  Acceptance Criteria:
  Return case note types for logged in staff users based on user-related context information (e.g. number of caseloads/agencies):

  Scenario: Retrieve valid case note types for current user
    Given user "itag_user" with password "password" has authenticated with the API
    When request is made to retrieve valid case note types for current user
    Then "11" case note types are returned
    And each case note type is returned with one or more sub-types
