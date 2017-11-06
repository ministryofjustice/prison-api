@global
Feature: Case Note Creation and Update

  Acceptance Criteria:
  A logged in staff user can create and update case notes for an existing offender booking

  Background:
    Given a user has authenticated with the API
    And case note test harness initialized
    And I have created a case note with text of "Hello this is a new case note"

  Scenario: Create a case note
    When a case note is created for an existing offender booking:
      | type               | COMMS                                       |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note is successfully created
    And correct case note source is used

  Scenario: Create a case note with nonexistent type
    When a case note is created for an existing offender booking:
      | type               | doesnotexist                                |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "Reference (type,subtype)=(doesnotexist,COM_IN) does not exist" occurs

  Scenario: Create a case note with nonexistent subType
    When a case note is created for an existing offender booking:
      | type               | COMMS                                       |
      | subType            | doesnotexist                                |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation error "Reference (type,subtype)=(COMMS,doesnotexist) does not exist" occurs

  Scenario: Create a case note with invalid type
    When a case note is created for an existing offender booking:
      | type               | invalid%charsandtoolong                     |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value is too long: max length is 12|Reference (type,subtype)=(invalid%charsandtoolong,COM_IN) does not exist|Value contains invalid characters: must match '\\w*'|

  Scenario: Create a case note with invalid subType
    When a case note is created for an existing offender booking:
      | type               | COMMS                                       |
      | subType            | invalid%charsandtoolong                     |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value is too long: max length is 12|Reference (type,subtype)=(COMMS,invalid%charsandtoolong) does not exist|Value contains invalid characters: must match '\\w*'|

  Scenario: Create a case note with blank type
    When a case note is created for an existing offender booking:
      | type               |                                             |
      | subType            | COM_IN                                      |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value cannot be blank|Reference (type,subtype)=(,COM_IN) does not exist|

  Scenario: Create a case note with blank subType
    When a case note is created for an existing offender booking:
      | type               | COMMS                                       |
      | subType            |                                             |
      | text               | A new case note (from Serenity BDD test **) |
      | occurrenceDateTime | 2017-04-14T10:15:30                         |
    Then case note validation errors are:
      |Value cannot be blank|Reference (type,subtype)=(COMMS,) does not exist|

  Scenario: Update a case note
    When the created case note is updated with text "Updated Case Note"
    Then case note is successfully updated with "Updated Case Note"
    And the original text is not replaced

  Scenario: Update a case note with blank data
    When the created case note is updated with text "  "
    Then case note validation error "Case Note text is blank" occurs

  Scenario: Update a case note with data which is too long
    When the created case note is updated with long text
    Then case note validation error "Case Note text is over 4000 characters" occurs

  Scenario: The logged on staff user's caseload does not include the booking id - create
    When a case note with booking id in different caseload is created
    Then resource not found response is received from caseload API

  Scenario: The logged on staff user's caseload does not include the booking id - update
    When a case note with booking id in different caseload is updated
    Then resource not found response is received from caseload API
