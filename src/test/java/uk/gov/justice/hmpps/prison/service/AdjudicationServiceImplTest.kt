package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.Agency
import uk.gov.justice.hmpps.prison.api.model.Location
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence
import uk.gov.justice.hmpps.prison.api.model.adjudications.Hearing
import uk.gov.justice.hmpps.prison.api.model.adjudications.OffenderAdjudicationHearing
import uk.gov.justice.hmpps.prison.api.support.Page
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import uk.gov.justice.hmpps.prison.repository.AdjudicationsRepository
import uk.gov.justice.hmpps.prison.repository.AgencyRepository
import uk.gov.justice.hmpps.prison.repository.LocationRepository
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class AdjudicationServiceImplTest {
  private val adjudicationsRepository: AdjudicationsRepository = mock()
  private val locationRepository: LocationRepository = mock()
  private val agencyRepository: AgencyRepository = mock()
  private var adjudicationService: AdjudicationService = AdjudicationService(adjudicationsRepository, agencyRepository, locationRepository, BATCH_SIZE)

  @Test
  fun findAdjudications() {
    val adjudication = Adjudication.builder().build()
    val expectedResult = Page(listOf(adjudication), 1, PageRequest())
    val criteria = AdjudicationSearchCriteria.builder().offenderNumber("OFF-1").build()

    whenever(adjudicationsRepository.findAdjudications(any())).thenReturn(expectedResult)

    assertThat(adjudicationService.findAdjudications(criteria)).isEqualTo(expectedResult)
  }

  @Test
  fun findAdjudicationDetails() {
    val dbResult = AdjudicationDetail.builder()
      .agencyId("MDI")
      .internalLocationId(1L)
      .hearing(Hearing.builder().internalLocationId(2L).build())
      .build()

    val expectedResult = AdjudicationDetail.builder()
      .agencyId("MDI")
      .establishment("Moorlands (HMP)")
      .internalLocationId(1)
      .interiorLocation("Wing 1")
      .hearing(
        Hearing.builder()
          .internalLocationId(2L)
          .establishment("Moorlands (HMP)")
          .location("Hearing Room 1")
          .build(),
      )
      .build()

    val incidentLocation =
      Location.builder().agencyId("MDI").description("MDI-AA-WING-1").userDescription("Wing 1").build()
    val hearingLocation =
      Location.builder().agencyId("MDI").description("MDI-AA-CR-1").userDescription("Hearing Room 1").build()
    val agency = Agency.builder().description("MOORLANDS (HMP)").build()

    whenever(agencyRepository.findAgency("MDI", StatusFilter.ALL, null)).thenReturn(Optional.of(agency))
    whenever(locationRepository.findLocation(1L, StatusFilter.ALL)).thenReturn(Optional.of(incidentLocation))
    whenever(locationRepository.findLocation(2L, StatusFilter.ALL)).thenReturn(Optional.of(hearingLocation))

    whenever(
      adjudicationsRepository.findAdjudicationDetails(any(), ArgumentMatchers.anyLong()),
    ).thenReturn(Optional.of(dbResult))

    assertThat(adjudicationService.findAdjudication("OFF-1", -1)).isEqualTo(expectedResult)

    verify(adjudicationsRepository).findAdjudicationDetails("OFF-1", -1)
  }

  @Test
  fun cannotFindAdjudicationDetails() {
    assertThatThrownBy { adjudicationService.findAdjudication("OFF-1", -1) }
      .isInstanceOf(EntityNotFoundException::class.java)

    verify(adjudicationsRepository).findAdjudicationDetails("OFF-1", -1)
  }

  @Test
  fun adjudicationOffences() {
    val expectedResult = listOf(AdjudicationOffence.builder().build())

    whenever(adjudicationsRepository.findAdjudicationOffences(ArgumentMatchers.anyString()))
      .thenReturn(expectedResult)

    assertThat(adjudicationService.findAdjudicationsOffences("OFF-1")).isEqualTo(expectedResult)

    verify(adjudicationsRepository).findAdjudicationOffences("OFF-1")
  }

  @Test
  fun adjudicationAgencies() {
    val dbResult = listOf(Agency.builder().description("MOORLANDS (HMP)").build())
    val expectedResult = listOf(Agency.builder().description("Moorlands (HMP)").build())

    whenever(adjudicationsRepository.findAdjudicationAgencies(ArgumentMatchers.anyString()))
      .thenReturn(dbResult)

    assertThat(adjudicationService.findAdjudicationAgencies("OFF-1")).isEqualTo(expectedResult)

    verify(adjudicationsRepository).findAdjudicationAgencies("OFF-1")
  }

  @Test
  fun findOffenderAdjudicationHearings() {
    val fromDate = LocalDate.now()
    val toDate = fromDate.plusDays(1)
    val expectedResult = listOf(fakeOffenderHearingStartingAt(fromDate.atStartOfDay()))

    whenever(adjudicationsRepository.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456")))
      .thenReturn(expectedResult)

    val actual =
      adjudicationService.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456"), TimeSlot.AM)

    assertThat(actual).isEqualTo(expectedResult)
  }

  @Test
  fun findOffenderAdjudicationHearingsAreFilteredToAmTimeSlot() {
    val fromDate = LocalDate.now()
    val toDate = fromDate.plusDays(1)

    val morningHearing = fakeOffenderHearingStartingAt(fromDate.atStartOfDay())
    val eveningHearing = fakeOffenderHearingStartingAt(fromDate.atTime(23, 59))

    val expectedResult = listOf(morningHearing)

    whenever(adjudicationsRepository.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456")))
      .thenReturn(
        listOf(morningHearing, eveningHearing),
      )

    val actual =
      adjudicationService.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456"), TimeSlot.AM)

    assertThat(actual).isEqualTo(expectedResult)
  }

  @Test
  fun findOffenderAdjudicationHearingsAreFilteredToPmTimeSlot() {
    val fromDate = LocalDate.now()
    val toDate = fromDate.plusDays(1)

    val morningHearing = fakeOffenderHearingStartingAt(fromDate.atStartOfDay())
    val afternoonHearing = fakeOffenderHearingStartingAt(fromDate.atTime(16, 59))

    val expectedResult = listOf(afternoonHearing)

    whenever(adjudicationsRepository.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456")))
      .thenReturn(
        listOf(morningHearing, afternoonHearing),
      )

    val actual =
      adjudicationService.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456"), TimeSlot.PM)

    assertThat(actual).isEqualTo(expectedResult)
  }

  @Test
  fun findOffenderAdjudicationHearingsAreFilteredToEveningTimeSlot() {
    val fromDate = LocalDate.now()
    val toDate = fromDate.plusDays(1)

    val morningHearing = fakeOffenderHearingStartingAt(fromDate.atStartOfDay())
    val eveningHearing = fakeOffenderHearingStartingAt(fromDate.atTime(23, 59))

    val expectedResult = listOf(eveningHearing)

    whenever(adjudicationsRepository.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456")))
      .thenReturn(
        listOf(morningHearing, eveningHearing),
      )

    val actual =
      adjudicationService.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456"), TimeSlot.ED)

    assertThat(actual).isEqualTo(expectedResult)
  }

  @Test
  fun findOffenderAdjudicationHearingsForPeriodOfOneMonth() {
    val fromDate = LocalDate.now()
    val toDate = fromDate.plusDays(31)
    val expectedResult = listOf(fakeOffenderHearingStartingAt(fromDate.atStartOfDay()))

    whenever(adjudicationsRepository.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456")))
      .thenReturn(expectedResult)

    val actual =
      adjudicationService.findOffenderAdjudicationHearings("MDI", fromDate, toDate, setOf("123456"), TimeSlot.AM)

    assertThat(actual).isEqualTo(expectedResult)
  }

  private fun fakeOffenderHearingStartingAt(time: LocalDateTime): OffenderAdjudicationHearing {
    return OffenderAdjudicationHearing(
      "LEI",
      "A1181HH",
      -1,
      "Governor's Hearing Adult",
      time,
      -1000,
      "LEI-AABCW-1",
      "SCH",
    )
  }

  @Test
  fun findOffenderAdjudicationHearingsFailsWhenToBeforeFrom() {
    val fromDate = LocalDate.now()
    val toDateBeforeFrom = fromDate.minusDays(1)

    assertThatThrownBy {
      adjudicationService.findOffenderAdjudicationHearings(
        "MDI",
        fromDate,
        toDateBeforeFrom,
        setOf("123456"),
        TimeSlot.AM,
      )
    }
      .isInstanceOf(BadRequestException::class.java)
      .hasMessage("The from date must be before the to date.")
  }

  @Test
  fun findOffenderAdjudicationHearingsFailsWhenFromEqualsTo() {
    val fromDate = LocalDate.now()

    assertThatThrownBy {
      adjudicationService.findOffenderAdjudicationHearings(
        "MDI",
        fromDate,
        fromDate,
        setOf("123456"),
        TimeSlot.AM,
      )
    }
      .isInstanceOf(BadRequestException::class.java)
      .hasMessage("The from date must be before the to date.")
  }

  @Test
  fun findOffenderAdjudicationHearingsFailsWhenPeriodRequestedGreateThanMonth() {
    val fromDate = LocalDate.now()
    val toDate = fromDate.plusDays(32)

    assertThatThrownBy {
      adjudicationService.findOffenderAdjudicationHearings(
        "MDI",
        fromDate,
        toDate,
        setOf("123456"),
        TimeSlot.AM,
      )
    }
      .isInstanceOf(BadRequestException::class.java)
      .hasMessage("A maximum of 31 days worth of offender adjudication hearings is allowed.")
  }

  @Test
  fun findOffenderAdjudicationHearingsFailsWhenNoOffendersSupplied() {
    val fromDate = LocalDate.now()
    val toDate = fromDate.plusDays(1)

    assertThatThrownBy {
      adjudicationService.findOffenderAdjudicationHearings(
        "MDI",
        fromDate,
        toDate,
        emptySet(),
        TimeSlot.AM,
      )
    }
      .isInstanceOf(BadRequestException::class.java)
      .hasMessage("At least one offender number must be supplied.")
  }

  companion object {
    private const val BATCH_SIZE = 1
  }
}
