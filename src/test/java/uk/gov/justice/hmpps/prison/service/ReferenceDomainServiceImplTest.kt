package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.mockito.ArgumentMatchers.eq
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.repository.ReferenceDataRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceDomainRepository
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReferenceDomainServiceImplTest {
  private val repository: ReferenceDataRepository = mock()
  private val referenceDomainRepository: ReferenceDomainRepository = mock()
  private val profileCodeRepository: ProfileCodeRepository = mock()
  private val profileTypeRepository: ProfileTypeRepository = mock()
  private lateinit var service: ReferenceDomainService

  @BeforeEach
  internal fun setUp() {
    MockitoAnnotations.initMocks(repository)
    service = ReferenceDomainService(
      repository,
      referenceDomainRepository,
      profileCodeRepository,
      profileTypeRepository,
    )
  }

  @Test
  internal fun testScheduleReasons() {
    whenever(
      repository.getReferenceCodeByDomainAndCode(
        ReferenceDomain.INTERNAL_SCHEDULE_TYPE.domain,
        "APP",
        false,
      ),
    )
      .thenReturn(
        Optional.of(
          ReferenceCode.builder()
            .domain(ReferenceDomain.INTERNAL_SCHEDULE_TYPE.domain)
            .build(),
        ),
      )

    whenever(repository.getScheduleReasons(eq("APP")))
      .thenReturn(
        listOf(
          ReferenceCode.builder().code("CODE1").description("HELLO To something").build(),
          ReferenceCode.builder().code("CODE2").description("goodbye To love").build(),
          ReferenceCode.builder().code("CODE3").description("apple").build(),
          ReferenceCode.builder().code("CODE4").description("zebra").build(),
          ReferenceCode.builder().code("CODE5").description("TReVOR").build(),
          ReferenceCode.builder().code("CODE6").description("COMPUTERS").build(),
          ReferenceCode.builder().code("CODE7").description("AGE UK MEETINGS").build(),
        ),
      )

    val scheduleReasons = service.getScheduleReasons("APP")

    assertThat(scheduleReasons).hasSize(7)
    assertThat(scheduleReasons[0].description).isEqualTo("Age UK Meetings")
    assertThat(scheduleReasons[1].description).isEqualTo("Apple")
    assertThat(scheduleReasons[2].description).isEqualTo("Computers")
    assertThat(scheduleReasons[3].description).isEqualTo("Goodbye To Love")
    assertThat(scheduleReasons[4].description).isEqualTo("Hello To Something")
    assertThat(scheduleReasons[5].description).isEqualTo("Trevor")
    assertThat(scheduleReasons[6].description).isEqualTo("Zebra")
  }

  @Test
  internal fun testReferenceCodeIsActive() {
    whenever(repository.getReferenceCodeByDomainAndCode("HDC_APPROVE", "APPROVED", false))
      .thenReturn(Optional.of(ReferenceCode.builder().activeFlag("Y").build()))
    assertThat(service.isReferenceCodeActive("HDC_APPROVE", "APPROVED")).isTrue()
  }

  @Test
  internal fun testReferenceCodeIsNotActive() {
    whenever(repository.getReferenceCodeByDomainAndCode("HDC_APPROVE", "DISABLED", false))
      .thenReturn(Optional.of(ReferenceCode.builder().activeFlag("N").build()))
    assertThat(service.isReferenceCodeActive("HDC_APPROVE", "DISABLED")).isFalse()
  }

  @Test
  internal fun testReferenceCodeIsNotPresent() {
    whenever(repository.getReferenceCodeByDomainAndCode("HDC_APPROVE", "APPROVED", false))
      .thenReturn(Optional.empty())
    assertThat(service.isReferenceCodeActive("HDC_APPROVE", "APPROVED")).isFalse()
  }

  @Nested
  inner class ReferenceOrProfileCode {

    val domain = "TEST"

    @Test
    internal fun `Find reference codes`() {
      whenever(repository.getReferenceDomain(domain)).thenReturn(
        Optional.of(
          uk.gov.justice.hmpps.prison.api.model.ReferenceDomain(),
        ),
      )
      whenever(repository.getReferenceCodesByDomain(domain, "code", Order.ASC)).thenReturn(
        listOf(
          ReferenceCode.builder().code("CODE1").domain(domain).description("Description 1").activeFlag("Y").build(),
          ReferenceCode.builder().code("CODE2").domain(domain).description("Description 2").activeFlag("Y").build(),
          ReferenceCode.builder().code("CODE3").domain(domain).description("Inactive Description").activeFlag("Y").build(),
        ),
      )

      assertThat(service.getReferenceOrProfileCodesByDomain(domain)).containsExactly(
        ReferenceCode.builder().code("CODE1").domain(domain).description("Description 1").activeFlag("Y").build(),
        ReferenceCode.builder().code("CODE2").domain(domain).description("Description 2").activeFlag("Y").build(),
        ReferenceCode.builder().code("CODE3").domain(domain).description("Inactive Description").activeFlag("Y").build(),
      )
      verifyNoInteractions(profileCodeRepository, profileTypeRepository)
    }

    @Test
    internal fun `Find profile codes`() {
      val profileType = ProfileType.builder().type(domain).build()
      whenever(repository.getReferenceDomain(domain)).thenReturn(Optional.empty())
      whenever(profileTypeRepository.findById(domain)).thenReturn(Optional.of(profileType))
      whenever(profileCodeRepository.findByProfileType(profileType)).thenReturn(
        listOf(
          ProfileCode.builder().id(ProfileCode.PK(profileType, "CODE1")).description("Description 1").listSequence(99).active(true).build(),
          ProfileCode.builder().id(ProfileCode.PK(profileType, "CODE2")).description("Description 2").listSequence(99).active(true).build(),
          ProfileCode.builder().id(ProfileCode.PK(profileType, "CODE3")).description("Inactive Description").listSequence(99).active(false).build(),
        ),
      )

      assertThat(service.getReferenceOrProfileCodesByDomain(domain)).containsExactly(
        ReferenceCode.builder().code("CODE1").domain(domain).description("Description 1").listSeq(99).activeFlag("Y").build(),
        ReferenceCode.builder().code("CODE2").domain(domain).description("Description 2").listSeq(99).activeFlag("Y").build(),
        ReferenceCode.builder().code("CODE3").domain(domain).description("Inactive Description").listSeq(99).activeFlag("N").build(),
      )
    }

    @ParameterizedTest
    @NullAndEmptySource
    internal fun `Throws exception when the domain is missing or blank`(inputDomain: String?) {
      assertThrows<EntityNotFoundException> { assertThat(service.getReferenceOrProfileCodesByDomain(domain)) }
    }

    @Test
    internal fun `Throws exception when domain is not valid`() {
      whenever(repository.getReferenceDomain(domain)).thenReturn(Optional.empty())
      whenever(profileTypeRepository.findById(domain)).thenReturn(Optional.empty())

      assertThrows<EntityNotFoundException> { assertThat(service.getReferenceOrProfileCodesByDomain(domain)) }
    }
  }
}
