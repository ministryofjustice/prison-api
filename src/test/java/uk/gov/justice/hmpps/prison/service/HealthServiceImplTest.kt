package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anySet
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.CreatePersonalCareNeed
import uk.gov.justice.hmpps.prison.api.model.PersonalCareCounterDto
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeeds
import uk.gov.justice.hmpps.prison.repository.InmateRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.HealthProblemType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderHealthProblem
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderHealthProblemRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class HealthServiceImplTest {
  private val inmateRepository: InmateRepository = mock()
  private val healthProblemCodeReferenceCodeRepository: ReferenceCodeRepository<HealthProblemCode> = mock()
  private val healthProblemStatusReferenceCodeRepository: ReferenceCodeRepository<HealthProblemStatus> = mock()
  private val offenderHealthProblemRepository: OffenderHealthProblemRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val bookingService: BookingService = mock()

  private var serviceToTest = HealthService(
    offenderBookingRepository,
    inmateRepository,
    healthProblemCodeReferenceCodeRepository,
    healthProblemStatusReferenceCodeRepository,
    offenderHealthProblemRepository,
    bookingService,
    100,
  )

  @Test
  @DisplayName("get personal care needs by problem type and subtype")
  fun getPersonalCareNeedsByProblemTypeAndSubtype() {
    val problemTypes = listOf("DISAB+RM", "DISAB+RC", "MATSTAT")
    val personalCareNeedsAll = listOf(
      PersonalCareNeed.builder().problemType("DISAB").problemCode("MI").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
      PersonalCareNeed.builder().problemType("DISAB").problemCode("RM").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
      PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
    )
    val personalCareNeeds = PersonalCareNeeds(
      listOf(
        PersonalCareNeed.builder().problemType("DISAB").problemCode("RM").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
        PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
      ),
    )

    whenever(inmateRepository.findPersonalCareNeeds(ArgumentMatchers.anyLong(), anySet())).thenReturn(personalCareNeedsAll)

    val response = serviceToTest.getPersonalCareNeeds(1L, problemTypes)

    verify(inmateRepository).findPersonalCareNeeds(1L, setOf("DISAB", "MATSTAT"))
    assertThat(response).isEqualTo(personalCareNeeds)
  }

  @Test
  @DisplayName("get personal care needs split by offender")
  fun getPersonalCareNeedsSplitByOffender() {
    val problemTypes = listOf("DISAB+RM", "DISAB+RC", "MATSTAT")

    val aaMat = PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9")
      .startDate(LocalDate.parse("2010-06-21")).offenderNo("A1234AA").build()
    val aaDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RM")
      .startDate(LocalDate.parse("2010-06-21")).offenderNo("A1234AA").build()
    val abDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RC")
      .startDate(LocalDate.parse("2010-06-22")).offenderNo("A1234AB").build()
    val acDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RM")
      .startDate(LocalDate.parse("2010-06-22")).offenderNo("A1234AC").build()
    val adDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("ND")
      .startDate(LocalDate.parse("2010-06-24")).offenderNo("A1234AD").build()

    whenever(inmateRepository.findPersonalCareNeeds(anyList(), anySet())).thenReturn(
      listOf(aaMat, aaDisab, abDisab, acDisab, adDisab),
    )

    val response = serviceToTest.getPersonalCareNeeds(listOf("A1234AA"), problemTypes)

    verify(inmateRepository).findPersonalCareNeeds(listOf("A1234AA"), setOf("DISAB", "MATSTAT"))
    assertThat(response).containsExactly(
      PersonalCareNeeds("A1234AA", listOf(aaMat, aaDisab)),
      PersonalCareNeeds("A1234AB", listOf(abDisab)),
      PersonalCareNeeds("A1234AC", listOf(acDisab)),
    )
  }

  @DisplayName("count personal care needs between dates split by offender")
  @Test
  fun countPersonalCareNeedsSplitByOffender() {
    val problemType = "DISAB"

    val abDisab = OffenderHealthProblem.builder()
      .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AB").build()).build())
      .caseloadType("CASELOAD_TYPE")
      .problemType(HealthProblemType(problemType, "Desc"))
      .startDate(LocalDate.parse("2022-06-21"))
      .build()

    val aaDisab1 = OffenderHealthProblem.builder()
      .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
      .caseloadType("CASELOAD_TYPE")
      .problemType(HealthProblemType(problemType, "Desc"))
      .startDate(LocalDate.parse("2022-06-21"))
      .build()

    val aaDisab2 = OffenderHealthProblem.builder()
      .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
      .caseloadType("CASELOAD_TYPE")
      .problemType(HealthProblemType(problemType, "Desc"))
      .startDate(LocalDate.parse("2022-06-22"))
      .build()
    val aaDisab3 = OffenderHealthProblem.builder()
      .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
      .caseloadType("CASELOAD_TYPE")
      .problemType(HealthProblemType(problemType, "Desc"))
      .startDate(LocalDate.parse("2022-06-22"))
      .build()
    val aaDisab4 = OffenderHealthProblem.builder()
      .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234AA").build()).build())
      .caseloadType("CASELOAD_TYPE")
      .problemType(HealthProblemType(problemType, "Desc"))
      .startDate(LocalDate.parse("2022-06-22"))
      .build()

    whenever(offenderHealthProblemRepository.findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateBetween(anyList(), any(), any(), any(), any())).thenReturn(
      listOf(abDisab, aaDisab1, aaDisab2, aaDisab3, aaDisab4),
    )

    val response = serviceToTest.countPersonalCareNeedsByOffenderNoAndProblemTypeBetweenDates(
      listOf("A1234AA", "A1234AB"),
      problemType,
      LocalDate.of(2022, 2, 2),
      LocalDate.of(2022, 3, 3),
    )

    verify(offenderHealthProblemRepository).findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateBetween(
      listOf("A1234AA", "A1234AB"),
      1,
      problemType,
      LocalDate.of(2022, 2, 2),
      LocalDate.of(2022, 3, 3),
    )
    assertThat(response).containsExactly(
      PersonalCareCounterDto("A1234AA", 4),
      PersonalCareCounterDto("A1234AB", 1),
    )
  }

  @Nested
  internal inner class AddPersonalCareNeed {
    private val booking: OffenderBooking = OffenderBooking.builder()
      .bookingId(1L)
      .active(true)
      .bookingSequence(1)
      .location(AgencyLocation.builder().id("MDI").type(AgencyLocationType.PRISON_TYPE).build())
      .offender(Offender.builder().nomsId("any noms id").build())
      .build()
    private val notActiveBooking: OffenderBooking = OffenderBooking.builder()
      .bookingId(1L)
      .active(false)
      .bookingSequence(1)
      .location(AgencyLocation.builder().id("MDI").type(AgencyLocationType.PRISON_TYPE).build())
      .offender(Offender.builder().nomsId("any noms id").build())
      .build()

    private val personalCareNeed: CreatePersonalCareNeed = CreatePersonalCareNeed.builder()
      .problemCode("D")
      .problemStatus("ON")
      .commentText("Disability")
      .startDate(LocalDate.of(2021, 1, 1))
      .endDate(LocalDate.of(2022, 9, 28))
      .build()

    @DisplayName("add personal care need")
    @Test
    fun canAddPersonalCareNeed() {
      whenever(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(booking))
      whenever(healthProblemCodeReferenceCodeRepository.findById(HealthProblemCode.pk("D"))).thenReturn(Optional.of(Companion.PROBLEM_CODE))
      whenever(healthProblemStatusReferenceCodeRepository.findById(HealthProblemStatus.pk("ON"))).thenReturn(Optional.of(Companion.PROBLEM_STATUS))

      serviceToTest.addPersonalCareNeed(1L, personalCareNeed)
      assertThat(booking.offenderHealthProblems[0]).usingRecursiveComparison().isEqualTo(
        OffenderHealthProblem
          .builder()
          .caseloadType("INST")
          .commentText("Disability")
          .offenderBooking(booking)
          .problemCode(Companion.PROBLEM_CODE)
          .problemType(Companion.PROBLEM_TYPE)
          .problemStatus(Companion.PROBLEM_STATUS)
          .startDate(LocalDate.of(2021, 1, 1))
          .endDate(LocalDate.of(2022, 9, 28))
          .build(),
      )
    }

    @DisplayName("booking id is invalid")
    @Test
    fun invalidOffenderBookingId() {
      whenever(offenderBookingRepository.findById(1L)).thenReturn(Optional.empty())

      assertThatThrownBy { serviceToTest.addPersonalCareNeed(1L, personalCareNeed) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Resource with id [1] not found.")
    }

    @DisplayName("problem status is invalid")
    @Test
    fun invalidProblemStatus() {
      whenever(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(booking))
      whenever(healthProblemCodeReferenceCodeRepository.findById(HealthProblemCode.pk("D"))).thenReturn(Optional.of(Companion.PROBLEM_CODE))
      whenever(healthProblemStatusReferenceCodeRepository.findById(HealthProblemStatus.pk("ON"))).thenReturn(Optional.empty())

      assertThatThrownBy { serviceToTest.addPersonalCareNeed(1L, personalCareNeed) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Resource with id [ON] not found.")
    }

    @DisplayName("problem code is missing")
    @Test
    fun missingProblemCode() {
      whenever(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(booking))
      whenever(healthProblemCodeReferenceCodeRepository.findById(HealthProblemCode.pk("D"))).thenReturn(Optional.empty())

      assertThatThrownBy { serviceToTest.addPersonalCareNeed(1L, personalCareNeed) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Resource with id [D] not found.")
    }

    @DisplayName("not active booking")
    @Test
    fun notActiveBooking() {
      whenever(offenderBookingRepository.findById(1L)).thenReturn(Optional.of(notActiveBooking))
      assertThatThrownBy { serviceToTest.addPersonalCareNeed(1L, personalCareNeed) }
        .isInstanceOf(BadRequestException::class.java)
    }
  }
  companion object {
    private val PROBLEM_TYPE = HealthProblemType("DISAB", null)
    private val PROBLEM_CODE = HealthProblemCode("D", null, PROBLEM_TYPE)

    private val PROBLEM_STATUS = HealthProblemStatus("ON", null)
  }
}
