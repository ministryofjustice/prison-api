package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.LatestTusedData
import uk.gov.justice.hmpps.prison.api.model.OffenderCalculatedKeyDates
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyDates
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepository
import uk.gov.justice.hmpps.prison.repository.SentenceCalculationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class OffenderDatesServiceTest {
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()

  private val sentenceCalculationRepository: SentenceCalculationRepository = mock()

  private val offenderCurfewRepository: OffenderCurfewRepository = mock()

  private val bookingRepository: BookingRepository = mock()

  private val telemetryClient: TelemetryClient = mock()

  private val clock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

  private var service: OffenderDatesService = OffenderDatesService(
    sentenceCalculationRepository,
    offenderBookingRepository,
    staffUserAccountRepository,
    telemetryClient,
    clock,
  )

  @Test
  fun updateOffenderDates_happy_path() {
    // Given
    val bookingId = 1L
    val offenderBooking = OffenderBooking.builder().bookingId(bookingId).build()
    whenever(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking))
    val submissionUser = "staff"
    val staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build()
    val staffUserAccount = StaffUserAccount.builder().username(submissionUser).staff(staff).build()
    whenever(staffUserAccountRepository.findById(submissionUser)).thenReturn(
      Optional.of(staffUserAccount),
    )
    val calculationUuid = UUID.randomUUID()
    val calculationDateTime = LocalDateTime.of(2021, 11, 17, 11, 0)
    val payload = RequestToUpdateOffenderDates.builder()
      .keyDates(createOffenderKeyDates())
      .submissionUser(submissionUser)
      .calculationDateTime(calculationDateTime)
      .calculationUuid(calculationUuid)
      .build()
    val keyDates = payload.keyDates

    // When
    service.updateOffenderKeyDates(bookingId, payload)

    // Then
    val expected = SentenceCalculation.builder()
      .offenderBooking(offenderBooking)
      .reasonCode("UPDATE")
      .calculationDate(calculationDateTime)
      .comments("The information shown was calculated using the Calculate Release Dates service. The calculation ID is: $calculationUuid")
      .staff(staff)
      .recordedDateTime(calculationDateTime)
      .recordedUser(staffUserAccount)
      .hdcedCalculatedDate(keyDates.homeDetentionCurfewEligibilityDate)
      .etdCalculatedDate(keyDates.earlyTermDate)
      .mtdCalculatedDate(keyDates.midTermDate)
      .ltdCalculatedDate(keyDates.lateTermDate)
      .dprrdCalculatedDate(keyDates.dtoPostRecallReleaseDate)
      .ardCalculatedDate(keyDates.automaticReleaseDate)
      .crdCalculatedDate(keyDates.conditionalReleaseDate)
      .pedCalculatedDate(keyDates.paroleEligibilityDate)
      .npdCalculatedDate(keyDates.nonParoleDate)
      .ledCalculatedDate(keyDates.licenceExpiryDate)
      .prrdCalculatedDate(keyDates.postRecallReleaseDate)
      .sedCalculatedDate(keyDates.sentenceExpiryDate)
      .tusedCalculatedDate(keyDates.topupSupervisionExpiryDate)
      .ersedOverridedDate(keyDates.earlyRemovalSchemeEligibilityDate)
      .effectiveSentenceEndDate(keyDates.effectiveSentenceEndDate)
      .effectiveSentenceLength(keyDates.sentenceLength)
      .judiciallyImposedSentenceLength(keyDates.sentenceLength)
      .build()

    assertThat(offenderBooking.sentenceCalculations).containsOnly(expected)
    assertThat(offenderBooking.latestCalculation).isEqualTo(Optional.of(expected))
    Mockito.verify(telemetryClient).trackEvent(
      "OffenderKeyDatesUpdated",
      mapOf(
        "bookingId" to bookingId.toString(),
        "calculationUuid" to calculationUuid.toString(),
        "submissionUser" to submissionUser,
      ),
      null,
    )
  }

  @Test
  fun updateOffenderDates_happy_path_default_calculation_date() {
    // Given
    val bookingId = 1L
    val offenderBooking = OffenderBooking.builder().bookingId(bookingId).build()
    whenever(offenderBookingRepository.findById(bookingId))
      .thenReturn(Optional.of(offenderBooking))
    val staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build()
    val staffUserAccount = StaffUserAccount.builder().username("staff").staff(staff).build()
    whenever(staffUserAccountRepository.findById("staff"))
      .thenReturn(Optional.of(staffUserAccount))
    val calculationUuid = UUID.randomUUID()
    val payload = RequestToUpdateOffenderDates.builder()
      .keyDates(createOffenderKeyDates())
      .submissionUser("staff")
      .calculationUuid(calculationUuid)
      .build()

    // When
    service.updateOffenderKeyDates(bookingId, payload)

    // Then
    val defaultedCalculationDate = LocalDateTime.now(clock)
    val latestCalculation = offenderBooking.latestCalculation.get()
    assertThat(latestCalculation.calculationDate).isEqualTo(defaultedCalculationDate)
    assertThat(latestCalculation.recordedDateTime).isEqualTo(defaultedCalculationDate)
  }

  @Test
  fun updateOffendDates_no_dates_to_store() {
    val bookingId = 1L
    val offenderBooking = OffenderBooking.builder().bookingId(bookingId).build()
    whenever(offenderBookingRepository.findById(bookingId))
      .thenReturn(Optional.of(offenderBooking))
    val staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build()
    val staffUserAccount = StaffUserAccount.builder().username("staff").staff(staff).build()
    whenever(staffUserAccountRepository.findById("staff"))
      .thenReturn(Optional.of(staffUserAccount))
    val calculationUuid = UUID.randomUUID()
    val payload = RequestToUpdateOffenderDates.builder()
      .keyDates(OffenderKeyDates())
      .noDates(true)
      .submissionUser("staff")
      .calculationUuid(calculationUuid)
      .build()

    // When
    service.updateOffenderKeyDates(bookingId, payload)

    // Then
    val latestCalculation = offenderBooking.latestCalculation.get()
    assertThat(latestCalculation).hasAllNullFieldsOrPropertiesExcept(
      "offenderBooking",
      "hdcEligible",
      "calculationDate",
      "comments",
      "staff",
      "reasonCode",
      "recordedDateTime",
      "recordedUser",
      "effectiveSentenceLength",
    )
  }

  @Test
  fun updateOffenderDates_exception_for_unknown_booking() {
    // Given
    whenever(offenderBookingRepository.findById(-1L))
      .thenReturn(Optional.empty())

    // Then
    assertThatThrownBy {
      service.updateOffenderKeyDates(
        -1L,
        RequestToUpdateOffenderDates.builder().build(),
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Resource with id [-1] not found.")
  }

  @Test
  fun updateOffenderDates_exception_for_unknown_staff() {
    // Given
    val bookingId = 1L
    val offenderBooking = OffenderBooking.builder().bookingId(bookingId).build()
    whenever(offenderBookingRepository.findById(bookingId)).thenReturn(Optional.of(offenderBooking))
    val staff = "staff"
    whenever(staffUserAccountRepository.findById(staff))
      .thenReturn(Optional.empty())

    // Then
    assertThatThrownBy {
      service.updateOffenderKeyDates(
        bookingId,
        RequestToUpdateOffenderDates.builder().submissionUser(staff).build(),
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Resource with id [staff] not found.")
  }

  @Test
  fun getOffenderKeyDates_all_data_by_offender_sent_calc_id() {
    // Given
    val offenderSentCalcId = 1L

    val sentenceCalculation =
      SentenceCalculation.builder()
        .id(1L)
        .hdcedCalculatedDate(LocalDate.of(2021, 11, 1))
        .etdCalculatedDate(LocalDate.of(2021, 11, 2))
        .mtdCalculatedDate(LocalDate.of(2021, 11, 3))
        .ltdCalculatedDate(LocalDate.of(2021, 11, 4))
        .dprrdCalculatedDate(LocalDate.of(2021, 11, 5))
        .ardCalculatedDate(LocalDate.of(2021, 11, 6))
        .crdCalculatedDate(LocalDate.of(2021, 11, 7))
        .pedCalculatedDate(LocalDate.of(2021, 11, 8))
        .npdCalculatedDate(LocalDate.of(2021, 11, 9))
        .ledCalculatedDate(LocalDate.of(2021, 11, 10))
        .prrdCalculatedDate(LocalDate.of(2021, 11, 11))
        .sedCalculatedDate(LocalDate.of(2021, 11, 12))
        .tusedCalculatedDate(LocalDate.of(2021, 11, 13))
        .effectiveSentenceEndDate(LocalDate.of(2021, 11, 14))
        .effectiveSentenceLength("11/00/11")
        .ersedOverridedDate(LocalDate.of(2021, 11, 15))
        .hdcadOverridedDate(LocalDate.of(2021, 11, 16))
        .tariffOverridedDate(LocalDate.of(2021, 11, 17))
        .tersedOverridedDate(LocalDate.of(2021, 11, 18))
        .apdOverridedDate(LocalDate.of(2021, 11, 19))
        .rotlOverridedDate(LocalDate.of(2021, 11, 20))
        .judiciallyImposedSentenceLength("11/00/00")
        .comments("Comments")
        .reasonCode("NEW")
        .calculationDate(LocalDateTime.of(2021, 11, 8, 10, 0, 0))
        .build()
    whenever(sentenceCalculationRepository.findById(offenderSentCalcId))
      .thenReturn(
        Optional.of(sentenceCalculation),
      )

    // When
    val result = service.getOffenderKeyDatesByOffenderSentCalcId(offenderSentCalcId)

    // Then
    assertOffenderCalculatedKeyDates(result)
  }

  @Test
  fun getOffenderKeyDates_all_data_available() {
    // Given
    val bookingId = 1L

    val offenderBooking = OffenderBooking.builder()
      .bookingId(bookingId)
      .sentenceCalculations(
        listOf(
          SentenceCalculation.builder()
            .id(1L)
            .hdcedCalculatedDate(LocalDate.of(2021, 11, 1))
            .etdCalculatedDate(LocalDate.of(2021, 11, 2))
            .mtdCalculatedDate(LocalDate.of(2021, 11, 3))
            .ltdCalculatedDate(LocalDate.of(2021, 11, 4))
            .dprrdCalculatedDate(LocalDate.of(2021, 11, 5))
            .ardCalculatedDate(LocalDate.of(2021, 11, 6))
            .crdCalculatedDate(LocalDate.of(2021, 11, 7))
            .pedCalculatedDate(LocalDate.of(2021, 11, 8))
            .npdCalculatedDate(LocalDate.of(2021, 11, 9))
            .ledCalculatedDate(LocalDate.of(2021, 11, 10))
            .prrdCalculatedDate(LocalDate.of(2021, 11, 11))
            .sedCalculatedDate(LocalDate.of(2021, 11, 12))
            .tusedCalculatedDate(LocalDate.of(2021, 11, 13))
            .effectiveSentenceEndDate(LocalDate.of(2021, 11, 14))
            .effectiveSentenceLength("11/00/11")
            .ersedOverridedDate(LocalDate.of(2021, 11, 15))
            .hdcadOverridedDate(LocalDate.of(2021, 11, 16))
            .tariffOverridedDate(LocalDate.of(2021, 11, 17))
            .tersedOverridedDate(LocalDate.of(2021, 11, 18))
            .apdOverridedDate(LocalDate.of(2021, 11, 19))
            .rotlOverridedDate(LocalDate.of(2021, 11, 20))
            .judiciallyImposedSentenceLength("11/00/00")
            .comments("Comments")
            .reasonCode("NEW")
            .calculationDate(LocalDateTime.of(2021, 11, 8, 10, 0, 0))
            .build(),
        ),
      )
      .build()
    whenever(offenderBookingRepository.findById(bookingId))
      .thenReturn(Optional.of(offenderBooking))

    // When
    val result = service.getOffenderKeyDates(bookingId)

    // Then
    assertOffenderCalculatedKeyDates(result)
  }

  @Test
  fun updateOffenderDates_with_passed_in_comment_and_reason() {
    // Given
    val bookingId = 1L
    val offenderBooking = OffenderBooking.builder().bookingId(bookingId).build()
    whenever(offenderBookingRepository.findById(bookingId))
      .thenReturn(Optional.of(offenderBooking))
    val submissionUser = "staff"
    val staff = Staff.builder().staffId(2L).firstName("Other").lastName("Staff").build()
    val staffUserAccount = StaffUserAccount.builder().username(submissionUser).staff(staff).build()
    whenever(staffUserAccountRepository.findById(submissionUser)).thenReturn(
      Optional.of(staffUserAccount),
    )
    val calculationUuid = UUID.randomUUID()
    val calculationDateTime = LocalDateTime.of(2021, 11, 17, 11, 0)
    val payload = RequestToUpdateOffenderDates.builder()
      .keyDates(createOffenderKeyDates())
      .submissionUser(submissionUser)
      .calculationDateTime(calculationDateTime)
      .calculationUuid(calculationUuid)
      .comment("Passed in comment should override default")
      .reason("TRANSFER")
      .build()
    val keyDates = payload.keyDates

    // When
    service.updateOffenderKeyDates(bookingId, payload)

    // Then
    val expected = SentenceCalculation.builder()
      .offenderBooking(offenderBooking)
      .reasonCode("TRANSFER")
      .calculationDate(calculationDateTime)
      .comments("Passed in comment should override default")
      .staff(staff)
      .recordedDateTime(calculationDateTime)
      .recordedUser(staffUserAccount)
      .hdcedCalculatedDate(keyDates.homeDetentionCurfewEligibilityDate)
      .etdCalculatedDate(keyDates.earlyTermDate)
      .mtdCalculatedDate(keyDates.midTermDate)
      .ltdCalculatedDate(keyDates.lateTermDate)
      .dprrdCalculatedDate(keyDates.dtoPostRecallReleaseDate)
      .ardCalculatedDate(keyDates.automaticReleaseDate)
      .crdCalculatedDate(keyDates.conditionalReleaseDate)
      .pedCalculatedDate(keyDates.paroleEligibilityDate)
      .npdCalculatedDate(keyDates.nonParoleDate)
      .ledCalculatedDate(keyDates.licenceExpiryDate)
      .prrdCalculatedDate(keyDates.postRecallReleaseDate)
      .sedCalculatedDate(keyDates.sentenceExpiryDate)
      .tusedCalculatedDate(keyDates.topupSupervisionExpiryDate)
      .ersedOverridedDate(keyDates.earlyRemovalSchemeEligibilityDate)
      .effectiveSentenceEndDate(keyDates.effectiveSentenceEndDate)
      .effectiveSentenceLength(keyDates.sentenceLength)
      .judiciallyImposedSentenceLength(keyDates.sentenceLength)
      .build()

    assertThat(offenderBooking.sentenceCalculations).containsOnly(expected)
    assertThat(offenderBooking.latestCalculation)
      .isEqualTo(Optional.of(expected))
  }

  @Test
  fun findLatestTusedDataIsReturnedCorrectly() {
    val latestTusedData = LatestTusedData(LocalDate.of(2023, 1, 3), null, null, "A1234AA")
    whenever(
      offenderBookingRepository.findLatestTusedDataFromNomsId(
        ArgumentMatchers.anyString(),
      ),
    ).thenReturn(
      Optional.of(latestTusedData),
    )
    val returned = service.getLatestTusedDataFromNomsId("A1234AA")
    assertThat(returned.offenderNo).isEqualTo("A1234AA")
    assertThat(returned.latestTused).isEqualTo(LocalDate.of(2023, 1, 3))
  }

  @Test
  fun findLatestTusedDataThrowsExpectedException() {
    whenever(
      offenderBookingRepository.findLatestTusedDataFromNomsId(
        ArgumentMatchers.anyString(),
      ),
    ).thenReturn(
      Optional.empty(),
    )
    val exception: Throwable = assertThrows(
      EntityNotFoundException::class.java,
    ) { service.getLatestTusedDataFromNomsId("A1234AA") }
    assertThat(exception.message).isEqualTo("Resource with id [A1234AA] not found.")
  }

  companion object {
    private val NOV_11_2021: LocalDate = LocalDate.of(2021, 11, 8)
    private fun assertOffenderCalculatedKeyDates(result: OffenderCalculatedKeyDates) {
      // Then
      assertThat(result).isEqualTo(
        OffenderCalculatedKeyDates.offenderCalculatedKeyDates()
          .homeDetentionCurfewEligibilityDate(LocalDate.of(2021, 11, 1))
          .earlyTermDate(LocalDate.of(2021, 11, 2))
          .midTermDate(LocalDate.of(2021, 11, 3))
          .lateTermDate(LocalDate.of(2021, 11, 4))
          .dtoPostRecallReleaseDate(LocalDate.of(2021, 11, 5))
          .automaticReleaseDate(LocalDate.of(2021, 11, 6))
          .conditionalReleaseDate(LocalDate.of(2021, 11, 7))
          .paroleEligibilityDate(LocalDate.of(2021, 11, 8))
          .nonParoleDate(LocalDate.of(2021, 11, 9))
          .licenceExpiryDate(LocalDate.of(2021, 11, 10))
          .postRecallReleaseDate(LocalDate.of(2021, 11, 11))
          .sentenceExpiryDate(LocalDate.of(2021, 11, 12))
          .topupSupervisionExpiryDate(LocalDate.of(2021, 11, 13))
          .effectiveSentenceEndDate(LocalDate.of(2021, 11, 14))
          .sentenceLength("11/00/11")
          .earlyRemovalSchemeEligibilityDate(LocalDate.of(2021, 11, 15))
          .homeDetentionCurfewApprovedDate(LocalDate.of(2021, 11, 16))
          .tariffDate(LocalDate.of(2021, 11, 17))
          .tariffExpiredRemovalSchemeEligibilityDate(LocalDate.of(2021, 11, 18))
          .approvedParoleDate(LocalDate.of(2021, 11, 19))
          .releaseOnTemporaryLicenceDate(LocalDate.of(2021, 11, 20))
          .judiciallyImposedSentenceLength("11/00/00")
          .comment("Comments")
          .reasonCode("NEW")
          .calculatedAt(LocalDateTime.of(2021, 11, 8, 10, 0, 0))
          .build(),
      )
    }

    @JvmOverloads
    fun createOffenderKeyDates(
      homeDetentionCurfewEligibilityDate: LocalDate = NOV_11_2021,
      earlyTermDate: LocalDate = NOV_11_2021,
      midTermDate: LocalDate = NOV_11_2021,
      lateTermDate: LocalDate = NOV_11_2021,
      dtoPostRecallReleaseDate: LocalDate = NOV_11_2021,
      automaticReleaseDate: LocalDate = NOV_11_2021,
      conditionalReleaseDate: LocalDate = NOV_11_2021,
      paroleEligibilityDate: LocalDate = NOV_11_2021,
      nonParoleDate: LocalDate = NOV_11_2021,
      licenceExpiryDate: LocalDate = NOV_11_2021,
      postRecallReleaseDate: LocalDate = NOV_11_2021,
      sentenceExpiryDate: LocalDate = NOV_11_2021,
      topupSupervisionExpiryDate: LocalDate = NOV_11_2021,
      earlyRemovalSchemeEligibilityDate: LocalDate = NOV_11_2021,
      effectiveSentenceEndDate: LocalDate = NOV_11_2021,
      sentenceLength: String = "11/00/00",
    ): OffenderKeyDates = OffenderKeyDates.builder()
      .homeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate)
      .earlyTermDate(earlyTermDate)
      .midTermDate(midTermDate)
      .lateTermDate(lateTermDate)
      .dtoPostRecallReleaseDate(dtoPostRecallReleaseDate)
      .automaticReleaseDate(automaticReleaseDate)
      .conditionalReleaseDate(conditionalReleaseDate)
      .paroleEligibilityDate(paroleEligibilityDate)
      .nonParoleDate(nonParoleDate)
      .licenceExpiryDate(licenceExpiryDate)
      .postRecallReleaseDate(postRecallReleaseDate)
      .sentenceExpiryDate(sentenceExpiryDate)
      .topupSupervisionExpiryDate(topupSupervisionExpiryDate)
      .earlyRemovalSchemeEligibilityDate(earlyRemovalSchemeEligibilityDate)
      .effectiveSentenceEndDate(effectiveSentenceEndDate)
      .sentenceLength(sentenceLength)
      .build()
  }
}
