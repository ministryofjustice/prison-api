Feature: Case Note Amendment

  Acceptance Criteria:
  A logged in staff user can amend a case note they created.
  A logged in staff user cannot amend a case note that they did not create.

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized

  Scenario: Attempt to update case note for offender that is not part of any of logged on staff user's caseloads
    When attempt is made to update case note for booking with id "-16"
    Then resource not found response is received from casenotes API

  Scenario: Attempt to update case note for offender that does not exist
    When attempt is made to update case note for booking with id "-99"
    Then resource not found response is received from casenotes API

  Scenario: A staff user can amend a case note they created
    When existing case note is updated with valid text
    Then case note is successfully updated with valid text
    And the original text is not replaced

  Scenario: A staff user cannot amend a case note that they did not create
    When existing case note for a different user is updated with valid text
    Then access denied response, with "User not authorised to amend case note." message, is received from booking case notes API

  Scenario: Update a case note with blank data
    When existing case note is updated with text "  "
    Then case note validation error "Case Note text is blank" occurs

  Scenario: Update a case note with data which is too long
    When the created case note is updated with long text
    Then case note validation error "Length should not exceed 3880 characters" occurs

  Scenario: Update a case note when there is no space left
    When a case note is created to use up all free space
    When the created case note is updated with long text
    Then case note validation error "Amendments can no longer be made due to the maximum character limit being reached" occurs
