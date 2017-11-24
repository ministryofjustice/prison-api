@global
Feature: Reference Domains

  Acceptance Criteria
  A logged on staff user can obtain a list of all alert types with alert codes (active and/or inactive).
  A logged on staff user can obtain a list of all case note sources.
  A logged on staff user can obtain a list of all case note types with sub-types (active and/or inactive).

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve all alert types with alert codes
    When request submitted to retrieve all alert types with alert codes
    Then "14" reference code items are returned
    And domain for all returned items is "ALERT"
    And codes of returned items are "A,C,H,L,M,O,P,R,S,T,TEST,V,W,X"
    And there are one or more sub codes for every returned item
    And domain for all returned item sub-codes is "ALERT_CODE"

  Scenario: Retrieve all case note sources
    When request submitted to retrieve all case note sources
    Then "4" reference code items are returned
    And domain for all returned items is "NOTE_SOURCE"
    And codes of returned items are "AUTO,COMM,EXT,INST"
    And descriptions of returned items are "System,Community,External,Prison"
    And there is no parent domain for any returned item
    And there are no sub codes for any returned item

  @broken
  Scenario: Retrieve reference codes for a domain without sub-codes
    When request submitted to retrieve all reference codes, without sub-codes, for domain "domain"
    Then "6" reference code items are returned

  @broken
  Scenario: Retrieve reference codes for a domain with sub-codes
    When request submitted to retrieve all reference codes, with sub-codes, for domain "domain"
    Then "12" reference code items are returned

  @broken
  Scenario Outline: Retrieve specific reference code
    When reference code with domain "<domain>" and code "<code>" is requested

    Examples:
      | domain | code | description    | activeFlag | parentDomain | parentCode |
      | DOMAIN | CODE | description    | Y          |              |            |
