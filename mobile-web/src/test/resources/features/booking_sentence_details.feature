@global @wip
Feature: Booking Sentence Details

  Acceptence Criteria
  A logged on staff user can retrieve sentence details for an offender.

  Background:
    Given a user has authenticated with the API

  Scenario: Retrieve sentence details for an offender
    When sentence details are requested for an offender with booking id "<bookingId>"
