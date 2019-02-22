Feature: Creating multiple appointments

  Background:
    Given a user has authenticated with the API

  Scenario: Create several appointments with start and end times using the bulk appointments end-point.
    Given These appointment defaults:
    | locationId      |               -25 |
    | appointmentType |              ACTI |
    | startTime       |  2020-02-20T14:30 |
    | endTime         |  2020-02-20T14:45 |
    | comment         | A default comment |

    And these appointment details:
    | bookingId |        startTime |          endTime |         comment |
    |       -31 |                  |                  |                 |
    |       -32 | 2020-02-20T14:35 | 2020-02-20T14:55 | Another comment |

    When I make a request to create bulk appointments

    Then appointments for the date <2020-02-20> are:
    | bookingId | appointmentType | startTime        | endTime          | eventLocation |
    |       -31 |            ACTI | 2020-02-20T14:30 | 2020-02-20T14:45 |        Chapel |
    |       -32 |            ACTI | 2020-02-20T14:35 | 2020-02-20T14:55 |        Chapel |

    And The bulk appointment request status code is <200>

  Scenario: Create several appointments with start times only using the bulk appointments end-point.
    Given These appointment defaults:
      | locationId      |               -25 |
      | appointmentType |              ACTI |
      | startTime       |  2020-02-02T14:30 |
      | endTime         |                   |
      | comment         | A default comment |

    And these appointment details:
      | bookingId |        startTime |          endTime |         comment |
      |       -31 |                  |                  |                 |
      |       -32 | 2020-02-02T14:35 | 2020-02-02T14:55 | Another comment |

    When I make a request to create bulk appointments

    Then appointments for the date <2020-02-02> are:
      | bookingId | appointmentType | startTime        | endTime          | eventLocation |
      |       -31 |            ACTI | 2020-02-02T14:30 |                  |        Chapel |
      |       -32 |            ACTI | 2020-02-02T14:35 | 2020-02-02T14:55 |        Chapel |

    And The bulk appointment request status code is <200>

    Scenario: Reject invalid request
      Given These appointment defaults:
        | locationId      |               -25 |
        | appointmentType |              ACTI |
        | startTime       |  2018-01-01T00:00 |
        | endTime         |                   |
        | comment         | A default comment |

      When I make a request to create bulk appointments
      Then the bulk appointment request is rejected
      And The bulk appointment request status code is <400>
