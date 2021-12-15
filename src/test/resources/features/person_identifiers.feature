Feature: Person Identifiers

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve identifiers for a personId that does not exist
    When the identifiers for a person represented by "1000" are requested
    Then there are no returned identifiers

  Scenario: Retrieve the identifiers for a personId having multiple identifiers
    When the identifiers for a person represented by "-1" are requested
    Then the returned identifiers are:
      | identifierType | identifierValue  |
      | EXTERNAL_REL   | DELIUS_1_2       |
      | DL             | NCDON805157PJ9FR |
      | PASS           | PB1575411        |
      | MERGED         | A1408CM          |
      | CRO            | 135196/95W       |

