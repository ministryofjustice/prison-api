@wip
Feature: Booking Sentence Details

  Acceptence Criteria
  A logged on staff user can retrieve sentence details for an offender.

  Background:
    Given a user has authenticated with the API

  Scenario Outline: Retrieve sentence details for an offender
    When sentence details are requested for an offender with booking id "<bookingId>"
    Then sentence start date matches "<ssd>"
    And sentence end date matches "<sed>"

    Examples:
      | bookingId | ssd        | sed        |
      | -1        | 2017-03-25 | 2017-09-24 |