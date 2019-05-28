Feature: Creating multiple appointments

  Background:
    Given a user has a token name of "BULK_APPOINTMENTS_USER"

  Scenario: Create several appointments with start and end times using the bulk appointments end-point.
    Given These appointment defaults:
      | locationId      | -25                      |
      | appointmentType | ACTI                     |
      | startTime       | Today_plus_1_days_T14:30 |
      | endTime         | Today_plus_1_days_T14:45 |
      | comment         | A default comment        |

    And these appointment details:
      | bookingId | startTime                | endTime                  | comment         |
      | -31       |                          |                          |                 |
      | -32       | Today_plus_1_days_T14:35 | Today_plus_1_days_T14:55 | Another comment |

    And these repeats:
    | period | count |
    | WEEKLY | 2     |

    When I make a request to create bulk appointments

    Then appointments for tomorrow are:
      | bookingId | appointmentType | startTime                | endTime                  | eventLocation |
      | -31       | ACTI            | Today_plus_1_days_T14:30 | Today_plus_1_days_T14:45 | Chapel        |
      | -32       | ACTI            | Today_plus_1_days_T14:35 | Today_plus_1_days_T14:55 | Chapel        |

    And The bulk appointment request status code is <200>

  Scenario: Create several appointments with start times only using the bulk appointments end-point.
    Given These appointment defaults:
      | locationId      | -25                      |
      | appointmentType | ACTI                     |
      | startTime       | Today_plus_2_days_T14:30 |
      | endTime         |                          |
      | comment         | A default comment        |

    And these appointment details:
      | bookingId | startTime                | endTime                  | comment         |
      | -31       |                          |                          |                 |
      | -32       | Today_plus_2_days_T14:35 | Today_plus_2_days_T14:55 | Another comment |

    When I make a request to create bulk appointments

    Then appointments for the day after tomorrow are:
      | bookingId | appointmentType | startTime                | endTime                  | eventLocation |
      | -31       | ACTI            | Today_plus_2_days_T14:30 |                          | Chapel        |
      | -32       | ACTI            | Today_plus_2_days_T14:35 | Today_plus_2_days_T14:55 | Chapel        |

    And The bulk appointment request status code is <200>

  Scenario: Reject invalid request
    Given These appointment defaults:
      | locationId      | -25               |
      | appointmentType | ACTI              |
      | startTime       | 2018-01-01T00:00  |
      | endTime         |                   |
      | comment         | A default comment |

    When I make a request to create bulk appointments
    Then the bulk appointment request is rejected
    And The bulk appointment request status code is <400>
