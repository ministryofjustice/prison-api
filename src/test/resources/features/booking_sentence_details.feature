Feature: Booking Sentence Details

  Acceptence Criteria
  A logged on staff user can retrieve sentence details for an offender booking.
  The earliest sentence start date is used when an offender booking has multiple imprisonment sentence terms.
  The latest sentence date calculation record is used when an offender booking has multiple calculation records.
  From the latest sentence date calculation record:
    - if present, the sentence expiry date is the overridden SED value, otherwise it is the calculated SED value.
    - if present, the late term date is the overridden LTD value, otherwise it is the calculated LTD value.
    - if present, the mid term date is the overridden MTD value, otherwise it is the calculated MTD value.
    - if present, the early term date is the overridden ETD value, otherwise it is the calculated ETD value.
    - if present, the automatic release date is the overridden ARD value, otherwise it is the calculated ARD value.
    - if present, the conditional release date is the overridden CRD value, otherwise it is the calculated CRD value.
    - if present, the non-parole date is the overridden NPD value, otherwise it is the calculated NPD value.
    - if present, the post-recall release date is the overridden PRRD value, otherwise it is the calculated PRRD value.
    - if present, the home detention curfew eligibility date is the overridden HDCED value, otherwise it is the calculated HDCED value.
    - if present, the parole eligibility date is the overridden PED value, otherwise it is the calculated PED value.
    - if present, the home detention curfew actual date is the overridden HDCAD value (the calculated HDCAD value is not used).
    - if present, the actual parole date is the overridden APD value (the calculated APD value is not used).
    - if present, the licence expiry date is the overridden LED value, otherwise it is the calculated LED value.
    - if present, the tariff date is the overriden tariff date value, otherwise it is the calculated tariff date value.
    - for NOMIS only, the release on temporary licence date value is the override ROTL value (there is no calculated ROTL value).
    - for NOMIS only, the early removal scheme eligibility date value is the override ERSED value (there is no calculated ERSED value).
    - for NOMIS only, the topup supervision expiry date value is the override TUSED value, otherwise it is the calculated TUSED value.
    - for NOMIS only, the tariff early removal scheme eligibility date is the override TERSED value (there is no calculated TERSED value).
    - the detention training order post recall date is the overriden DPRRD value of present, otherwise the calculated one.
    - the release date for a non-DTO sentence type is derived from one or more of ARD, CRD, NPD and/or PRRD as follows:
      - if more than one ARD, CRD, NPD and/or PRRD value is present (calculated or overridden), the latest date is the release date
  Additional days awarded is the sum of all active sentence adjustment records for an offender booking with the 'ADA' adjustment type.

  Background:
    Given a user has authenticated with the API

  Scenario: Sentence details are requested for booking that does not exist
    When sentence details are requested for an offender with booking id "-99" and version "1.0"
    Then resource not found response is received from sentence details API

  Scenario: Sentence details are requested for booking that is not part of any of logged on staff user's caseloads
    When sentence details are requested for an offender with booking id "-16" and version "1.0"
    Then resource not found response is received from sentence details API

  Scenario: Sentence details are requested for booking that is inactive
    When sentence details are requested for an offender with booking id "-20" and version "1.0"
    Then resource not found response is received from sentence details API

  Scenario Outline: Sentence details are requested for booking that is inactive
    When a user has a token name of "INACTIVE_BOOKING_USER"
    And sentence details are requested for an offender with booking id "<bookingId>" and version "<version>"
    Then sentence start date matches "<ssd>"
    And automatic release date matches "<ard>"
    And override automatic release date matches "<ardOverride>"
    And conditional release date matches "<crd>"
    And override conditional release date matches "<crdOverride>"
    And non-parole date matches "<npd>"
    And override non-parole date matches "<npdOverride>"
    And post-recall release date matches "<prrd>"
    And override post-recall release date matches "<prrdOverride>"
    And non-DTO release date matches "<nonDtoReleaseDate>"
    And non-DTO release date type matches "<nonDtoReleaseDateType>"

  Examples:
    | bookingId | version | ssd        | ard        | ardOverride | crd        | crdOverride | npd        | npdOverride | prrd       | prrdOverride | nonDtoReleaseDate | nonDtoReleaseDateType |
    | -20       | 1.0     | 2017-03-25 |            |             | 2019-03-24 |             |            |             |            |              | 2019-03-24        | CRD                   |
    | -20       | 1.1     | 2017-03-25 |            |             | 2019-03-24 |             |            |             |            |              | 2019-03-24        | CRD                   |

  Scenario Outline: Sentence details are requested for booking that is inactive
    When a user has a token name of "INACTIVE_BOOKING_USER"
    And sentence details are requested for an offender with booking id "<bookingId>" and version "<version>"
    Then sentence start date matches "<ssd>"
    And automatic release date matches "<ard>"
    And override automatic release date matches "<ardOverride>"
    And conditional release date matches "<crd>"
    And override conditional release date matches "<crdOverride>"
    And non-parole date matches "<npd>"
    And override non-parole date matches "<npdOverride>"
    And post-recall release date matches "<prrd>"
    And override post-recall release date matches "<prrdOverride>"
    And non-DTO release date matches "<nonDtoReleaseDate>"
    And non-DTO release date type matches "<nonDtoReleaseDateType>"

    Examples:
      | bookingId | version | ssd        | ard        | ardOverride | crd        | crdOverride | npd        | npdOverride | prrd       | prrdOverride | nonDtoReleaseDate | nonDtoReleaseDateType |
      | -20       | 1.0     | 2017-03-25 |            |             | 2019-03-24 |             |            |             |            |              | 2019-03-24        | CRD                   |
      | -20       | 1.1     | 2017-03-25 |            |             | 2019-03-24 |             |            |             |            |              | 2019-03-24        | CRD                   |

  Scenario Outline: Retrieve sentence details for an offender, check non-DTO sentence details only
    When sentence details are requested for an offender with booking id "<bookingId>" and version "<version>"
    Then sentence start date matches "<ssd>"
    And automatic release date matches "<ard>"
    And override automatic release date matches "<ardOverride>"
    And conditional release date matches "<crd>"
    And override conditional release date matches "<crdOverride>"
    And non-parole date matches "<npd>"
    And override non-parole date matches "<npdOverride>"
    And post-recall release date matches "<prrd>"
    And override post-recall release date matches "<prrdOverride>"
    And non-DTO release date matches "<nonDtoReleaseDate>"
    And non-DTO release date type matches "<nonDtoReleaseDateType>"

    Examples:
      | bookingId | version | ssd        | ard        | ardOverride | crd        | crdOverride | npd        | npdOverride | prrd       | prrdOverride | nonDtoReleaseDate | nonDtoReleaseDateType |
      | -1        | 1.0     | 2017-03-25 |            |             | 2019-03-24 |             |            |             |            |              | 2019-03-24        | CRD                   |
      | -2        | 1.0     | 2017-05-22 | 2018-05-21 | 2018-04-21  |            |             |            |             |            |              | 2018-04-21        | ARD                   |
      | -3        | 1.0     | 2015-03-16 |            |             |            |             |            |             |            |              |                   |                       |
      | -4        | 1.0     | 2007-10-16 | 2021-05-06 |             |            |             |            |             | 2021-08-29 | 2021-08-31   | 2021-08-31        | PRRD                  |
      | -5        | 1.0     | 2017-02-08 |            |             | 2023-02-07 |             | 2022-02-15 | 2022-02-02  |            |              | 2023-02-07        | CRD                   |
      | -6        | 1.0     | 2017-09-01 | 2018-02-28 |             | 2018-01-31 |             |            |             |            |              | 2018-02-28        | ARD                   |
      | -7        | 1.0     | 2017-09-01 | 2018-02-28 |             |            |             | 2017-12-31 |             |            |              | 2018-02-28        | ARD                   |
      | -8        | 1.0     | 2017-09-01 | 2018-02-28 |             |            |             |            |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -9        | 1.0     | 2017-09-01 |            |             | 2018-01-31 |             | 2017-12-31 |             |            |              | 2018-01-31        | CRD                   |
      | -10       | 1.0     | 2017-09-01 |            |             | 2018-01-31 |             |            |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -11       | 1.0     | 2017-09-01 |            |             |            |             | 2017-12-31 |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -12       | 1.0     | 2017-09-01 |            |             |            |             |            |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -17       | 1.0     | 2015-05-05 |            |             |            |             |            |             |            |              |                   |                       |
      | -18       | 1.0     | 2016-11-17 |            |             |            |             |            |             |            |              |                   |                       |
      | -24       | 1.0     | 2017-07-07 |            |             |            |             |            |             |            |              |                   |                       |
      | -25       | 1.0     | 2009-09-09 |            |             |            |             |            |             |            |              |                   |                       |
      | -29       | 1.0     | 2017-02-08 |            |             |            |             | 2017-12-31 |             |            |              | 2017-12-31        | NPD                   |
      | -30       | 1.0     | 2007-10-16 |            |             |            |             |            |             |            |              |                   |                       |
      | -32       | 1.0     |            |            |             |            |             |            |             |            |              |                   |                       |
      | -1        | 1.1     | 2017-03-25 |            |             | 2019-03-24 |             |            |             |            |              | 2019-03-24        | CRD                   |
      | -2        | 1.1     | 2017-05-22 | 2018-05-21 | 2018-04-21  |            |             |            |             |            |              | 2018-04-21        | ARD                   |
      | -3        | 1.1     | 2015-03-16 |            |             |            |             |            |             |            |              |                   |                       |
      | -4        | 1.1     | 2007-10-16 | 2021-05-06 |             |            |             |            |             | 2021-08-29 | 2021-08-31   | 2021-08-31        | PRRD                  |
      | -5        | 1.1     | 2017-02-08 |            |             | 2023-02-07 |             | 2022-02-15 | 2022-02-02  |            |              | 2023-02-07        | CRD                   |
      | -6        | 1.1     | 2017-09-01 | 2018-02-28 |             | 2018-01-31 |             |            |             |            |              | 2018-02-28        | ARD                   |
      | -7        | 1.1     | 2017-09-01 | 2018-02-28 |             |            |             | 2017-12-31 |             |            |              | 2018-02-28        | ARD                   |
      | -8        | 1.1     | 2017-09-01 | 2018-02-28 |             |            |             |            |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -9        | 1.1     | 2017-09-01 |            |             | 2018-01-31 |             | 2017-12-31 |             |            |              | 2018-01-31        | CRD                   |
      | -10       | 1.1     | 2017-09-01 |            |             | 2018-01-31 |             |            |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -11       | 1.1     | 2017-09-01 |            |             |            |             | 2017-12-31 |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -12       | 1.1     | 2017-09-01 |            |             |            |             |            |             | 2018-03-31 |              | 2018-03-31        | PRRD                  |
      | -17       | 1.1     | 2015-05-05 |            |             |            |             |            |             |            |              |                   |                       |
      | -18       | 1.1     | 2016-11-17 |            |             |            |             |            |             |            |              |                   |                       |
      | -24       | 1.1     | 2017-07-07 |            |             |            |             |            |             |            |              |                   |                       |
      | -25       | 1.1     | 2009-09-09 |            |             |            |             |            |             |            |              |                   |                       |
      | -29       | 1.1     | 2017-02-08 |            |             |            |             | 2017-12-31 |             |            |              | 2017-12-31        | NPD                   |
      | -30       | 1.1     | 2007-10-16 |            |             |            |             |            |             |            |              |                   |                       |
      | -32       | 1.1     |            |            |             |            |             |            |             |            |              |                   |                       |

  Scenario Outline: Retrieve sentence details for an offender, check DTO sentence details
    When sentence details are requested for an offender with booking id "<bookingId>" and version "<version>"
    Then sentence start date matches "<ssd>"
    And sentence expiry date matches "<sed>"
    And additional days awarded matches "<ada>"
    And early term date matches "<etd>"
    And mid term date matches "<mtd>"
    And late term date matches "<ltd>"

    Examples:
      | bookingId | version |  ssd       | sed        | ada | etd        | mtd        | ltd        |
      | -1        | 1.0     | 2017-03-25 | 2020-03-24 | 12  |            |            |            |
      | -2        | 1.0     | 2017-05-22 | 2019-05-21 |     |            |            |            |
      | -3        | 1.0     | 2015-03-16 | 2020-03-15 |     | 2017-09-15 | 2018-03-15 | 2018-09-15 |
      | -4        | 1.0     | 2007-10-16 | 2022-10-20 | 5   |            |            |            |
      | -5        | 1.0     | 2017-02-08 | 2023-08-07 | 14  | 2023-02-07 | 2023-05-07 | 2023-08-07 |
      | -6        | 1.0     | 2017-09-01 | 2018-05-31 | 17  |            |            |            |
      | -7        | 1.0     | 2017-09-01 | 2018-05-31 |     | 2017-11-30 | 2017-12-31 | 2018-01-31 |
      | -8        | 1.0     | 2017-09-01 | 2018-05-31 |     | 2017-11-30 | 2017-12-31 | 2018-01-31 |
      | -9        | 1.0     | 2017-09-01 | 2018-05-31 |     | 2017-11-30 | 2017-12-31 | 2018-01-31 |
      | -10       | 1.0     | 2017-09-01 | 2018-05-31 |     |            |            |            |
      | -11       | 1.0     | 2017-09-01 | 2018-05-31 |     |            |            |            |
      | -12       | 1.0     | 2017-09-01 | 2018-05-31 |     |            |            |            |
      | -17       | 1.0     | 2015-05-05 |            |     |            |            |            |
      | -18       | 1.0     | 2016-11-17 |            |     |            |            |            |
      | -24       | 1.0     | 2017-07-07 |            |     |            |            |            |
      | -25       | 1.0     | 2009-09-09 |            |     | 2023-09-08 | 2024-09-08 | 2025-09-08 |
      | -29       | 1.0     | 2017-02-08 | 2023-08-07 |     |            |            |            |
      | -30       | 1.0     | 2007-10-16 | 2022-10-20 |     | 2021-02-28 | 2021-03-25 | 2021-04-28 |
      | -32       | 1.0     |            |            |     |            |            |            |
      | -1        | 1.1     | 2017-03-25 | 2020-03-24 | 12  |            |            |            |
      | -2        | 1.1     | 2017-05-22 | 2019-05-21 |     |            |            |            |
      | -3        | 1.1     | 2015-03-16 | 2020-03-15 |     | 2017-09-15 | 2018-03-15 | 2018-09-15 |
      | -4        | 1.1     | 2007-10-16 | 2022-10-20 | 5   |            |            |            |
      | -5        | 1.1     | 2017-02-08 | 2023-08-07 | 14  | 2023-02-07 | 2023-05-07 | 2023-08-07 |
      | -6        | 1.1     | 2017-09-01 | 2018-05-31 | 17  |            |            |            |
      | -7        | 1.1     | 2017-09-01 | 2018-05-31 |     | 2017-11-30 | 2017-12-31 | 2018-01-31 |
      | -8        | 1.1     | 2017-09-01 | 2018-05-31 |     | 2017-11-30 | 2017-12-31 | 2018-01-31 |
      | -9        | 1.1     | 2017-09-01 | 2018-05-31 |     | 2017-11-30 | 2017-12-31 | 2018-01-31 |
      | -10       | 1.1     | 2017-09-01 | 2018-05-31 |     |            |            |            |
      | -11       | 1.1     | 2017-09-01 | 2018-05-31 |     |            |            |            |
      | -12       | 1.1     | 2017-09-01 | 2018-05-31 |     |            |            |            |
      | -17       | 1.1     | 2015-05-05 |            |     |            |            |            |
      | -18       | 1.1     | 2016-11-17 |            |     |            |            |            |
      | -24       | 1.1     | 2017-07-07 |            |     |            |            |            |
      | -25       | 1.1     | 2009-09-09 |            |     | 2023-09-08 | 2024-09-08 | 2025-09-08 |
      | -29       | 1.1     | 2017-02-08 | 2023-08-07 |     |            |            |            |
      | -30       | 1.1     | 2007-10-16 | 2022-10-20 |     | 2021-02-28 | 2021-03-25 | 2021-04-28 |
      | -32       | 1.1     |            |            |     |            |            |            |

@wip
  Scenario Outline: Retrieve sentence details for an offender, check other dates
    When sentence details are requested for an offender with booking id "<bookingId>" and version "<version>"
    Then sentence start date matches "<ssd>"
    And home detention curfew eligibility date matches "<hdced>"
    And parole eligibility date matches "<ped>"
    And licence expiry date matches "<led>"
    And home detention curfew actual date matches "<hdcad>"
    And actual parole date matches "<apd>"
    And confirmed release date matches "<confRelDate>"
    And release date matches "<releaseDate>"
    And tariff date matches "<tariffDate>"
    And detention training order post-recall release date matches "<dtoPostRecall>"
    And effective sentence end date matches "<effectiveEndDate>"

    Examples:
      | bookingId | version | ssd        | hdced      | ped        | led        | hdcad      | apd        | confRelDate | releaseDate | tariffDate | dtoPostRecall | effectiveEndDate |
      | -1        | 1.0     | 2017-03-25 |            |            |            |            | 2018-09-27 | 2018-04-23  | 2018-04-23  |            | 2020-03-22    | 2025-05-05       |
      | -2        | 1.0     | 2017-05-22 |            |            |            |            |            | 2018-04-19  | 2018-04-19  |            |               |                  |
      | -3        | 1.0     | 2015-03-16 |            |            |            |            |            |             | 2018-03-15  |            |               |                  |
      | -4        | 1.0     | 2007-10-16 |            |            |            |            |            |             | 2021-08-31  |            |               |                  |
      | -5        | 1.0     | 2017-02-08 | 2019-06-02 | 2019-06-01 |            |            |            |             | 2023-05-07  |            |               |                  |
      | -6        | 1.0     | 2017-09-01 |            |            |            | 2018-05-15 |            |             | 2018-05-15  |            |               |                  |
      | -7        | 1.0     | 2017-09-01 |            |            |            |            |            | 2018-01-05  | 2018-01-05  |            |               |                  |
      | -8        | 1.0     | 2017-09-01 |            |            |            |            | 2017-12-23 |             | 2017-12-23  |            |               |                  |
      | -9        | 1.0     | 2017-09-01 |            |            |            | 2018-01-15 |            | 2018-01-13  | 2018-01-13  |            |               |                  |
      | -10       | 1.0     | 2017-09-01 |            |            |            |            | 2018-02-22 |             | 2018-02-22  |            |               |                  |
      | -11       | 1.0     | 2017-09-01 |            |            |            |            |            |             | 2018-03-31  |            |               |                  |
      | -12       | 1.0     | 2017-09-01 |            |            |            |            |            |             | 2018-03-31  |            |               |                  |
      | -17       | 1.0     | 2015-05-05 |            |            |            |            |            | 2018-01-16  | 2018-01-16  |            |               |                  |
      | -18       | 1.0     | 2016-11-17 |            |            |            | 2019-09-19 |            |             | 2019-09-19  |            |               |                  |
      | -24       | 1.0     | 2017-07-07 |            |            |            |            | 2022-06-06 | 2022-02-02  | 2022-02-02  |            |               |                  |
      | -25       | 1.0     | 2009-09-09 |            |            |            |            | 2019-09-08 | 2023-03-03  | 2023-03-03  |            |               |                  |
      | -27       | 1.0     | 2014-09-09 |            |            |            |            |            |             |             | 2029-09-08 |               |                  |
      | -28       | 1.0     | 2014-09-09 |            |            |            |            |            |             |             | 2031-03-08 |               |                  |
      | -29       | 1.0     | 2017-02-08 |            | 2021-05-05 | 2020-08-07 |            |            |             | 2017-12-31  |            |               |                  |
      | -30       | 1.0     | 2007-10-16 | 2020-12-30 |            | 2021-09-24 |            | 2021-01-02 |             | 2021-01-02  |            |               |                  |
      | -32       | 1.0     |            |            |            |            |            |            |             |             |            |               |                  |
      | -1        | 1.1     | 2017-03-25 |            |            |            |            | 2018-09-27 | 2018-04-23  | 2018-04-23  |            | 2020-03-22    | 2025-05-05       |
      | -2        | 1.1     | 2017-05-22 |            |            |            |            |            | 2018-04-19  | 2018-04-19  |            |               |                  |
      | -3        | 1.1     | 2015-03-16 |            |            |            |            |            |             | 2018-03-15  |            |               |                  |
      | -4        | 1.1     | 2007-10-16 |            |            |            |            |            |             | 2021-08-31  |            |               |                  |
      | -5        | 1.1     | 2017-02-08 | 2019-06-02 | 2019-06-01 |            |            |            |             | 2023-05-07  |            |               |                  |
      | -6        | 1.1     | 2017-09-01 |            |            |            | 2018-05-15 |            |             | 2018-05-15  |            |               |                  |
      | -7        | 1.1     | 2017-09-01 |            |            |            |            |            | 2018-01-05  | 2018-01-05  |            |               |                  |
      | -8        | 1.1     | 2017-09-01 |            |            |            |            | 2017-12-23 |             | 2017-12-23  |            |               |                  |
      | -9        | 1.1     | 2017-09-01 |            |            |            | 2018-01-15 |            | 2018-01-13  | 2018-01-13  |            |               |                  |
      | -10       | 1.1     | 2017-09-01 |            |            |            |            | 2018-02-22 |             | 2018-02-22  |            |               |                  |
      | -11       | 1.1     | 2017-09-01 |            |            |            |            |            |             | 2018-03-31  |            |               |                  |
      | -12       | 1.1     | 2017-09-01 |            |            |            |            |            |             | 2018-03-31  |            |               |                  |
      | -17       | 1.1     | 2015-05-05 |            |            |            |            |            | 2018-01-16  | 2018-01-16  |            |               |                  |
      | -18       | 1.1     | 2016-11-17 |            |            |            | 2019-09-19 |            |             | 2019-09-19  |            |               |                  |
      | -24       | 1.1     | 2017-07-07 |            |            |            |            | 2022-06-06 | 2022-02-02  | 2022-02-02  |            |               |                  |
      | -25       | 1.1     | 2009-09-09 |            |            |            |            | 2019-09-08 | 2023-03-03  | 2023-03-03  |            |               |                  |
      | -27       | 1.1     | 2014-09-09 |            |            |            |            |            |             |             | 2029-09-08 |               |                  |
      | -28       | 1.1     | 2014-09-09 |            |            |            |            |            |             |             | 2031-03-08 |               |                  |
      | -29       | 1.1     | 2017-02-08 |            | 2021-05-05 | 2020-08-07 |            |            |             | 2017-12-31  |            |               |                  |
      | -30       | 1.1     | 2007-10-16 | 2020-12-30 |            | 2021-09-24 |            | 2021-01-02 |             | 2021-01-02  |            |               |                  |
      | -32       | 1.1     |            |            |            |            |            |            |             |             |            |               |                  |

  Scenario Outline: Retrieve sentence details for an offender, check other dates - NOMIS only - for ROTL, ERSED, TUSED and TERSED
    When sentence details are requested for an offender with booking id "<bookingId>" and version "<version>"
    Then sentence start date matches "<ssd>"
    And release on temporary licence date matches "<rotl>"
    And early removal scheme eligibility date matches "<ersed>"
    And topup supervision expiry date matches "<tused>"
    And tariff early removal scheme eligibility date matches "<tersed>"

    Examples:
      | bookingId | version | ssd        | rotl       | ersed      | tused      | tersed     |
      | -1        | 1.0     | 2017-03-25 |            |            |            | 2020-06-25 |
      | -2        | 1.0     | 2017-05-22 | 2018-02-25 |            |            |            |
      | -3        | 1.0     | 2015-03-16 |            |            |            |            |
      | -4        | 1.0     | 2007-10-16 |            | 2019-09-01 |            |            |
      | -5        | 1.0     | 2017-02-08 |            |            |            |            |
      | -6        | 1.0     | 2017-09-01 |            |            | 2021-03-30 |            |
      | -7        | 1.0     | 2017-09-01 |            |            | 2021-03-31 |            |
      | -8        | 1.0     | 2017-09-01 |            |            |            |            |
      | -9        | 1.0     | 2017-09-01 |            |            |            |            |
      | -10       | 1.0     | 2017-09-01 |            |            |            |            |
      | -11       | 1.0     | 2017-09-01 |            |            |            |            |
      | -12       | 1.0     | 2017-09-01 |            |            |            |            |
      | -17       | 1.0     | 2015-05-05 |            |            |            |            |
      | -18       | 1.0     | 2016-11-17 |            |            |            |            |
      | -24       | 1.0     | 2017-07-07 |            |            |            |            |
      | -25       | 1.0     | 2009-09-09 |            |            |            |            |
      | -29       | 1.0     | 2017-02-08 |            |            |            |            |
      | -30       | 1.0     | 2007-10-16 |            |            |            |            |
      | -32       | 1.0     |            |            |            |            |            |
      | -1        | 1.1     | 2017-03-25 |            |            |            | 2020-06-25 |
      | -2        | 1.1     | 2017-05-22 | 2018-02-25 |            |            |            |
      | -3        | 1.1     | 2015-03-16 |            |            |            |            |
      | -4        | 1.1     | 2007-10-16 |            | 2019-09-01 |            |            |
      | -5        | 1.1     | 2017-02-08 |            |            |            |            |
      | -6        | 1.1     | 2017-09-01 |            |            | 2021-03-30 |            |
      | -7        | 1.1     | 2017-09-01 |            |            | 2021-03-31 |            |
      | -8        | 1.1     | 2017-09-01 |            |            |            |            |
      | -9        | 1.1     | 2017-09-01 |            |            |            |            |
      | -10       | 1.1     | 2017-09-01 |            |            |            |            |
      | -11       | 1.1     | 2017-09-01 |            |            |            |            |
      | -12       | 1.1     | 2017-09-01 |            |            |            |            |
      | -17       | 1.1     | 2015-05-05 |            |            |            |            |
      | -18       | 1.1     | 2016-11-17 |            |            |            |            |
      | -24       | 1.1     | 2017-07-07 |            |            |            |            |
      | -25       | 1.1     | 2009-09-09 |            |            |            |            |
      | -29       | 1.1     | 2017-02-08 |            |            |            |            |
      | -30       | 1.1     | 2007-10-16 |            |            |            |            |
      | -32       | 1.1     |            |            |            |            |            |

  Scenario Outline: Retrieve sentence details as a list and filter by booking id and check data matches
    When sentence details are requested for an offenders in logged in users caseloads with offender No "<offenderNo>"
    Then sentence start date matches "<ssd>"
    And home detention curfew eligibility date matches "<hdced>"
    And parole eligibility date matches "<ped>"
    And licence expiry date matches "<led>"
    And home detention curfew actual date matches "<hdcad>"
    And actual parole date matches "<apd>"
    And confirmed release date matches "<confRelDate>"
    And release date matches "<releaseDate>"
    And tariff date matches "<tariffDate>"
    And tariff early removal scheme eligibility date matches "<tersed>"
    And effective sentence end date matches "<effectiveEndDate>"
    And detention training order post-recall release date matches "<dtoPostRecall>"

    Examples:
      | offenderNo| ssd        | hdced      | ped        | led        | hdcad      | apd        | confRelDate | releaseDate | tariffDate | tersed      | effectiveEndDate | dtoPostRecall |
      | A1234AA   | 2017-03-25 |            |            |            |            | 2018-09-27 | 2018-04-23  | 2018-04-23  |            | 2020-06-25  | 2025-05-05       | 2020-03-22    |
      | A1234AB   | 2017-05-22 |            |            |            |            |            | 2018-04-19  | 2018-04-19  |            |             |                  |               |
      | A1234AC   | 2015-03-16 |            |            |            |            |            |             | 2018-03-15  |            |             |                  |               |
      | A1234AD   | 2007-10-16 |            |            |            |            |            |             | 2021-08-31  |            |             |                  |               |
      | A1234AE   | 2017-02-08 | 2019-06-02 | 2019-06-01 |            |            |            |             | 2023-05-07  |            |             |                  |               |
      | A1234AF   | 2017-09-01 |            |            |            | 2018-05-15 |            |             | 2018-05-15  |            |             |                  |               |
      | A1234AG   | 2017-09-01 |            |            |            |            |            | 2018-01-05  | 2018-01-05  |            |             |                  |               |
      | A1234AH   | 2017-09-01 |            |            |            |            | 2017-12-23 |             | 2017-12-23  |            |             |                  |               |
      | A1234AI   | 2017-09-01 |            |            |            | 2018-01-15 |            | 2018-01-13  | 2018-01-13  |            |             |                  |               |
      | A1234AJ   | 2017-09-01 |            |            |            |            | 2018-02-22 |             | 2018-02-22  |            |             |                  |               |
      | A1234AK   | 2017-09-01 |            |            |            |            |            |             | 2018-03-31  |            |             |                  |               |
      | A1234AL   | 2017-09-01 |            |            |            |            |            |             | 2018-03-31  |            |             |                  |               |

  Scenario: Retrieve sentence details as a list
    When sentence details are requested of offenders for the logged in users caseloads
    Then "27" offenders are returned

  Scenario: Retrieve sentence details for an agency
    When sentence details are requested of offenders for agency "MDI"
    Then some offenders are returned

  Scenario: Retrieve sentence details as a list using post request for multiple offender Nos
    When sentence details are requested by a POST request for offender Nos "A1234AK,A1234AE,A1234AJ,A1234AC"
    Then "4" offenders are returned

  Scenario: Retrieve sentence details as a list using post request for empty list of offender Nos
    When sentence details are requested by a POST request for offender Nos ""
    Then bad request response is received from booking sentence API

  Scenario: Retrieve sentence details as a list using post request for multiple booking ids
      Note -11 = A1234AK; -5 = A1234AE, -16 not in caseload
    When sentence details are requested by a POST request for booking ids "-11,-5,-16"
    Then "2" offenders are returned

  Scenario: Retrieve sentence details across caseloads as system user
    Given a system client "batchadmin" has authenticated with the API
    When sentence details are requested by a POST request for booking ids "-1,-16,-36"
    Then "3" offenders are returned

  Scenario Outline: Retrieve sentence details multiple offender Nos
    When sentence details are requested for an offenders in logged in users caseloads with offender No "A1234AK,A1234AE,A1234AJ,A1234AC"
    Then "4" offenders are returned
    When I look at row "<row_num>"
    And sentence start date matches "<ssd>"
    And home detention curfew eligibility date matches "<hdced>"
    And release date matches "<releaseDate>"

    Examples:
      | row_num | ssd        | hdced      | releaseDate |
      | 1       | 2017-09-01 |            | 2018-02-22  |
      | 2       | 2015-03-16 |            | 2018-03-15  |
      | 3       | 2017-09-01 |            | 2018-03-31  |
      | 4       | 2017-02-08 | 2019-06-02 | 2023-05-07  |

  Scenario Outline: Retrieve sentence details with sorting and with sentence date set
    When sentence details are requested of offenders for the logged in users caseloads
    Then "27" offenders are returned
    When I look at row "<row_num>"
    And sentence start date matches "<ssd>"
    And home detention curfew eligibility date matches "<hdced>"
    And confirmed release date matches "<confRelDate>"
    And release date matches "<releaseDate>"

    Examples:
      | row_num | ssd        | hdced      | confRelDate | releaseDate |
      | 1       | 2017-09-01 |            |             | 2017-12-23  |
      | 2       | 2017-02-08 |            |             | 2017-12-31  |
      | 3       | 2017-09-01 |            | 2018-01-05  | 2018-01-05  |
      | 4       | 2017-09-01 |            | 2018-01-13  | 2018-01-13  |
      | 5       | 2015-05-05 |            | 2018-01-16  | 2018-01-16  |
      | 6       | 2017-09-01 |            |             | 2018-02-22  |
      | 7       | 2015-03-16 |            |             | 2018-03-15  |
      | 8       | 2017-09-01 |            |             | 2018-03-31  |
      | 9       | 2017-09-01 |            |             | 2018-03-31  |
      | 10      | 2017-05-22 |            | 2018-04-19  | 2018-04-19  |
      | 11      | 2017-03-25 |            | 2018-04-23  | 2018-04-23  |
      | 12      | 2017-09-01 |            |             | 2018-05-15  |
      | 13      | 2016-11-17 |            |             | 2019-09-19  |
      | 14      | 2007-10-16 | 2020-12-30 |             | 2021-01-02  |
      | 15      | 2007-10-16 |            |             | 2021-08-31  |
      | 16      | 2017-07-07 |            | 2022-02-02  | 2022-02-02  |
      | 17      | 2009-09-09 |            | 2023-03-03  | 2023-03-03  |
      | 18      | 2017-02-08 | 2019-06-02 |             | 2023-05-07  |
      | 19      | 2014-09-09 |            |             |             |
      | 20      | 2014-09-09 |            |             |             |

  Scenario: Retrieve sentence details for offenders who are candidates for Home Detention Curfew.
    When sentence details are requested for offenders who are candidates for Home Detention Curfew
    Then some offender sentence details are returned
