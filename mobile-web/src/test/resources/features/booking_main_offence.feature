@nomis
Feature: Booking Main Sentence Summary

Acceptence Criteria
  A logged on staff user can retrieve a sentence summary for an offender booking.

Background:
    Given a user has authenticated with the API

Scenario: Retrieve sentence
    When a sentence with booking id -1 is requested
    Then the returned mainSentence sentenceLength is "6 months"
    And the returned mainSentence releaseDate is "2018-04-23"
    And the returned mainSentence mainOffenceDescription is "Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)"

Scenario: Booking id does not exist
    When a sentence with a nonexistent booking id is requested
    Then resource not found response is received from sentence API

  Scenario: The logged on staff user's caseload does not include the booking id
    When a sentence with a booking id in a different caseload is requested
    Then resource not found response is received from sentence API
