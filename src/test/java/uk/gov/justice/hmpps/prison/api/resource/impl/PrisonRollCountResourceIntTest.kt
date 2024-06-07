package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GET /api/prison/roll-count")
class PrisonRollCountResourceIntTest : ResourceTest() {

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not authorised role and override role`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 if has incorrect role`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI")
        .headers(setClientAuthorisation(listOf("DUMMY")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 200 if has authorised role and override role`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI")
        .headers(setClientAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return 403 if does not have prison in caseload`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI")
        .headers(setAuthorisation("WAI_USER", listOf("PRISON")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 200 if has prison in caseload`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI")
        .headers(setAuthorisation(listOf("PRISON")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  inner class HappyPath {
    @Test
    fun `should return correct data for Leeds`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI")
        .headers(setAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json(
          """
            {
              "prisonId": "LEI",
              "numUnlockRollToday": 24,
              "numCurrentPopulation": 24,
              "numArrivedToday": 0,
              "numInReception": 0,
              "numStillToArrive": 2,
              "numOutToday": 0,
              "numNoCellAllocated": 0,
              "totals": {
                "bedsInUse": 26,
                "currentlyInCell": 23,
                "currentlyOut": 2,
                "workingCapacity": 33,
                "netVacancies": 7,
                "outOfOrder": 3
              },
              "locations": [
                {
                  "locationId": "-1",
                  "locationType": "WING",
                  "locationCode": "A",
                  "fullLocationPath": "A",
                  "certified": true,
                  "localName": "Block A",
                  "rollCount": {
                    "bedsInUse": 12,
                    "currentlyInCell": 11,
                    "currentlyOut": 0,
                    "workingCapacity": 13,
                    "netVacancies": 1,
                    "outOfOrder": 3
                  },
                  "subLocations": [
                    {
                      "locationId": "-2",
                      "locationType": "LAND",
                      "locationCode": "1",
                      "fullLocationPath": "A-1",
                      "certified": true,
                      "localName": "Landing A/1",
                      "rollCount": {
                        "bedsInUse": 12,
                        "currentlyInCell": 11,
                        "currentlyOut": 0,
                        "workingCapacity": 13,
                        "netVacancies": 1,
                        "outOfOrder": 1
                      },
                      "subLocations": []
                    }
                  ]
                },
                {
                  "locationId": "-13",
                  "locationType": "WING",
                  "locationCode": "H",
                  "fullLocationPath": "H",
                  "certified": true,
                  "localName": "H",
                  "rollCount": {
                    "bedsInUse": 14,
                    "currentlyInCell": 12,
                    "currentlyOut": 2,
                    "workingCapacity": 20,
                    "netVacancies": 6,
                    "outOfOrder": 0
                  },
                  "subLocations": [
                    {
                      "locationId": "-14",
                      "locationType": "LAND",
                      "locationCode": "1",
                      "fullLocationPath": "H-1",
                      "certified": true,
                      "localName": "Landing H/1",
                      "rollCount": {
                        "bedsInUse": 14,
                        "currentlyInCell": 12,
                        "currentlyOut": 2,
                        "workingCapacity": 20,
                        "netVacancies": 6,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    }
                  ]
                }
              ]
            } 
          """.trimIndent(),

          false,
        )
    }

    @Test
    fun `should return correct cell data for Leeds on a landing`() {
      webTestClient.get().uri("/api/prison/roll-count/LEI/cells-only/-2")
        .headers(setAuthorisation(listOf("ESTABLISHMENT_ROLL")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json(
          """
            {
              "prisonId": "LEI",
              "numUnlockRollToday": 24,
              "numCurrentPopulation": 24,
              "numArrivedToday": 0,
              "numInReception": 0,
              "numStillToArrive": 2,
              "numOutToday": 0,
              "numNoCellAllocated": 0,
              "totals": {
                "bedsInUse": 26,
                "currentlyInCell": 23,
                "currentlyOut": 2,
                "workingCapacity": 33,
                "netVacancies": 7,
                "outOfOrder": 3
              },
              "locations": [
                {
                  "locationId": "-2",
                  "locationType": "LAND",
                  "locationCode": "1",
                  "fullLocationPath": "A-1",
                  "certified": true,
                  "localName": "Landing A/1",
                  "rollCount": {
                    "bedsInUse": 12,
                    "currentlyInCell": 11,
                    "currentlyOut": 0,
                    "workingCapacity": 13,
                    "netVacancies": 1,
                    "outOfOrder": 1
                  },
                  "subLocations": [
                    {
                      "locationId": "-3",
                      "locationType": "CELL",
                      "locationCode": "1",
                      "fullLocationPath": "A-1-1",
                      "certified": true,
                      "localName": "1",
                      "rollCount": {
                        "bedsInUse": 3,
                        "currentlyInCell": 3,
                        "currentlyOut": 0,
                        "workingCapacity": 2,
                        "netVacancies": -1,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-12",
                      "locationType": "CELL",
                      "locationCode": "10",
                      "fullLocationPath": "A-1-10",
                      "certified": true,
                      "localName": "10",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 1,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-1001",
                      "locationType": "CELL",
                      "locationCode": "BROKEN",
                      "fullLocationPath": "A-1-1001",
                      "certified": true,
                      "localName": "BROKEN",
                      "rollCount": {
                        "bedsInUse": 0,
                        "currentlyInCell": 0,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 1,
                        "outOfOrder": 1
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-1002",
                      "locationType": "CELL",
                      "locationCode": "REPURPOSED",
                      "fullLocationPath": "A-1-1002",
                      "certified": true,
                      "localName": "REPURPOSED",
                      "rollCount": {
                        "bedsInUse": 0,
                        "currentlyInCell": 0,
                        "currentlyOut": 0,
                        "workingCapacity": 0,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-4",
                      "locationType": "CELL",
                      "locationCode": "2",
                      "fullLocationPath": "A-1-2",
                      "certified": true,
                      "localName": "2",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 1,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-5",
                      "locationType": "CELL",
                      "locationCode": "3",
                      "fullLocationPath": "A-1-3",
                      "certified": true,
                      "localName": "3",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 1,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-6",
                      "locationType": "CELL",
                      "locationCode": "4",
                      "fullLocationPath": "A-1-4",
                      "certified": true,
                      "localName": "4",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 1,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-7",
                      "locationType": "CELL",
                      "locationCode": "5",
                      "fullLocationPath": "A-1-5",
                      "certified": true,
                      "localName": "5",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 0,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-8",
                      "locationType": "CELL",
                      "locationCode": "6",
                      "fullLocationPath": "A-1-6",
                      "certified": true,
                      "localName": "6",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 1,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-9",
                      "locationType": "CELL",
                      "locationCode": "7",
                      "fullLocationPath": "A-1-7",
                      "certified": true,
                      "localName": "7",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 1,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-10",
                      "locationType": "CELL",
                      "locationCode": "8",
                      "fullLocationPath": "A-1-8",
                      "certified": true,
                      "localName": "8",
                      "rollCount": {
                        "bedsInUse": 0,
                        "currentlyInCell": 0,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 1,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-11",
                      "locationType": "CELL",
                      "locationCode": "9",
                      "fullLocationPath": "A-1-9",
                      "certified": true,
                      "localName": "9",
                      "rollCount": {
                        "bedsInUse": 0,
                        "currentlyInCell": 0,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 1,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    },
                    {
                      "locationId": "-1000",
                      "locationType": "CELL",
                      "locationCode": "THE_ROOM",
                      "fullLocationPath": "AABCW-1",
                      "certified": true,
                      "localName": "THE_ROOM",
                      "rollCount": {
                        "bedsInUse": 1,
                        "currentlyInCell": 1,
                        "currentlyOut": 0,
                        "workingCapacity": 1,
                        "netVacancies": 0,
                        "outOfOrder": 0
                      },
                      "subLocations": []
                    }
                  ]
                }
              ]
            }   
          """.trimIndent(),

          false,
        )
    }
  }
}
