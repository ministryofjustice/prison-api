@global
Feature: Case Note Creation and Update

  Acceptance Criteria:
  A logged in staff user can create and update case notes for an existing offender booking

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized

  Scenario: Create a case note
    When a case note is created for booking:
      | bookingId          | -15                                         |
      | type               | COMMS                                       |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note is successfully created
    And correct case note source is used

  Scenario: Create a case note with invalid type
    When a case note is created for booking:
      | bookingId          | -15                                         |
      | type               | doesnotexist                                |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "Reference type/subtype=doesnotexist/COM_IN does not exist" occurs

  Scenario: Create a case note with invalid subType
    When a case note is created for booking:
      | bookingId          | -15                                         |
      | type               | COMMS                                       |
      | subType            | doesnotexist                                |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "Reference type/subtype=COMMS/doesnotexist does not exist" occurs

  Scenario: Update a case note
    When existing case note is updated with text "Updated Case Note"
    Then case note is successfully updated with "Updated Case Note"
    And the original text is not replaced

  Scenario: Attempt to create case note for offender is not part of any of logged on staff user's caseloads
    When attempt is made to create case note for booking:
      | bookingId          | -16                                         |
      | type               | COMMS                                       |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then resource not found response is received from casenotes API

  Scenario: Attempt to create case note for offender that does not exist
    When attempt is made to create case note for booking:
      | bookingId          | -99                                         |
      | type               | COMMS                                       |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then resource not found response is received from casenotes API

  Scenario: Attempt to update case note for offender that is not part of any of logged on staff user's caseloads
    When attempt is made to update case note for booking with id -16
    Then resource not found response is received from casenotes API

  Scenario: Attempt to update case note for offender that does not exist
    When attempt is made to update case note for booking with id -99
    Then resource not found response is received from casenotes API
