Feature: Booking Contacts

  Acceptance Criteria:
  A logged in staff user can retrieve correct booking details for a provided offender booking id.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Request for next of kin information about an offender
    When contact details with booking id <bookingId> is requested
    Then the next of kin lastName is "<lastName>"
    And the next of kin firstName is "<firstName>"
    And the next of kin middleName is "<middleName>"
    And the next of kin contactType is "<contactType>"
    And the next of kin contactTypeDescription is "<contactTypeDescription>"
    And the next of kin relationship is "<relationship>"
    And the next of kin relationshipDescription is "<relationshipDescription>"
    And the next of kin emergencyContact is "<emergencyContact>"

    Examples:
      | bookingId | lastName  | firstName | middleName | contactType | contactTypeDescription | relationship | relationshipDescription | emergencyContact |
      | -1        | SMITH1    | JESSY     |            | S           | Social/Family          | UN           | Uncle                   |      false       |
      | -2        | Smith     | John      | asdf       | S           | Social/Family          | BOF          | Boyfriend               |      true        |
      | -3        | JOHNSON   | JOHN      | JUSTICE    | S           | Social/Family          | BRO          | Brother                 |      false       |

  Scenario Outline: Request for non next of kin information about an offender
    When contact details with booking id <bookingId> is requested
    Then the other contacts lastName is "<lastName>"
    And the other contacts firstName is "<firstName>"
    And the other contacts middleName is "<middleName>"
    And the other contacts contactType is "<contactType>"
    And the other contacts contactTypeDescription is "<contactTypeDescription>"
    And the other contacts relationship is "<relationship>"
    And the other contacts relationshipDescription is "<relationshipDescription>"
    And the other contacts emergencyContact is "<emergencyContact>"

    Examples:
      | bookingId | lastName  | firstName | middleName | contactType | contactTypeDescription | relationship | relationshipDescription    | emergencyContact |
      | -1        | BREAKFAST | ENGLISH   |            | O           | Official               | COM          | Community Offender Manager |      false       |

  Scenario: Offender has no next of kin, no data
    When contact details with booking id -4 is requested
    Then There is no next of kin

  Scenario: Offender has no next of kin, flag=N
    When contact details with booking id -7 is requested
    Then There is no next of kin

  Scenario: Offender has more than 1 next of kin
    When contact details with booking id -10 is requested
    Then the next of kin results are:
      | lastName  | firstName | middleName | contactType | contactTypeDescription | relationship | relationshipDescription | emergencyContact |
      | SMITH13   | JESSY     |            | S           | Social/Family          | SDAU         | Stepdaughter            |      true        |
      | ROBERTSON | ELLY      |            | S           | Social/Family          | FRI          | Friend                  |      false       |
