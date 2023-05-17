package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalService
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class ServiceAgencySwitchesRepositoryTest {

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
    val anotherService = externalServiceRepository.save(ExternalService.builder().serviceName("ANOTHER_SERVICE").description("Another service").build())

    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, leeds)))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(someService, moorland)))
    serviceAgencySwitchesRepository.save(ServiceAgencySwitch(ServiceAgencySwitchId(anotherService, moorland)))
  }

  @AfterEach
  fun `tear down`() {
    serviceAgencySwitchesRepository.deleteAll()
    externalServiceRepository.deleteAll()
  }

  @Test
  fun `can find agency switches by service`() {
    val someService = externalServiceRepository.findByIdOrNull("SOME_SERVICE") ?: throw EntityNotFoundException("SOME_SERVICE not found")
    val someAgencies = serviceAgencySwitchesRepository.findByIdExternalService(someService)
    assertThat(someAgencies).extracting("id").extracting("agencyLocation").extracting("id").containsExactly("LEI", "MDI")
  }

  @Test
  fun `can delete agency switches`() {
    val someService = externalServiceRepository.findByIdOrNull("SOME_SERVICE") ?: throw EntityNotFoundException("SOME_SERVICE not found")
    val someServiceAtMoorland = serviceAgencySwitchesRepository.findByIdOrNull(ServiceAgencySwitchId(someService, moorland)) ?: throw EntityNotFoundException("SOME_SERVICE at MDI not found")
    serviceAgencySwitchesRepository.delete(someServiceAtMoorland)

    val someAgencies = serviceAgencySwitchesRepository.findByIdExternalService(someService)
    assertThat(someAgencies).extracting("id").extracting("agencyLocation").extracting("id").containsExactly("LEI")
  }
}
