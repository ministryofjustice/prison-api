package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class LocationResourceIntTest : ResourceTest() {
  @Nested
  @DisplayName("GET api/locations/{locationId}")
  inner class Locations {
    @Test
    fun testGetLocation_found() {
      getLocation(ACTIVE_LOCATION_ID, "")
        .expectStatus().isOk
        .expectBody()
        .jsonPath("locationId").isEqualTo(ACTIVE_LOCATION_ID)
        .jsonPath("locationType").isEqualTo("WING")
        .jsonPath("description").isEqualTo("Block A")
      getLocation(-2, "")
        .expectStatus().isOk
        .expectBody()
        .jsonPath("locationId").isEqualTo(-2)
        .jsonPath("locationType").isEqualTo("LAND")
        .jsonPath("description").isEqualTo("Landing A/1")
      getLocation(-3, "")
        .expectStatus().isOk
        .expectBody()
        .jsonPath("locationId").isEqualTo(-3)
        .jsonPath("locationType").isEqualTo("CELL")
        .jsonPath("description").isEqualTo("LEI-A-1-1")
    }

    @Test
    fun `Request for specific location record that does not exist`() {
      getLocation(-9999, "")
        .expectStatus().isNotFound
    }

    @Test
    fun testGetLocation_Inactive_location_not_found() {
      getLocation(INACTIVE_LOCATION_ID, "")
        .expectStatus().isNotFound
    }

    @Test
    fun testGetLocation_Inactive_location_included_and_found() {
      getLocation(INACTIVE_LOCATION_ID, "?includeInactive=True")
        .expectStatus().isOk
        .expectBody()
        .jsonPath("locationId").isEqualTo(INACTIVE_LOCATION_ID)
    }

    @Test
    fun testGetLocation_Bad_query_parameter_value_Bad_request() {
      getLocation(INACTIVE_LOCATION_ID, "?includeInactive=Nope")
        .expectStatus().isBadRequest
    }

    @Test
    fun testGetLocation_No_query_parameter_value_Not_Found() {
      getLocation(INACTIVE_LOCATION_ID, "?includeInactive")
        .expectStatus().isNotFound
    }

    fun getLocation(locationId: Long, queryString: String): WebTestClient.ResponseSpec =
      webTestClient.get()
        .uri("/api/locations/$locationId$queryString")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
  }

  @Nested
  @DisplayName("GET api/locations/code/{code}")
  inner class LocationByCode {
    @Test
    fun testGetLocation_by_code() {
      val response = getLocationByCode("LEI-A")
      response.expectStatus().isOk
        .expectBody()
        .jsonPath("agencyId").isEqualTo("LEI")
        .jsonPath("locationType").isEqualTo("WING")
        .jsonPath("userDescription").isEqualTo("Block A")
        .jsonPath("subLocations").isEqualTo(true)
    }

    @Test
    fun testGetLocation_by_code_cell() {
      val response = getLocationByCode("LEI-A-1-1")
      response.expectStatus().isOk
        .expectBody()
        .jsonPath("agencyId").isEqualTo("LEI")
        .jsonPath("locationType").isEqualTo("CELL")
        .jsonPath("subLocations").isEqualTo(false)
    }

    @Test
    fun testGetLocation_by_code_not_found() {
      val response = getLocationByCode("LEI-X")
      response.expectStatus().isNotFound
    }

    private fun getLocationByCode(code: String): WebTestClient.ResponseSpec =
      webTestClient.get()
        .uri("/api/locations/code/$code")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
  }

  @Nested
  @DisplayName("GET api/locations/description/{agency}/inmates")
  inner class DescriptionInmates {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/locations/description/LEI/inmates")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/locations/description/LEI/inmates")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 if has the wrong role`() {
      webTestClient.get().uri("/api/locations/description/LEI/inmates")
        .headers(setClientAuthorisation(listOf("BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Retrieve a list of inmates at a specific agency location`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .header("Page-Limit", "30")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(27)
        .jsonPath("$.length($.[?(@.convictedStatus == 'Convicted')].length())").isEqualTo(8)
        .jsonPath("$.length($.[?(@.convictedStatus == 'Remand')].length())").isEqualTo(3)

      webTestClient.get()
        .uri("/api/locations/description/BMI/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `Retrieve a list of inmates queried by convicted status`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?convictedStatus=Convicted")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange().expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(8)

      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?convictedStatus=Remand")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange().expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(3)
    }

    @Test
    fun `Search all offenders across all allowed locations`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectHeader().valueEquals("Page-Offset", 0)
        .expectHeader().valueEquals("Page-Limit", 10)
        .expectHeader().valueEquals("Total-Records", 27)
    }

    @Test
    fun `Search based on keywords 1`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=ANDERSON")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ARTHUR", "GILLIAN") }
        .jsonPath("[*].middleName").value<List<String>> { assertThat(it).containsOnly("BORIS", "EVE") }
        .jsonPath("[*].assignedLivingUnitDesc").value<List<String>> { assertThat(it).containsOnly("A-1-1", "H-1-5") }
    }

    @Test
    fun `Search based on keywords 2`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=ARTHUR")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ARTHUR") }
        .jsonPath("[*].middleName").value<List<String>> { assertThat(it).containsOnly("BORIS") }
        .jsonPath("[*].assignedLivingUnitDesc").value<List<String>> { assertThat(it).containsOnly("A-1-1") }
    }

    @Test
    fun `Search based on keywords 3`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=MATTHEWS")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DONALD") }
        .jsonPath("[*].middleName").value<List<String>> { assertThat(it).containsOnly("JEFFREY") }
        .jsonPath("[*].assignedLivingUnitDesc").value<List<String>> { assertThat(it).containsOnly("A-1-10") }
    }

    @Test
    fun `Search based on keywords 4`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=anderson")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ARTHUR", "GILLIAN") }
    }

    @Test
    fun `Search based on keywords 5`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=AnDersOn")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ARTHUR", "GILLIAN") }
    }

    @Test
    fun `Search based on keywords 6`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=UNKNOWN")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `Search based on keywords 7`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=DONALD MATTHEWS")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DONALD") }
        .jsonPath("[*].middleName").value<List<String>> { assertThat(it).containsOnly("JEFFREY") }
        .jsonPath("[*].assignedLivingUnitDesc").value<List<String>> { assertThat(it).containsOnly("A-1-10") }
    }

    @Test
    fun `Search based on keywords 8`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=A1234AB")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("GILLIAN") }
    }

    @Test
    fun `Search based on keywords 9`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=ANDERSON, GILLIAN")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("GILLIAN") }
    }

    @Test
    fun `Search based on keywords 10`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=ANDERSON GILLIAN")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("GILLIAN") }
    }

    @Test
    fun `Search all offenders across a specified location 1`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-A/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .header("Page-Limit", "30")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(11)
    }

    @Test
    fun `Search all offenders across a specified location 2`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-H/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .header("Page-Limit", "30")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(14)
    }

    @Test
    fun `Search all offenders across a specified location 3`() {
      webTestClient.get()
        .uri("/api/locations/description/BXI/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `Search all offenders across a specified location 4`() {
      webTestClient.get()
        .uri("/api/locations/description/XXX/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `Search based on keywords and locations 1`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-A/inmates?keywords=ANDERSON")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ARTHUR") }
    }

    @Test
    fun `Search based on keywords and locations 2`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-A/inmates?keywords=ARTHUR")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ARTHUR") }
    }

    @Test
    fun `Search based on keywords and locations 3`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-A-1/inmates?keywords=JONES")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("HARRY") }
    }

    @Test
    fun `Search based on keywords and locations 4`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=D SMITH")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DANIEL", "DARIUS") }
    }

    @Test
    fun `Search based on keywords and locations 5`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=SMITH D")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DANIEL", "DARIUS") }
    }

    @Test
    fun `Search based on keywords and locations 6`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=SMITH,D")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DANIEL", "DARIUS") }
    }

    @Test
    fun `Search based on keywords and locations 7`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=SMITH DAR")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DARIUS") }
    }

    @Test
    fun `Search based on keywords and locations 8`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=DAN SMITH")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DANIEL") }
    }

    @Test
    fun `Search based on keywords and locations 9`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-A-1/inmates?keywords=MATTHEWS")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("DONALD") }
    }

    @Test
    fun `Search based on keywords and locations 10`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-H/inmates?keywords=ANDERSON")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("GILLIAN") }
    }

    @Test
    fun `Search based on keywords and locations 11`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-RECP/inmates?keywords=anderson")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `Search based on keywords and locations 12`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=AN")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ANTHONY", "ARTHUR", "GILLIAN") }
    }

    @Test
    fun `Search based on keywords and locations 13`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=G AN")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("GILLIAN") }
    }

    @Test
    fun `Search based on keywords and locations 14`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?keywords=AN A")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("ANTHONY", "ARTHUR") }
    }

    @Test
    fun `Search based on keywords and locations 15`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI-H/inmates?keywords=A1234AB")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsOnly("GILLIAN") }
    }

    @Test
    fun `Search based on alerts with category 1`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?alerts=SR&returnAlerts=true&returnCategory=true")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].lastName").value<List<String>> { assertThat(it).containsOnly("BATES") }
        .jsonPath("[*].alertsDetails").value<List<List<String>>> { assertThat(it).containsOnly(listOf("SR", "XTACT")) }
        .jsonPath("[*].categoryCode").value<List<String>> { assertThat(it).containsOnly("X") }
    }

    @Test
    fun `Search based on alerts with category 2`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?alerts=V46&alerts=P1&returnAlerts=true&returnCategory=true")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].lastName").value<List<String>> { assertThat(it).containsOnly("ANDREWS", "MATTHEWS") }
        .jsonPath("[*].alertsDetails")
        .value<List<List<String>>> { assertThat(it).containsOnly(listOf("V46", "XTACT"), listOf("P1", "XTACT")) }
        .jsonPath("[*].categoryCode").value<List<String>> { assertThat(it).containsOnly("C", "Z") }
    }

    @Test
    fun `Search based on alerts with category 3`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?alerts=XA&returnAlerts=true&returnCategory=true")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].lastName").value<List<String>> { assertThat(it).containsOnly("ANDERSON") }
        .jsonPath("[*].alertsDetails")
        .value<List<List<String>>> { assertThat(it).containsOnly(listOf("XA", "HC", "XTACT")) }
        .jsonPath("[*].categoryCode").value<List<String>> { assertThat(it).containsOnly("LOW") }
    }

    @Test
    fun `Search based on alerts with category 4`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?alerts=RSS&returnAlerts=true&returnCategory=true")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `Search based on date of birth ranges 1`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?fromDob=1970-01-01&toDob=1972-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(5)
        .jsonPath("[*].dateOfBirth").value<List<String>> {
          assertThat(it).containsOnly(
            "1970-01-01",
            "1970-01-01",
            "1970-03-01",
            "1970-12-30",
            "1972-01-01",
          )
        }
    }

    @Test
    fun `Search based on date of birth ranges 2`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?fromDob=1972-01-02")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(7)
        .jsonPath("[*].dateOfBirth").value<List<String>> {
          assertThat(it).containsOnly(
            "1974-01-01",
            "1977-01-02",
            "1979-12-31",
            "1980-01-02",
            "1986-06-01",
            "1998-08-28",
            "1999-10-27",
          )
        }
    }

    @Test
    fun `Search based on date of birth ranges 3`() {
      webTestClient.get()
        .uri("/api/locations/description/LEI/inmates?toDob=1945-01-09")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("[*].dateOfBirth").value<List<String>> { assertThat(it).containsOnly("1945-01-09") }
    }
  }

  @Nested
  @DisplayName("GET api/locations/{locationId}/inmates")
  inner class Inmates {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/locations/description/LEI/inmates")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/locations/description/LEI/inmates")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 if has the wrong role`() {
      webTestClient.get().uri("/api/locations/description/LEI/inmates")
        .headers(setClientAuthorisation(listOf("BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Perform location search by location id`() {
      webTestClient.get()
        .uri("/api/locations/-8/inmates")
        .headers(setAuthorisation("ITAG_USER", listOf("VIEW_PRISONER_DATA")))
        // .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .header("Page-Limit", "30")
        .accept(MediaType.APPLICATION_JSON)
        .exchange().expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$.[0].bookingId").isEqualTo(-10)
    }
  }

  companion object {
    private const val ACTIVE_LOCATION_ID = -1L
    private const val INACTIVE_LOCATION_ID = -31L
  }
}
