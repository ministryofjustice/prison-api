package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.resource.AgencyDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.ALL_AGENCIES
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalServiceEntity
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalServiceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ServiceAgencySwitchesRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException

class ServiceAgencySwitchResourceIntTest : ResourceTest() {

  @Autowired
  private lateinit var externalServiceRepository: ExternalServiceRepository

  @Autowired
  private lateinit var serviceAgencySwitchesRepository: ServiceAgencySwitchesRepository

  @Autowired
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  private lateinit var leeds: AgencyLocation
  private lateinit var moorland: AgencyLocation
  private lateinit var all: AgencyLocation
  private lateinit var someService: ExternalServiceEntity
  private lateinit var otherService: ExternalServiceEntity

  @BeforeEach
  fun `set up`() {
    leeds = agencyLocationRepository.findByIdOrNull("LEI") ?: throw EntityNotFoundException("Agency LEI is not saved")
    moorland = agencyLocationRepository.findByIdOrNull("MDI") ?: throw EntityNotFoundException("Agency MDI is not saved")
    all = agencyLocationRepository.findByIdOrNull(ALL_AGENCIES) ?: throw EntityNotFoundException("Agency *ALL* is not saved")

    someService = externalServiceRepository.save(ExternalServiceEntity("SOME_SERVICE", "Some service"))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, leeds)))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, moorland)))

    otherService = externalServiceRepository.save(ExternalServiceEntity("OTHER_SERVICE", "Other service"))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(otherService, all)))
  }

  @AfterEach
  fun `tear down`() {
    serviceAgencySwitchesRepository.deleteAll()
    externalServiceRepository.deleteAll()
  }

  @Nested
  inner class GetAgencySwitches {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/agency-switches/MDI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/agency-switches/MDI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.get()
        .uri("/api/agency-switches/INVALID")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service code INVALID does not exist")
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO", "ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW"])
    fun `should return a list of agencies for the service`(role: String) {
      webTestClient.get()
        .uri("/api/agency-switches/SOME_SERVICE")
        .headers(setAuthorisation(listOf(role)))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("$[*]").value<List<AgencyDetails>> {
          assertThat(it).extracting("agencyId").containsExactlyInAnyOrder("LEI", "MDI")
        }
    }

    @Test
    fun `should return the dummy all agency for the service`() {
      webTestClient.get()
        .uri("/api/agency-switches/OTHER_SERVICE")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("$[*]").value<List<AgencyDetails>> {
          assertThat(it).extracting("agencyId").containsExactlyInAnyOrder("*ALL*")
        }
    }
  }

  @Nested
  inner class CheckServiceAgency {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/agency-switches/OTHER_SERVICES/agency/MDI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/agency-switches/OTHER_SERVICES/agency/MDI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.get()
        .uri("/api/agency-switches/INVALID_SERVICE/agency/MDI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service INVALID_SERVICE not turned on for agency MDI")
        }
    }

    @Test
    fun `should return not found if service not switched on`() {
      webTestClient.get()
        .uri("/api/agency-switches/SOME_SERVICE/agency/SYI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service SOME_SERVICE not turned on for agency SYI")
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO", "ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW"])
    fun `should return no content if service switched on`(role: String) {
      webTestClient.get()
        .uri("/api/agency-switches/SOME_SERVICE/agency/MDI")
        .headers(setAuthorisation(listOf(role)))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return no content if service switched on and agency wildcarded`() {
      webTestClient.get()
        .uri("/api/agency-switches/OTHER_SERVICE/agency/SYI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  inner class CreateServiceAgency {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.post()
        .uri("/api/agency-switches/SOME_SERVICE/agency/BXI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.post()
        .uri("/api/agency-switches/SOME_SERVICE/agency/BXI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.post()
        .uri("/api/agency-switches/INVALID/agency/BXI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service code INVALID does not exist")
        }
    }

    @Test
    fun `should return not found if agency does not exist`() {
      webTestClient.post()
        .uri("/api/agency-switches/SOME_SERVICE/agency/INVALID")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Agency id INVALID does not exist")
        }
    }

    @Test
    fun `should return conflict if agency already active for a service`() {
      webTestClient.post()
        .uri("/api/agency-switches/SOME_SERVICE/agency/MDI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(409)
        .expectBody()
        .jsonPath("userMessage").value<String> {
          assertThat(it).contains("Agency MDI is already active for service SOME_SERVICE")
        }
    }

    @Test
    fun `should return ok if agency added to the service`() {
      webTestClient.post()
        .uri("/api/agency-switches/SOME_SERVICE/agency/BXI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("agencyId").isEqualTo("BXI")

      val switch = serviceAgencySwitchesRepository.findByIdExternalServiceEntity(someService).firstOrNull { it.id.agencyLocation.id == "BXI" }
      assertThat(switch).isNotNull
    }
  }

  @Nested
  inner class RemoveServiceAgency {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.delete()
        .uri("/api/agency-switches/SOME_SERVICE/agency/BXI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.delete()
        .uri("/api/agency-switches/SOME_SERVICE/agency/BXI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.delete()
        .uri("/api/agency-switches/INVALID/agency/MDI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service code INVALID does not exist")
        }
    }

    @Test
    fun `should return not found if agency does not exist`() {
      webTestClient.delete()
        .uri("/api/agency-switches/SOME_SERVICE/agency/INVALID")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Agency id INVALID does not exist")
        }
    }

    @Test
    fun `should return ok if agency already inactive for a service`() {
      webTestClient.delete()
        .uri("/api/agency-switches/SOME_SERVICE/agency/BXI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    fun `should return ok if agency removed from the service`() {
      webTestClient.delete()
        .uri("/api/agency-switches/SOME_SERVICE/agency/MDI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RW")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      val switch = serviceAgencySwitchesRepository.findByIdExternalServiceEntity(someService).firstOrNull { it.id.agencyLocation.id == "MDI" }
      assertThat(switch).isNull()
    }
  }
}
