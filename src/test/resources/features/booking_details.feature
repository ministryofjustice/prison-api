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

  Scenario: Request for specific offender booking record that does not exist
    When an offender booking request is made with booking id "-9999"
    Then resource not found response is received from bookings API

  Scenario: Request for specific offender inactive booking record
    When a basic offender booking request is made with booking id "-13"
    Then resource not found response is received from bookings API

  Scenario: Request for specific offender as global search user can return data even though booking is a different caseload
    When a user has a token name of "GLOBAL_SEARCH"
    When an offender booking request is made with booking id "-16"
    Then booking number of offender booking returned is "A00126"

  Scenario Outline: Request for assessment information about an offender
    When an offender booking assessment information request is made with booking id <bookingId> and "<assessmentCode>"
    Then the classification is "<classification>"
    And the Cell Sharing Alert is <CSRA>
    And the Next Review Date is "<nextReviewDate>"

    Examples:
      | bookingId | assessmentCode | CSRA  | classification | nextReviewDate |
      | -1        | CSR            | true  | High           | 2018-06-01     |
      | -2        | CSR            | true  |                | 2018-06-02     |
      | -3        | CSR            | true  | Low            | 2018-06-03     |
      | -4        | CSR            | true  | Medium         | 2018-06-04     |
      | -5        | CSR            | true  | High           | 2018-06-05     |
      | -6        | CATEGORY       | false | Cat C          | 2018-06-07     |
      | -6        | CSR            | true  | Standard       | 2018-06-06     |
      | -6        | PAROLE         | false | High           | 2018-06-08     |

  Scenario: Request for assessment information for booking that does not have requested assessment
    When an offender booking assessment information request is made with booking id -9 and "CSR"
    Then resource not found response is received from booking assessments API
    And user message in resource not found response from booking assessments API is "Offender does not have a [CSR] assessment on record."

  Scenario: Request for assessment information for booking that does not exist
    When an offender booking assessment information request is made with booking id -99 and "CSR"
    Then resource not found response is received from booking assessments API

  Scenario: Request for assessment information for booking that is not part of any of logged on staff user's caseloads
    When an offender booking assessment information request is made with booking id -16 and "CSR"
    Then resource not found response is received from booking assessments API

  Scenario: Request for CSR assessment information for multiple offenders
    When an offender booking assessment information request is made with offender numbers "A1234AA,A1234AB,A1234AC,A1234AE,A1234AF,A1234AG,A1234AP,NEXIST" and "CSR" and latest="false" and active="true"
    Then correct results are returned as for single assessment

  Scenario: Request for category assessment information for multiple offenders
    When an offender booking assessment information request is made with offender numbers "A1234AE,A1234AF" and "CATEGORY" and latest="false" and active="false"
    Then full category history is returned

  Scenario: Request for CSR assessment information for multiple offenders (using post request which allows large sets of offenders)
    When an offender booking assessment information POST request is made with offender numbers "A1234AA,A1234AB,A1234AC,A1234AE,A1234AF,A1234AG,A1234AP,NEXIST" and "CSR"
    Then correct results are returned as for single assessment

  Scenario: Request for assessment information with empty list of offenders (using post request which allows large sets of offenders)
    When an offender booking assessment information POST request is made with offender numbers "" and "CSR"
    Then bad request response is received from booking assessments API with message "List of Offender Ids must be provided"

  Scenario: Request for CSRAs for multiple offenders (using post request which allows large sets of offenders)
    When an offender booking CSRA information POST request is made with offender numbers "A1234AA,A1234AB,A1234AC,A1234AE,A1234AF,A1234AG,A1234AP,NEXIST"
    Then correct results are returned as for single assessment

  Scenario: Request for offenders who need to be categorised
    When a request is made for uncategorised offenders at "MDI"
    Then some uncategorised offenders are returned

  Scenario: Request for offenders who need to be categorised with invalid agency
    When a request is made for uncategorised offenders at "XXXX"
    Then resource not found response is received from booking assessments API

  Scenario: Request for offenders who have an approved categorisation
    When a request is made for categorised offenders at "LEI" with an approval from Date of "2018-02-02"
    Then 1 categorised offenders are returned

  Scenario: Request for offenders who have an approved categorisation using default 1 month period
    When a request is made for categorised offenders at "LEI" with an approval from Date of ""
    Then 0 categorised offenders are returned

  Scenario: Request for offenders who need to be recategorised
    When a request is made for offenders who need to be recategorised at "LEI" with cutoff Date of "2018-07-01"
    Then 4 categorised offenders are returned


  Scenario: Approve categorisation validation: no auth
    When a categorisation is approved for booking "-34" with category "D" date "2019-02-28" and comment "Make it so"
    Then access denied response is received from booking assessments API

  Scenario: Approve categorisation validation: no category
    Given a user has a token name of "CATEGORISATION_APPROVE"
    When a categorisation is approved for booking "-34" with category "" date "2019-02-28" and comment "Make it so"
    Then bad request response is received from booking assessments API with message "category must be provided"

  Scenario: Approve categorisation validation: no booking
    Given a user has a token name of "CATEGORISATION_APPROVE"
    When a categorisation is approved for booking "" with category "C" date "2019-02-28" and comment "Make it so"
    Then bad request response is received from booking assessments API with message "bookingId must be provided"

  Scenario: Approve categorisation validation: invalid booking
    Given a user has a token name of "CATEGORISATION_APPROVE"
    When a categorisation is approved for booking "-999" with category "C" date "2019-02-28" and comment ""
    Then resource not found response is received from booking assessments API

  Scenario: Approve categorisation validation: no date
    Given a user has a token name of "CATEGORISATION_APPROVE"
    When a categorisation is approved for booking "-34" with category "B" date "" and comment ""
    Then bad request response is received from booking assessments API with message "Date of approval must be provided"

  Scenario: Approve categorisation validation: invalid category
    Given a user has a token name of "CATEGORISATION_APPROVE"
    When a categorisation is approved for booking "-34" with category "hmm" date "2019-02-28" and comment ""
    Then bad request response is received from booking assessments API with message "Category not recognised."

  Scenario: Approve categorisation validation: no pending category exists
    Given a user has a token name of "CATEGORISATION_APPROVE"
    When a categorisation is approved for booking "-33" with category "C" date "2019-02-28" and comment ""
    Then bad request response is received from booking assessments API with message "No category assessment found, category C, booking -33"

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

 Scenario: Request for offender identifiers
    When offender identifiers are requested for Booking Id "-4"
    Then "2" row of offender identifiers is returned

  Scenario: Request for image metadata
    When image metadata is requested for Booking Id "-1"
    Then image metadata is returned

  Scenario: Request for image data
    When image data is requested by booking Id "-1"
    Then image bytes are returned

  Scenario: Request for image data full size
    When full size image is requested by booking Id "-1"
    Then image bytes are returned

    Scenario Outline: Request offender basic details by offender numbers
      When a request is made for "A1234AE,A1234AB"
      Then data is returned that includes "<firstName>" "<lastName>" "<middleName>" "<offenderNo>" "<bookingId>" "<agencyId>"

      Examples:
      | firstName | lastName   | middleName     | offenderNo   | bookingId | agencyId |
      | Donald    | Matthews   | Jeffrey Robert | A1234AE      | -5        | LEI      |
      | Gillian   | Anderson   | Eve            | A1234AB      | -2        | LEI      |

  Scenario: A GLOBAL_SEARCH user can see offender details from any agency
    Given a trusted client with VIEW_PRISONER_DATA role has authenticated with the API
    When a request is made for "A1234AE,A1234AB,Z0017ZZ"
    Then the total records returned are "3"
