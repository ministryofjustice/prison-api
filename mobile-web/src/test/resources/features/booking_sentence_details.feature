@global @wip
Feature: Booking Sentence Details

  Acceptence Criteria
  A logged on staff user can retrieve sentence details for an offender.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve sentence details for an offender
    When sentence details are requested for an offender with booking id "<bookingId>"
    Then sentence start date matches "<ssd>"
    And sentence end date matches "<sed>"
    And conditional release date matches "<crd>"
    And release date matches "<releaseDate>"

    Examples:
      | bookingId | ssd        | sed        | crd        | releaseDate |
      | -1        | 2017-03-25 | 2020-03-24 | 2019-03-24 | 2019-03-24  |
      | -2        | 2016-11-22 | 2019-05-21 |            | 2018-04-21  |