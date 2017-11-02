@global
Feature: Booking Details

  Acceptance Criteria:
  A logged in staff user can retrieve correct booking details for a provided offender booking id.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Request for specific offender booking record (assigned officer)
    When an offender booking request is made with booking id "<bookingId>"
    Then booking number of offender booking returned is "<bookingNo>"
    And assigned officer id of offender booking returned is "<assignedOfficerId>"

    Examples:
      | bookingId | bookingNo | assignedOfficerId |
      | -1        | A00111    | -2                |
      | -8        | A00118    | -2                |

  Scenario Outline: Request for specific offender booking record (physical attributes)
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

  Scenario Outline: Request for specific offender booking record (physical characteristics)
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
      | -13       | A00123    | Hair Colour=Ginger                       |
      | -14       | A00124    | Hair Colour=Dyed,Build=Slight            |
      | -15       | A00125    | Facial Hair=Sideburns,Build=Heavy        |

  Scenario: Request for specific offender booking record that does not exist
    When an offender booking request is made with booking id "-9999"
    Then resource not found response is received from bookings API

  Scenario Outline: Request for assessment information about an offender
    When an offender booking request is made with booking id <bookingId> and "<assessmentCode>"
    Then the classification is "<classification>"
    And the Cell Sharing Alert is <CSRA>

    Examples:
      | bookingId | assessmentCode | CSRA  | classification |
      | -1        | CSR            | true  | High           |
      | -2        | CSR            | true  |                |
      | -3        | CSR            | true  | Low            |
      | -4        | CSR            | true  | Medium         |
      | -5        | CSR            | true  | High           |
      | -6        | CATEGORY       | false | Cat C          |
      | -6        | CSR            | true  | Standard       |
      | -6        | PAROLE         | false | High           |

  Scenario: Request for specific offender booking record that does not exist
    When an offender booking request is made with booking id -16 and "CSR"
    Then resource not found response is received from bookings API