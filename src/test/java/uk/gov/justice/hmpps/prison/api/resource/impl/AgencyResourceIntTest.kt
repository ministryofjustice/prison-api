package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.hmpps.prison.api.model.Location.builder
import uk.gov.justice.hmpps.prison.repository.LocationRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.LocationGroupService

class AgencyResourceIntTest : ResourceTest() {

  @MockBean
  private lateinit var repository: LocationRepository

  @SpyBean
  private lateinit var locationGroupService: LocationGroupService

  @Nested
  @DisplayName("GET /api/agencies/{agencyId}/locations/groups")
  inner class LocationGroups {
    private val location1 = builder().locationId(-1L).locationType("WING").description("LEI-A").userDescription("BLOCK A").internalLocationCode("A").build()

    @Test
    fun locationGroups_allOk_returnsSuccessAndData() {
      whenever(repository.getLocationGroupData("LEI")).thenReturn(listOf(location1))

      webTestClient.get().uri("/api/agencies/LEI/locations/groups")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(1)
        .jsonPath("[0].name").isEqualTo("Block A")
        .jsonPath("[0].key").isEqualTo("A")
        .jsonPath("[0].children").isEmpty
    }

    @Test
    fun locationGroups_randomError_returnsErrorFromControllerAdvice() {
      whenever(locationGroupService.getLocationGroups("LEI")).thenThrow(EntityNotFoundException("test ex"))
      webTestClient.get().uri("/api/agencies/LEI/locations/groups")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("test ex")
    }
  }

  @Nested
  @DisplayName("GET /api/agencies/{agencyId}/locations/type/{type}")
  inner class InternalLocations {

    @Test
    fun locationsByType_singleResult_returnsSuccessAndData() {
      webTestClient.get().uri("/api/agencies/SYI/locations/type/AREA")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[0].locationId").isEqualTo(-208)
        .jsonPath("[0].locationType").isEqualTo("AREA")
        .jsonPath("[0].agencyId").isEqualTo("SYI")
        .jsonPath("[0].description").isEqualTo("CHAP")
        .jsonPath("[0].userDescription").isEqualTo("Chapel")
        .jsonPath("[0].locationPrefix").isEqualTo("SYI-CHAP")
        .jsonPath("[0].operationalCapacity").isEqualTo(1)
        .jsonPath("[0].currentOccupancy").isEqualTo(1)
    }

    @Test
    fun locationsByType_multipleResults_returnsAllLocations() {
      webTestClient.get().uri("/api/agencies/SYI/locations/type/CELL")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[*].locationId").value<List<Int>> { assertThat(it).containsExactlyInAnyOrder(-202, -204, -207) }
    }

    @Test
    fun locationsByType_agencyNotFound_returnsNotFound() {
      webTestClient.get().uri("/api/agencies/XYZ/locations/type/AREA")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Locations of type AREA in agency XYZ not found")
    }

    @Test
    fun locationsByType_locationTypeNotFound_returnsNotFound() {
      webTestClient.get().uri("/api/agencies/SYI/locations/type/WXYZ")
        .headers(setAuthorisation("ITAG_USER", listOf("SYSTEM_USER")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Locations of type WXYZ in agency SYI not found")
    }
  }

  @Nested
  @DisplayName("GET /api/agencies/{agencyId}/eventLocationsBooked")
  inner class EventLocationsBooked {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/agencies/LEI/eventLocationsBooked?bookedOnDay=2017-09-15")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return success if authorised`() {
      webTestClient.get().uri("/api/agencies/LEI/eventLocationsBooked?bookedOnDay=2017-09-15")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return locations for an agency, that are booked for offenders on the given date`() {
      webTestClient.get().uri("/api/agencies/LEI/eventLocationsBooked?bookedOnDay=2017-09-15")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(5)
        .jsonPath("[0].locationId").isEqualTo(-5)
        .jsonPath("[0].description").isEqualTo("LEI-A-1-3")
        .jsonPath("[0].userDescription").isEqualTo("A-1-3")
        .jsonPath("[1].locationId").isEqualTo(-26)
        .jsonPath("[1].description").isEqualTo("LEI-CARP")
        .jsonPath("[1].userDescription").isEqualTo("Carpentry Workshop")
        .jsonPath("[2].locationId").isEqualTo(-25)
        .jsonPath("[2].description").isEqualTo("LEI-CHAP")
        .jsonPath("[2].userDescription").isEqualTo("Chapel")
        .jsonPath("[3].locationId").isEqualTo(-27)
        .jsonPath("[3].description").isEqualTo("LEI-CRM1")
        .jsonPath("[3].userDescription").isEqualTo("Classroom 1")
        .jsonPath("[4].locationId").isEqualTo(-29)
        .jsonPath("[4].description").isEqualTo("LEI-MED")
        .jsonPath("[4].userDescription").isEqualTo("Medical Centre")
    }

    @Test
    fun `should return locations for an agency, that are booked for offenders on the given date with timeslot`() {
      webTestClient.get().uri("/api/agencies/LEI/eventLocationsBooked?bookedOnDay=2017-09-15&timeSlot=AM")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(1)
        .jsonPath("[0].locationId").isEqualTo(-25)
        .jsonPath("[0].description").isEqualTo("LEI-CHAP")
        .jsonPath("[0].userDescription").isEqualTo("Chapel")
    }

    @Test
    fun `should return locations for an agency, that are booked for offenders on the given date (appointment event_id=-15)`() {
      webTestClient.get().uri("/api/agencies/LEI/eventLocationsBooked?bookedOnDay=2017-12-25")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(1)
        .jsonPath("[0].locationId").isEqualTo(-25)
        .jsonPath("[0].description").isEqualTo("LEI-CHAP")
        .jsonPath("[0].userDescription").isEqualTo("Chapel")
    }

    @Test
    fun `should return locations for an agency, that are booked for offenders on the given date (offender_visit_id=-14)`() {
      webTestClient.get().uri("/api/agencies/LEI/eventLocationsBooked?bookedOnDay=2017-03-10")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(1)
        .jsonPath("[0].locationId").isEqualTo(-25)
        .jsonPath("[0].description").isEqualTo("LEI-CHAP")
        .jsonPath("[0].userDescription").isEqualTo("Chapel")
    }
  }

  @Nested
  @DisplayName("GET /api/agencies/type/{type}")
  inner class GetAgencyByType {
    @Nested
    inner class Security {
      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.get().uri("/api/agencies/type/INST")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `should return 200 so long as the client has a token`() {
        webTestClient.get().uri("/api/agencies/type/INST")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `should return 200 - role doesn't matter for insensitive data`() {
        webTestClient.get().uri("/api/agencies/type/INST")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    inner class HappyPath {

      @Test
      fun `can get a list of all prisons`() {
        webTestClient.get().uri("/api/agencies/type/INST")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$[?(@.agencyId=='BMI')]").exists()
          .jsonPath("$[?(@.agencyId=='BMI')].description").isEqualTo("Birmingham")
          .jsonPath("$[?(@.agencyId=='BMI')].agencyType").isEqualTo("INST")
          .jsonPath("$[?(@.agencyId=='BMI')].active").isEqualTo(true)
          .jsonPath("$[?(@.agencyId=='BXI')]").exists()
          .jsonPath("$[?(@.agencyId=='LEI')]").exists()
          .jsonPath("$[?(@.agencyId=='MDI')]").exists()
          .jsonPath("$[?(@.agencyId=='MUL')]").exists()
          .jsonPath("$[?(@.agencyId=='RNI')]").exists()
          .jsonPath("$[?(@.agencyId=='RSI')]").exists()
          .jsonPath("$[?(@.agencyId=='SYI')]").exists()
          .jsonPath("$[?(@.agencyId=='TRO')]").exists()
          .jsonPath("$[?(@.agencyId=='WAI')]").exists()
          .jsonPath("$[?(@.agencyType=='CRT')]").doesNotExist()
      }

      @Test
      fun `can get a list of courts for a certain type`() {
        webTestClient.get().uri("/api/agencies/type/CRT?courtType=YC")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$[?(@.agencyId=='ABDRCT')]").exists()
          .jsonPath("$[?(@.agencyId=='ABDRCT')].description").isEqualTo("Court 2")
          .jsonPath("$[?(@.agencyId=='ABDRCT')].agencyType").isEqualTo("CRT")
          .jsonPath("$[?(@.agencyId=='ABDRCT')].active").isEqualTo(true)
          .jsonPath("$[?(@.agencyId=='ABDRCT')].courtType").isEqualTo("YC")
          .jsonPath("$[?(@.agencyType=='INST')]").doesNotExist()
          .jsonPath("$[?(@.courtType=='MC')]").doesNotExist()
      }

      @Test
      fun `can get a list of courts for a certain type using the deprecated jurisdiction parameter`() {
        webTestClient.get().uri("/api/agencies/type/CRT?jurisdictionCode=YC")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$[?(@.agencyId=='ABDRCT')]").exists()
          .jsonPath("$[?(@.agencyId=='ABDRCT')].courtType").isEqualTo("YC")
          .jsonPath("$[?(@.agencyType=='INST')]").doesNotExist()
          .jsonPath("$[?(@.courtType=='MC')]").doesNotExist()
      }

      @Test
      fun `can request no formatting of description`() {
        webTestClient.get().uri("/api/agencies/type/CRT?courtType=YC&skipFormatLocation=true")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$[?(@.agencyId=='ABDRCT')].description").isEqualTo("court 2")
      }

      @Test
      fun `can get a list of courts for a list of types`() {
        webTestClient.get().uri("/api/agencies/type/CRT?courtType=YC&courtType=MC")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$[?(@.agencyId=='ABDRCT')]").exists()
          .jsonPath("$[?(@.agencyId=='ABDRCT')].courtType").isEqualTo("YC")
          .jsonPath("$[?(@.agencyId=='COURT1')]").exists()
          .jsonPath("$[?(@.agencyId=='COURT1')].courtType").isEqualTo("MC")
          .jsonPath("$[?(@.agencyType=='INST')]").doesNotExist()
      }
    }
  }
}
