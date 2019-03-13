Feature: Offender Identifiers

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve the identifiers for a type and value
    When the identifiers are requested for "<identifierType>" and "<identifierValue>"
    Then the Offender Nos returned are "<offenderNos>"
    And the Booking Ids returned are "<bookingIds>"

    Examples:
      | identifierType | identifierValue  | offenderNos | bookingIds |
      | CRO            | CRO112233        | A1234AC     | -3         |
      | MERGED         | A3333AB          | A1234AA     | -1         |
