package uk.gov.justice.hmpps.prison.service.curfews

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.Agency
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus
import uk.gov.justice.hmpps.prison.api.model.BaseSentenceCalcDates
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalc
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalculation
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepository
import uk.gov.justice.hmpps.prison.service.BookingService
import uk.gov.justice.hmpps.prison.service.CaseloadToAgencyMappingService
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService
import uk.gov.justice.hmpps.prison.service.support.OffenderCurfew
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.streams.asSequence

class OffenderCurfewServiceTest {
  private val bookingService: BookingService = mock()
  private val offenderCurfewRepository: OffenderCurfewRepository = mock()
  private val caseloadToAgencyMappingService: CaseloadToAgencyMappingService = mock()
  private val referenceDomainService: ReferenceDomainService = mock()
  private val offenderCurfewService = OffenderCurfewService(
    offenderCurfewRepository,
    caseloadToAgencyMappingService,
    bookingService,
    referenceDomainService,
  )

  @Test
  fun givenTwoOffenderCurfewWhenComparedThenOrderingIsCorrect() {
    assertThat(
      compare(
        offenderCurfew(1, 1, null),
        offenderCurfew(2, 1, null),
      ),
    ).isEqualTo(-1)

    assertThat(
      compare(
        offenderCurfew(1, 1, null),
        offenderCurfew(1, 1, null),
      ),
    ).isEqualTo(0)

    assertThat(
      compare(
        offenderCurfew(2, 1, null),
        offenderCurfew(1, 1, null),
      ),
    ).isEqualTo(1)

    assertThat(
      compare(
        offenderCurfew(1, 1, "2018-01-01"),
        offenderCurfew(2, 1, "2018-01-01"),
      ),
    ).isEqualTo(-1)

    assertThat(
      compare(
        offenderCurfew(2, 1, "2018-01-01"),
        offenderCurfew(1, 1, "2018-01-01"),
      ),
    ).isEqualTo(1)

    assertThat(
      compare(
        offenderCurfew(2, 1, "2018-01-01"),
        offenderCurfew(1, 1, "2018-01-02"),
      ),
    ).isEqualTo(-1)

    assertThat(
      compare(
        offenderCurfew(2, 1, "2018-01-01"),
        offenderCurfew(1, 1, null),
      ),
    ).isEqualTo(-1)
  }

  @Test
  fun oscByHdcedComparator() {
    val d1 = LocalDate.of(2019, 1, 1)
    val d2 = LocalDate.of(2019, 1, 2)

    assertOscComparison(offenderSentenceCalc(), offenderSentenceCalc(), 0)
    assertOscComparison(offenderSentenceCalc(), offenderSentenceCalc(d1), 1)
    assertOscComparison(offenderSentenceCalc(d1), offenderSentenceCalc(), -1)
    assertOscComparison(offenderSentenceCalc(d1), offenderSentenceCalc(d1), 0)
    assertOscComparison(offenderSentenceCalc(d1), offenderSentenceCalc(d2), -1)
    assertOscComparison(offenderSentenceCalc(d2), offenderSentenceCalc(d1), 1)
    assertOscComparison(offenderSentenceCalc(null), offenderSentenceCalc(), 0)
  }

  private fun assertOscComparison(
    a: OffenderSentenceCalc<out BaseSentenceCalcDates>?,
    b: OffenderSentenceCalc<out BaseSentenceCalcDates>?,
    expected: Int,
  ) {
    assertThat(OffenderCurfewService.OSC_BY_HDCED_COMPARATOR.compare(a, b)).isEqualTo(expected)
  }

  @Test
  fun givenNoCurfewsForAgencyWhenFilteredForCurrentCurfewThenTheResultShouldBeEmpty() {
    assertThat(
      OffenderCurfewService.currentOffenderCurfews(listOf()),
    ).isEmpty()
  }

  @Test
  fun givenOneCurfewWhenFilteredForCurrentCurfewThenTheCurfewShouldBeReturned() {
    val curfew: OffenderCurfew = offenderCurfew(1, 2, null)
    assertThat(
      extractCurfewIds(
        OffenderCurfewService.currentOffenderCurfews(
          listOf(curfew),
        ),
      ),
    ).containsOnly(1L)
  }

  @Test
  fun givenTwoCurfewsForOneOffenderBookIdHavingNullAssessmentDateWhenFilteredForCurrentCurfewThenhigherOffenderCurfewIdWins() {
    val curfews = listOf(
      offenderCurfew(1, 1, null),
      offenderCurfew(2, 1, null),
    )

    assertThat(extractCurfewIds(OffenderCurfewService.currentOffenderCurfews(curfews)))
      .containsOnly(2L)
  }

  @Test
  fun givenTwoCurfewsForOneOffenderBookIdHavingSameAssessmentDateWhenFilteredForCurrentCurfewThenhigherOffenderCurfewIdWins() {
    val curfews = listOf(
      offenderCurfew(2, 1, "2018-05-01"),
      offenderCurfew(1, 1, "2018-05-01"),
    )

    assertThat(extractCurfewIds(OffenderCurfewService.currentOffenderCurfews(curfews)))
      .containsOnly(2L)
  }

  @Test
  fun givenTwoCurfewsForOneOffenderBookIdHavingDifferentAssessmentDatesWhenFilteredForCurrentCurfewThenhighestAssessmentDateWins() {
    val curfews = listOf(
      offenderCurfew(1, 1, "2018-05-02"),
      offenderCurfew(2, 1, "2018-05-01"),
    )

    assertThat(extractCurfewIds(OffenderCurfewService.currentOffenderCurfews(curfews)))
      .containsOnly(1L)
  }

  @Test
  fun givenTwoCurfewsForOneOffenderBookIdHavingDifferentWhenFilteredForCurrentCurfewThenhighestAssessmentDateWins() {
    val curfews = listOf(
      offenderCurfew(2, 1, "2018-05-01"),
      offenderCurfew(1, 1, null),
    )

    assertThat(extractCurfewIds(OffenderCurfewService.currentOffenderCurfews(curfews)))
      .containsOnly(1L)
  }

  @Test
  fun givenCurfewsForSeveralOffenderBookIdsWhenFilteredForCurrentCurfewTheHighestCurfewForEachOffenderBookIdIsRetained() {
    val curfews = listOf(
      offenderCurfew(1, 1, null),
      offenderCurfew(2, 1, null),

      offenderCurfew(3, 2, "2018-01-01"),
      offenderCurfew(4, 2, null),

      offenderCurfew(5, 3, "2018-01-01"),
      offenderCurfew(6, 3, "2018-01-01"),

      offenderCurfew(7, 4, "2018-01-01"),
      offenderCurfew(8, 4, "2018-01-02"),
      offenderCurfew(9, 4, "2018-01-01"),

    )

    assertThat(
      OffenderCurfewService.currentOffenderCurfews(curfews),
    )
      .containsOnly(
        offenderCurfew(2, 1, null),
        offenderCurfew(4, 2, null),
        offenderCurfew(6, 3, "2018-01-01"),
        offenderCurfew(8, 4, "2018-01-02"),
      )
  }

  @Test
  fun givenNoOffendersInAgencyThenNoResults() {
    whenever(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME))
      .thenReturn(
        agencyIdsToAgencies(AGENCY_ID),
      )
    assertThat(
      offenderCurfewService.getHomeDetentionCurfewCandidates(
        USERNAME,
      ),
    ).isEmpty()
  }

  @Test
  fun givenOffendersThenFilteredByHdcedPresent() {
    whenever(
      offenderCurfewRepository.offenderCurfews(setOf(AGENCY_ID)),
    ).thenReturn(
      listOf(
        offenderCurfew(1, 1, null),
        offenderCurfew(1, 2, null),
        offenderCurfew(1, 3, null),
        offenderCurfew(1, 4, null),
        offenderCurfew(1, 5, null),
      ),
    )

    whenever(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(USERNAME))
      .thenReturn(
        agencyIdsToAgencies(AGENCY_ID),
      )
    whenever(
      bookingService.getOffenderSentenceCalculationsForAgency(
        setOf(AGENCY_ID),
      ),
    ).thenReturn(offenderSentenceCalculations())

    val eligibleOffenders = offenderCurfewService.getHomeDetentionCurfewCandidates(USERNAME)

    assertThat(eligibleOffenders.map { it.bookingId }).containsExactly(1L, 2L, 3L, 4L)
  }

  @Test
  fun badApprovalStatus() {
    val rejectedStatus = "XXX"

    whenever(
      referenceDomainService.isReferenceCodeActive(
        eq("HDC_APPROVE"),
        anyString(),
      ),
    ).thenReturn(false)

    val badApprovalStatus = ApprovalStatus.builder().approvalStatus(rejectedStatus).build()

    assertThatThrownBy {
      offenderCurfewService.setApprovalStatus(
        1L,
        badApprovalStatus,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Approval status code 'XXX' is not a valid NOMIS value.")
  }

  @Test
  fun badRefusedReason() {
    val rejectedStatus = "REJECTED"
    val refusedReason = "XXX"

    whenever(referenceDomainService.isReferenceCodeActive("HDC_APPROVE", rejectedStatus))
      .thenReturn(true)
    whenever(
      referenceDomainService.isReferenceCodeActive(
        eq("HDC_REJ_RSN"),
        anyString(),
      ),
    ).thenReturn(false)

    val badApprovalStatus = ApprovalStatus.builder().approvalStatus(rejectedStatus).refusedReason(refusedReason).build()

    assertThatThrownBy {
      offenderCurfewService.setApprovalStatus(
        1L,
        badApprovalStatus,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Refused reason code 'XXX' is not a valid NOMIS value.")
  }

  @Test
  fun getBatchLatestHdcStatusByBookingIds() {
    val bookingIds = listOf(1L, 2L, 3L, 4L)
    whenever(
      offenderCurfewRepository.getBatchLatestHomeDetentionCurfew(
        bookingIds,
        StatusTrackingCodes.REFUSED_REASON_CODES,
      ),
    )
      .thenReturn(
        listOf(
          HomeDetentionCurfew.builder().id(1L).passed(true).approvalStatus("REJECTED").build(),
          HomeDetentionCurfew.builder().id(2L).passed(false).build(),
          HomeDetentionCurfew.builder().id(3L).build(),
        ),
      )

    val curfews = offenderCurfewService.getBatchLatestHomeDetentionCurfew(bookingIds)

    assertThat(curfews.map { it.id })
      .containsExactly(1L, 2L, 3L)
  }

  @Test
  fun getBatchLatestHdcStatusByBookingIdsEmpty() {
    val bookingIds = listOf(1L, 2L, 3L, 4L)
    whenever(
      offenderCurfewRepository.getBatchLatestHomeDetentionCurfew(
        bookingIds,
        StatusTrackingCodes.REFUSED_REASON_CODES,
      ),
    )
      .thenReturn(listOf())
    val curfews = offenderCurfewService.getBatchLatestHomeDetentionCurfew(bookingIds)
    assertThat(curfews.map { it.id })
      .hasSize(0)
  }

  private fun offenderSentenceCalculations(): List<OffenderSentenceCalculation> = listOf(
    offenderSentenceDetail(1L, TODAY.plusDays((CUTOFF_DAYS_OFFSET - 2).toLong()), null, HDCED),
    offenderSentenceDetail(2L, TODAY.plusDays((CUTOFF_DAYS_OFFSET - 1).toLong()), null, HDCED),
    offenderSentenceDetail(3L, TODAY.plusDays(CUTOFF_DAYS_OFFSET.toLong()), null, HDCED),
    offenderSentenceDetail(4L, TODAY.plusDays((CUTOFF_DAYS_OFFSET + 1).toLong()), null, HDCED),
    offenderSentenceDetail(5L, TODAY.plusDays((CUTOFF_DAYS_OFFSET + 2).toLong()), null, null),
  )

  private fun offenderSentenceDetail(
    bookingId: Long?,
    automaticReleaseDate: LocalDate?,
    conditionalReleaseDate: LocalDate?,
    homeDetentionCurfewEligibilityDate: LocalDate?,
  ): OffenderSentenceCalculation = OffenderSentenceCalculation.builder()
    .offenderNo("A1234BC")
    .firstName("John")
    .lastName("Smith")
    .agencyLocationId("MDI")
    .bookingId(bookingId)
    .automaticReleaseDate(automaticReleaseDate)
    .conditionalReleaseDate(conditionalReleaseDate)
    .homeDetCurfEligibilityDate(homeDetentionCurfewEligibilityDate)
    .build()

  private fun offenderSentenceCalc(): OffenderSentenceCalc<out BaseSentenceCalcDates> = OffenderSentenceCalc(
    bookingId = 1,
    offenderNo = "A1234BC",
    firstName = "John",
    lastName = "Smith",
    agencyLocationId = "MDI",
    sentenceDetail = BaseSentenceCalcDates.builder().build(),
  )

  private fun offenderSentenceCalc(hdced: LocalDate?): OffenderSentenceCalc<out BaseSentenceCalcDates> = OffenderSentenceCalc(
    bookingId = 1,
    offenderNo = "A1234BC",
    firstName = "John",
    lastName = "Smith",
    agencyLocationId = "MDI",
    sentenceDetail =
    BaseSentenceCalcDates
      .builder()
      .homeDetentionCurfewEligibilityDate(hdced)
      .build(),
  )

  companion object {
    private const val AGENCY_ID = "LEI"
    private const val USERNAME = "ERIC"
    private const val CUTOFF_DAYS_OFFSET = 28

    private val TODAY: LocalDate = LocalDate.of(2017, 6, 15)
    private val HDCED: LocalDate = LocalDate.of(2018, 12, 12)

    private fun compare(a: OffenderCurfew?, b: OffenderCurfew?): Int = OffenderCurfewService.OFFENDER_CURFEW_COMPARATOR.compare(a, b)

    private fun offenderCurfew(offenderCurfewId: Long, offenderBookId: Long, assessmentDate: String?): OffenderCurfew = OffenderCurfew
      .builder()
      .offenderCurfewId(offenderCurfewId)
      .offenderBookId(offenderBookId)
      .assessmentDate(toLocalDate(assessmentDate))
      .build()

    private fun toLocalDate(string: String?): LocalDate? {
      if (string == null) return null
      return LocalDate.parse(string)
    }

    private fun extractCurfewIds(curfews: Stream<OffenderCurfew>): List<Long> = curfews.asSequence().map { it.offenderCurfewId }.toList()

    private fun agencyIdsToAgencies(vararg agencyIds: String?): List<Agency> = agencyIds.map {
      Agency
        .builder()
        .agencyId(it)
        .build()
    }
  }
}
