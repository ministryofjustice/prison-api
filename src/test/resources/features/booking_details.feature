Feature: Booking Details

  Acceptance Criteria:
  A logged in staff user can retrieve correct booking details for a provided offender booking id.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Request for specific offender booking record - assigned officer, CSRA and category
    When an offender booking request is made with booking id "<bookingId>"
    Then booking number of offender booking returned is "<bookingNo>"
    And the CSRA is "<csra>"
    And the category is "<category>"

    Examples:
      | bookingId | bookingNo | csra | category                     |
      | -1        | A00111    | High | Low                          |
      | -2        | A00112    |      |                              |
      | -3        | A00113    | Low  | Uncategorised Sentenced Male |
      | -8        | A00118    |      |                              |

  Scenario Outline: Request for specific offender booking record basic details only
    When a basic offender booking request is made with booking id "<bookingId>"
    Then booking number of offender booking returned is "<bookingNo>"
    And firstname of offender booking returned is "<firstName>"
    And lastName of offender booking returned is "<lastName>"
    And offenderNo of offender booking returned is "<offenderNo>"
    And activeFlag of offender booking returned is "<activeFlag>"

    Examples:
      | bookingId | bookingNo | firstName | lastName | offenderNo | activeFlag |
      | -1        | A00111    | ARTHUR    | ANDERSON | A1234AA    | true       |
      | -2        | A00112    | GILLIAN   | ANDERSON | A1234AB    | true       |


  Scenario Outline: Request for specific offender booking record - physical attributes
    When an offender booking request is made with booking id "<bookingId>"
    Then booking number of offender booking returned is "<bookingNo>"
    And gender matches "<gender>"
    And ethnicity matches "<ethnicity>"
    And height in feet matches "<ft>"
    And height in inches matches "<in>"
    And height in centimetres matches "<cm>"
    And height in metres matches "<m>"
    And weight in pounds matches "<lb>"
    And weight in kilograms matches "<kg>"

    Examples:
      | bookingId | bookingNo | gender | ethnicity                      | ft | in | cm  | m    | lb  | kg  |
      | -1        | A00111    | Male   | White: British                 | 5  | 6  | 168 | 1.68 | 165 | 75  |
      | -2        | A00112    | Female | White: Irish                   |    |    |     |      | 120 | 55  |
      | -3        | A00113    | Male   | White: British                 | 5  | 10 | 178 | 1.78 |     |     |
      | -4        | A00114    | Male   | White: British                 | 6  | 1  | 185 | 1.85 | 218 | 99  |
      | -5        | A00115    | Male   | White: British                 | 6  | 0  | 183 | 1.83 | 190 | 86  |
      | -6        | A00116    | Male   | White: British                 | 6  | 2  | 188 | 1.88 |     |     |
      | -7        | A00117    | Male   | White: British                 | 5  | 11 | 180 | 1.80 | 196 | 89  |
      | -8        | A00118    | Male   | White: British                 | 5  | 11 | 180 | 1.80 |     |     |
      | -9        | A00119    | Male   | Mixed: White and Black African | 5  | 10 | 178 | 1.78 | 185 | 84  |
      | -10       | A00120    | Male   | White: British                 | 6  | 6  | 198 | 1.98 | 235 | 107 |

  Scenario Outline: Request for specific offender booking record - physical characteristics
    When an offender booking request is made with booking id "<bookingId>"
    Then booking number of offender booking returned is "<bookingNo>"
    And characteristics match "<characteristicsList>"

    Examples:
      | bookingId | bookingNo | characteristicsList                      |
      | -1        | A00111    | Right Eye Colour=Blue,Shape of Face=Oval |
      | -2        | A00112    | Shape of Face=Round                      |
      | -3        | A00113    | Shoe Size=8                              |
      | -4        | A00114    | Shoe Size=10                             |
      | -5        | A00115    |                                          |
      | -7        | A00117    | Left Eye Colour=Hazel                    |
      | -10       | A00120    | Complexion=Fair                          |
      | -11       | A00121    | Hair Colour=Brunette,Complexion=Blotched |
      | -12       | A00122    | Hair Colour=Bald                         |

  Scenario: Request for specific offender as global search user can return data even though booking is a different caseload
    When a user has a token name of "GLOBAL_SEARCH"
    When an offender booking request is made with booking id "-16"
    Then booking number of offender booking returned is "A00126"

  Scenario Outline: Request for specific offender booking record returns language
    When an offender booking request is made with booking id "<bookingId>"
    Then language of offender booking returned is "<language>"

    Examples:
      | bookingId | language |
      | -1        | Polish   |
      | -2        | Polish   |
      | -3        | Turkish  |
      | -4        |          |
      | -7        |          |

  Scenario Outline: When requesting offender details a count of active and inactive alerts are returned
    When an offender booking request is made with booking id "<bookingId>"
    Then the number of active alerts is <activeAlerts>
    And the number of inactive alerts is <inactiveAlerts>
    And the list of active alert types is "<alertTypes>"

    Examples:
      | bookingId | activeAlerts | inactiveAlerts | alertTypes |
      | -1        | 3            | 1              | H,X        |
      | -2        | 2            | 0              | H          |
      | -11       | 0            | 0              |            |
