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
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.ALL_AGENCIES
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalServiceEntity
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalServiceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ServiceAgencySwitchesRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException

class ServicePrisonsResourceIntTest : ResourceTest() {

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
  inner class GetServicePrisons {
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

    @ParameterizedTest
    @ValueSource(strings = ["ROLE_SERVICE_AGENCY_SWITCHES", "ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO"])
    fun `should return a list of prisons for the service`(role: String) {
      webTestClient.get()
        .uri("/api/service-prisons/SOME_SERVICE")
        .headers(setAuthorisation(listOf(role)))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("$[*]").value<List<PrisonDetails>> {
          assertThat(it).extracting("prisonId").containsExactlyInAnyOrder("LEI", "MDI")
        }
    }

    @Test
    fun `should return the dummy all prison for the service`() {
      webTestClient.get()
        .uri("/api/service-prisons/OTHER_SERVICE")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("$[*]").value<List<PrisonDetails>> {
          assertThat(it).extracting("prisonId").containsExactlyInAnyOrder("*ALL*")
        }
    }
  }

  @Nested
  inner class CheckServicePrison {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.get()
        .uri("/api/service-prisons/OTHER_SERVICES/prison/MDI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.get()
        .uri("/api/service-prisons/OTHER_SERVICES/prison/MDI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.get()
        .uri("/api/service-prisons/INVALID_SERVICE/prison/MDI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service INVALID_SERVICE not turned on for prison MDI")
        }
    }

    @Test
    fun `should return not found if service not switched on`() {
      webTestClient.get()
        .uri("/api/service-prisons/SOME_SERVICE/prison/SYI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service SOME_SERVICE not turned on for prison SYI")
        }
    }

    @Test
    fun `should return no content if service switched on`() {
      webTestClient.get()
        .uri("/api/service-prisons/SOME_SERVICE/prison/MDI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return no content if service switched on and prison wildcarded`() {
      webTestClient.get()
        .uri("/api/service-prisons/OTHER_SERVICE/prison/SYI")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__SERVICE_AGENCY_SWITCHES__RO")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  inner class CreateServicePrison {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.post()
        .uri("/api/service-prisons/SOME_SERVICE/prison/BXI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.post()
        .uri("/api/service-prisons/SOME_SERVICE/prison/BXI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.post()
        .uri("/api/service-prisons/INVALID/prison/BXI")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service code INVALID does not exist")
        }
    }

    @Test
    fun `should return not found if prison does not exist`() {
      webTestClient.post()
        .uri("/api/service-prisons/SOME_SERVICE/prison/INVALID")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Agency id INVALID does not exist")
        }
    }

    @Test
    fun `should return conflict if prison already active for a service`() {
      webTestClient.post()
        .uri("/api/service-prisons/SOME_SERVICE/prison/MDI")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(409)
        .expectBody()
        .jsonPath("userMessage").value<String> {
          assertThat(it).contains("Prison MDI is already active for service SOME_SERVICE")
        }
    }

    @Test
    fun `should return ok if prison added to the service`() {
      webTestClient.post()
        .uri("/api/service-prisons/SOME_SERVICE/prison/BXI")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("prisonId").isEqualTo("BXI")

      val switch = serviceAgencySwitchesRepository.findByIdExternalServiceEntity(someService).firstOrNull { it.id.agencyLocation.id == "BXI" }
      assertThat(switch).isNotNull
    }
  }

  @Nested
  inner class RemoveServicePrison {
    @Test
    fun `should return unauthorised without an auth token`() {
      webTestClient.delete()
        .uri("/api/service-prisons/SOME_SERVICE/prison/BXI")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden without a valid role`() {
      webTestClient.delete()
        .uri("/api/service-prisons/SOME_SERVICE/prison/BXI")
        .headers(setAuthorisation(listOf("ROLE_INVALID")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return not found if service does not exist`() {
      webTestClient.delete()
        .uri("/api/service-prisons/INVALID/prison/MDI")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Service code INVALID does not exist")
        }
    }

    @Test
    fun `should return not found if prison does not exist`() {
      webTestClient.delete()
        .uri("/api/service-prisons/SOME_SERVICE/prison/INVALID")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").value<String> {
          assertThat(it).contains("Agency id INVALID does not exist")
        }
    }

    @Test
    fun `should return ok if prison already inactive for a service`() {
      webTestClient.delete()
        .uri("/api/service-prisons/SOME_SERVICE/prison/BXI")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `should return ok if prison removed from the service`() {
      webTestClient.delete()
        .uri("/api/service-prisons/SOME_SERVICE/prison/MDI")
        .headers(setAuthorisation(listOf("ROLE_SERVICE_AGENCY_SWITCHES")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent

      val switch = serviceAgencySwitchesRepository.findByIdExternalServiceEntity(someService).firstOrNull { it.id.agencyLocation.id == "MDI" }
      assertThat(switch).isNull()
    }
  }
}
