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
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalServiceEntity
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
  private lateinit var someService: ExternalServiceEntity

  @BeforeEach
  fun `set up`() {
    leeds = agencyLocationRepository.findByIdOrNull("LEI") ?: throw EntityNotFoundException("Agency LEI is not saved")
    moorland = agencyLocationRepository.findByIdOrNull("MDI") ?: throw EntityNotFoundException("Agency MDI is not saved")

    someService = externalServiceRepository.save(ExternalServiceEntity("SOME_SERVICE", "Some service"))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, leeds)))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, moorland)))
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
          assertThat(it).contains("Prison id INVALID does not exist")
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
          assertThat(it).contains("Prison id INVALID does not exist")
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
