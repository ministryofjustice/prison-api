@global
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
    - if present, the parole eligibility date is the overridden PED value, otherwise it is the calculated PED value.
    - if present, the licence expiry date is the overridden LED value, otherwise it is the calculated LED value.
    - if present, the home detention curfew eligibility date is the overridden HDCED value, otherwise it is the calculated HDCED value.
    - the release date for a non-DTO sentence type is derived from one or more of ARD, CRD, NPD and/or PRRD as follows:
      - if more than one ARD, CRD, NPD and/or PRRD value is present (calculated or overridden), the latest date is the release date
  Days remaining is the number of days between the derived release date (from latest sentence date calculation record) and current system date.
  Additional days awarded is the sum of all active sentence adjustment records for an offender booking with the 'ADA' adjustment type.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve sentence details for an offender, check non-DTO sentence details only
    When sentence details are requested for an offender with booking id "<bookingId>"
    Then sentence start date matches "<ssd>"
    And sentence expiry date matches "<sed>"
    And automatic release date matches "<ard>"
    And override automatic release date matches "<ardOverride>"
    And conditional release date matches "<crd>"
    And override conditional release date matches "<crdOverride>"
    And non-parole date matches "<npd>"
    And override non-parole date matches "<npdOverride>"
    And post-recall release date matches "<prrd>"
    And override post-recall release date matches "<prrdOverride>"
    And release date matches "<releaseDate>"
    And days remaining is correct for release date of "<releaseDate>"

    Examples:
      | bookingId | ssd        | sed        | ard        | ardOverride | crd        | crdOverride | npd        | npdOverride | prrd       | prrdOverride | releaseDate |
      | -1        | 2017-03-25 | 2020-03-24 |            |             | 2019-03-24 |             |            |             |            |              | 2019-03-24  |
      | -2        | 2016-11-22 | 2019-05-21 | 2018-05-21 | 2018-04-21  |            |             |            |             |            |              | 2018-04-21  |
      | -3        | 2015-03-16 | 2020-03-15 |            |             |            |             |            |             |            |              |             |
      | -4        | 2007-10-16 | 2022-10-20 | 2021-05-06 |             |            |             |            |             | 2021-08-29 | 2021-08-31   | 2021-08-31  |
      | -5        | 2017-02-08 | 2023-08-07 |            |             | 2023-02-07 |             | 2022-02-15 | 2022-02-02  |            |              | 2023-02-07  |
      | -6        | 2017-09-01 | 2018-05-31 | 2018-02-28 |             | 2018-01-31 |             |            |             |            |              | 2018-02-28  |
      | -7        | 2017-09-01 | 2018-05-31 | 2018-02-28 |             |            |             | 2017-12-31 |             |            |              | 2018-02-28  |
      | -8        | 2017-09-01 | 2018-05-31 | 2018-02-28 |             |            |             |            |             | 2018-03-31 |              | 2018-03-31  |
      | -9        | 2017-09-01 | 2018-05-31 |            |             | 2018-01-31 |             | 2017-12-31 |             |            |              | 2018-01-31  |
      | -10       | 2017-09-01 | 2018-05-31 |            |             | 2018-01-31 |             |            |             | 2018-03-31 |              | 2018-03-31  |
      | -11       | 2017-09-01 | 2018-05-31 |            |             |            |             | 2017-12-31 |             | 2018-03-31 |              | 2018-03-31  |
      | -12       | 2017-09-01 | 2018-05-31 |            |             |            |             |            |             | 2018-03-31 |              | 2018-03-31  |
      | -13       | 2017-02-08 | 2023-08-07 |            |             |            |             |            |             |            |              |             |
      | -14       | 2007-10-16 | 2022-10-20 |            |             |            |             |            |             |            |              |             |

  Scenario Outline: Retrieve sentence details for an offender, check DTO sentence details and parole eligibility dates
    When sentence details are requested for an offender with booking id "<bookingId>"
    Then sentence start date matches "<ssd>"
    And sentence expiry date matches "<sed>"
    And additional days awarded matches "<ada>"
    And early term date matches "<etd>"
    And mid term date matches "<mtd>"
    And late term date matches "<ltd>"
    And home detention curfew eligibility date matches "<hdced>"
    And parole eligibility date matches "<ped>"
    And licence expiry date matches "<led>"

    Examples:
      | bookingId | ssd        | sed        | ada | etd        | mtd        | ltd        | hdced      | ped        | led        |
      | -1        | 2017-03-25 | 2020-03-24 | 12  |            |            |            |            |            |            |
      | -2        | 2016-11-22 | 2019-05-21 |     |            |            |            |            |            |            |
      | -3        | 2015-03-16 | 2020-03-15 |     |            |            | 2018-09-15 |            |            |            |
      | -4        | 2007-10-16 | 2022-10-20 | 5   |            |            |            |            |            |            |
      | -5        | 2017-02-08 | 2023-08-07 | 14  |            |            |            |            |            |            |
      | -6        | 2017-09-01 | 2018-05-31 | 17  |            |            |            |            |            |            |
      | -7        | 2017-09-01 | 2018-05-31 |     |            |            |            |            |            |            |
      | -8        | 2017-09-01 | 2018-05-31 |     |            |            |            |            |            |            |
      | -9        | 2017-09-01 | 2018-05-31 |     |            |            |            |            |            |            |
      | -10       | 2017-09-01 | 2018-05-31 |     |            |            |            |            |            |            |
      | -11       | 2017-09-01 | 2018-05-31 |     |            |            |            |            |            |            |
      | -12       | 2017-09-01 | 2018-05-31 |     |            |            |            |            |            |            |
      | -13       | 2017-02-08 | 2023-08-07 |     |            |            |            |            | 2021-05-05 | 2020-08-07 |
      | -14       | 2007-10-16 | 2022-10-20 |     | 2021-02-28 | 2021-03-25 |            | 2020-12-30 |            | 2021-09-24 |
