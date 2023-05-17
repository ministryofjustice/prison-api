package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalService
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalServiceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ServiceAgencySwitchesRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException

@WithMockUser
class ServiceAgencySwitchResourceIntTest : ResourceTest() {

  @Autowired
  private lateinit var externalServiceRepository: ExternalServiceRepository

  @Autowired
  private lateinit var serviceAgencySwitchesRepository: ServiceAgencySwitchesRepository

  @Autowired
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  private lateinit var leeds: AgencyLocation
  private lateinit var moorland: AgencyLocation

  @BeforeEach
  fun `set up`() {
    leeds = agencyLocationRepository.findByIdOrNull("LEI") ?: throw EntityNotFoundException("Agency LEI is not saved")
    moorland = agencyLocationRepository.findByIdOrNull("MDI") ?: throw EntityNotFoundException("Agency MDI is not saved")

    val someService = externalServiceRepository.save(ExternalService.builder().serviceName("SOME_SERVICE").description("Some service").build())

    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, leeds)))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, moorland)))
  }

  @AfterEach
  fun `tear down`() {
    serviceAgencySwitchesRepository.deleteAll()
    externalServiceRepository.deleteAll()
  }

  @Nested
  inner class GetPrisons {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/service-prisons/MDI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/service-prisons/MDI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.get()
        .uri("/api/service-prisons/INVALID")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service code INVALID does not exist")
        }
    }

    @Test
    fun `should return a list of prisons for the service`() {
      webTestClient.get()
        .uri("/api/service-prisons/SOME_SERVICE")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("$[*]").value<List<PrisonDetails>> {
          assertThat(it).extracting("prisonId").containsExactlyInAnyOrder("LEI", "MDI")
        }
    }
  }
}
