Feature: Change the current IEP Level for a booking

  Scenario: An authorised user attempts to change the current IEP Level of a booking within their caseloads.
    Given a user has a token name of "MAINTAIN_IEP"
    And a booking having id "-4"
    And the new IEP Level should be "STD" with comment "A Comment"
    When the new level is applied to the booking
    Then the response status code is "204"

  Scenario: An authorised user attempts to change the current IEP Level of a booking outside their caseloads.
    Given a user has a token name of "MAINTAIN_IEP"
    And a booking having id "-54"
    And the new IEP Level should be "STD" with comment "A Comment"
    When the new level is applied to the booking
    Then the response status code is "404"
    And the error response message contains "Offender booking with id -54 not found."

  Scenario: A user who is not authorised to change IEP Levels attempts to change the current IEP level of a booking within their caseloads.
    Given a user has a token name of "NORMAL_USER"
    And a booking having id "-4"
    And the new IEP Level should be "STD" with comment "A Comment"
    When the new level is applied to the booking
    Then the response status code is "403"
    And the error response message contains "Access is denied"

  Scenario: An authorised user attempts to change the current IEP Level of a booking within their caseloads when the new IEP Level is not valid.
    Given a user has a token name of "MAINTAIN_IEP"
    And a booking having id "-4"
    And the new IEP Level should be "" with comment ""
    When the new level is applied to the booking
    Then the response status code is "400"
    And the error response message contains "The IEP level must not be blank"
    And the error response message contains "The IEP comment must not be blank"
    And the error response message contains "The IEP level must have comment text of between 1 and 240 characters"
