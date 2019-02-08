@wip
Feature: Creating multiple appointments

  Background:
    Given a user has authenticated with the API

  Scenario: Create several appointments with start and end times using the bulk appointments end-point.
    Given These appointment defaults:
    | locationId      |               -25 |
    | appointmentType |              ACTI |
    | startTime       |  2030-01-01T14:30 |
    | endTime         |  2030-01-01T14:45 |
    | comment         | A default comment |

    And these appointment details:
    | bookingId |        startTime |          endTime |         comment |
    |       -31 |                  |                  |                 |
    |       -32 | 2030-01-01T14:35 | 2030-01-01T14:55 | Another comment |

    When I make a request to create bulk appointments

    Then appointments for the date <2030-01-01> are:
    | bookingId | appointmentType | startTime        | endTime          | eventLocation |
    |       -31 |            ACTI | 2030-01-01T14:30 | 2030-01-01T14:45 |        Chapel |
    |       -32 |            ACTI | 2030-01-01T14:35 | 2030-01-01T14:55 |        Chapel |

  Scenario: Create several appointments with start times only using the bulk appointments end-point.
    Given These appointment defaults:
      | locationId      |               -25 |
      | appointmentType |              ACTI |
      | startTime       |  2030-02-02T14:30 |
      | endTime         |                   |
      | comment         | A default comment |

    And these appointment details:
      | bookingId |        startTime |          endTime |         comment |
      |       -31 |                  |                  |                 |
      |       -32 | 2030-02-02T14:35 | 2030-02-02T14:55 | Another comment |

    When I make a request to create bulk appointments

    Then appointments for the date <2030-02-02> are:
      | bookingId | appointmentType | startTime        | endTime          | eventLocation |
      |       -31 |            ACTI | 2030-02-02T14:30 |                  |        Chapel |
      |       -32 |            ACTI | 2030-02-02T14:35 | 2030-02-02T14:55 |        Chapel |
