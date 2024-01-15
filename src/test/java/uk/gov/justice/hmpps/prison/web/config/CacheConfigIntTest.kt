@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.web.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.api.resource.AgencyResource
import uk.gov.justice.hmpps.prison.api.support.Order.ASC
import uk.gov.justice.hmpps.prison.repository.AgencyRepository
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.ReferenceDataRepository
import uk.gov.justice.hmpps.prison.repository.StaffRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceCalcTypeRepository
import uk.gov.justice.hmpps.prison.service.AgencyService
import uk.gov.justice.hmpps.prison.service.CaseNoteService
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService
import uk.gov.justice.hmpps.prison.service.StaffService
import java.time.LocalDate

@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.cache.type=jcache"])
@TestClassOrder(ClassOrderer.OrderAnnotation::class)
class CacheConfigIntTest {
  @Autowired
  private lateinit var cacheManager: CacheManager

  @Autowired
  private lateinit var referenceDomainService: ReferenceDomainService

  @SpyBean
  private lateinit var referenceDataRepository: ReferenceDataRepository

  @Autowired
  private lateinit var caseNoteService: CaseNoteService

  @SpyBean
  private lateinit var caseNoteRepository: CaseNoteRepository

  @Autowired
  private lateinit var staffService: StaffService

  @SpyBean
  private lateinit var staffRepository: StaffRepository

  @Autowired
  private lateinit var agencyResource: AgencyResource

  @SpyBean
  private lateinit var agencyService: AgencyService

  @SpyBean
  private lateinit var agencyRepository: AgencyRepository

  @SpyBean
  private lateinit var sentenceCalcTypeRepository: SentenceCalcTypeRepository

  @Test
  fun `test that each cache is tested by this class`() {
    val nestedTests = this::class.nestedClasses.map { it.simpleName!!.replace("_.*".toRegex(), "") }.toSet()

    assertThat(cacheManager.cacheNames.toSet().minus(nestedTests).minus("jwks")).isEmpty()
  }

  @Nested
  @Order(1)
  inner class referenceDomain_cache {
    @Test
    fun `test domain that doesn't exist won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        referenceDomainService.getReferenceCodesByDomain("NOT_EXISTS")
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test domain that is null won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        referenceDomainService.getReferenceCodesByDomain(null)
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test domain that is blank won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        referenceDomainService.getReferenceCodesByDomain(" ")
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test domain that exist is added to cache`() {
      reset(referenceDataRepository)

      val existingDomain = "AGY_LOC_TYPE"
      val codes = referenceDomainService.getReferenceCodesByDomain(existingDomain)
      assertThat(codes).isNotEmpty

      // calling twice should only result in one call to the repository
      referenceDomainService.getReferenceCodesByDomain(existingDomain)

      verify(referenceDataRepository).getReferenceDomain(existingDomain)
    }
  }

  @Nested
  inner class referenceCodesByDomain_cache {
    // Code checks referenceDataRepository.getReferenceDomain to ensure that the domain exists so therefore can't test
    // whether referenceDataRepository.getReferenceCodesByDomain exists

    @Test
    fun `test domain that exist is added to cache`() {
      val types = referenceDomainService.getAlertTypes(null, null, 0, 10)
      assertThat(types?.items).isNotEmpty

      // calling twice should only result in one call to the repository
      referenceDomainService.getAlertTypes(null, null, 0, 10)

      verify(referenceDataRepository).getReferenceCodesByDomain("ALERT", true, "code", ASC, 0, 10)
    }
  }

  @Nested
  inner class referenceCodeByDomainAndCode_cache {
    @Test
    fun `test code that doesn't exist won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        referenceDomainService.getReferenceCodeByDomainAndCode("ALERT", "NOT_EXISTS", false)
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test code that is null won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        referenceDomainService.getReferenceCodeByDomainAndCode("ALERT", null, false)
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test code that is blank won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        referenceDomainService.getReferenceCodeByDomainAndCode("ALERT", " ", false)
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test code that exist is added to cache`() {
      val existingDomain = "AGY_LOC_TYPE"
      val code = referenceDomainService.getReferenceCodeByDomainAndCode(existingDomain, "INST", false)
      assertThat(code).isNotEmpty

      // calling twice should only result in one call to the repository
      referenceDomainService.getReferenceCodeByDomainAndCode(existingDomain, "INST", false)

      verify(referenceDataRepository).getReferenceCodeByDomainAndCode(existingDomain, "INST", false)
    }
  }

  @Nested
  inner class getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag_cache {
    @Test
    fun `test case load type that doesn't exist won't cause cache to fall over in a heap`() {
      val types = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType("NOT_EXISTS")
      assertThat(types).isEmpty()
    }

    @Test
    fun `test case load type that is null won't cause cache to fall over in a heap`() {
      val types = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(null)
      assertThat(types).isEmpty()
    }

    @Test
    fun `test case load type that is blank won't cause cache to fall over in a heap`() {
      val types = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType("  ")
      assertThat(types).isEmpty()
    }

    @Test
    fun `test case load type that exist is added to cache`() {
      val types = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType("INST")
      assertThat(types).isNotEmpty

      // calling twice should only result in one call to the repository
      caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType("INST")

      verify(caseNoteRepository).getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag("INST", true)
    }
  }

  @Nested
  inner class usedCaseNoteTypesWithSubTypes_cache {
    // No parameters here so can't test the doesn't exist scenario

    @Test
    fun `test case load type that exist is added to cache`() {
      val code = caseNoteService.usedCaseNoteTypesWithSubTypes
      assertThat(code).isNotEmpty

      // calling twice should only result in one call to the repository
      caseNoteService.usedCaseNoteTypesWithSubTypes

      verify(caseNoteRepository).usedCaseNoteTypesWithSubTypes
    }
  }

  @Nested
  inner class findByStaffId_cache {
    @Test
    fun `test staff that doesn't exist won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        staffService.getStaffDetail(-12345L)
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test staff that is null won't cause cache to fall over in a heap`() {
      assertThatThrownBy {
        staffService.getStaffDetail(null)
      }.isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `test staff that exist is added to cache`() {
      val code = staffService.getStaffDetail(-1L)
      assertThat(code).isNotNull

      // calling twice should only result in one call to the repository
      staffService.getStaffDetail(-1L)

      verify(staffRepository).findByStaffId(-1L)
    }
  }

  @Nested
  inner class findAgenciesByUsername_cache {
    @Test
    fun `test staff that doesn't exist won't cause cache to fall over in a heap`() {
      val agencies = agencyService.findAgenciesByUsername("NOT_EXISTS", false)
      assertThat(agencies).isEmpty()
    }

    @Test
    fun `test staff not present won't cause cache to fall over in a heap`() {
      val agencies = agencyService.findAgenciesByUsername(null, false)
      assertThat(agencies).isEmpty()
    }

    @Test
    fun `test staff that is blank won't cause cache to fall over in a heap`() {
      val agencies = agencyService.findAgenciesByUsername(" ", false)
      assertThat(agencies).isEmpty()
    }

    @Test
    fun `test staff that exist is added to cache`() {
      val agencies = agencyService.findAgenciesByUsername("ITAG_USER", false)
      assertThat(agencies).isNotNull

      // calling twice should only result in one call to the repository
      agencyService.findAgenciesByUsername("ITAG_USER", false)

      verify(agencyRepository).findAgenciesByUsername("ITAG_USER", false)
    }
  }

  @Nested
  inner class getAgencyLocationsBooked_cache {
    @Test
    fun `test staff that doesn't exist won't cause cache to fall over in a heap`() {
      val agencies = agencyResource.getAgencyEventLocationsBooked("NOT_EXISTS", LocalDate.now(), null)
      assertThat(agencies).isEmpty()
    }

    @Test
    fun `test staff that is null won't cause cache to fall over in a heap`() {
      val agencies = agencyResource.getAgencyEventLocationsBooked(null, LocalDate.now(), null)
      assertThat(agencies).isEmpty()
    }

    @Test
    fun `test staff that is blank won't cause cache to fall over in a heap`() {
      val agencies = agencyResource.getAgencyEventLocationsBooked("  ", LocalDate.now(), null)
      assertThat(agencies).isEmpty()
    }

    @Test
    fun `test staff that exist is added to cache`() {
      val agencies = agencyResource.getAgencyEventLocationsBooked("MDI", LocalDate.now(), null)
      assertThat(agencies).isNotNull

      // calling twice should only result in one call to the repository
      agencyResource.getAgencyEventLocationsBooked("MDI", LocalDate.now(), null)

      verify(agencyService).getAgencyEventLocationsBooked("MDI", LocalDate.now(), null)
    }
  }
}
