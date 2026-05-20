package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.api.model.PrisonToCourtHearing
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventFilter
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional

class CourtHearingsServiceTest {
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val courtEventRepository: CourtEventRepository = mock()
  private val agencyLocationRepository: AgencyLocationRepository = mock()
  private val eventTypeRepository: ReferenceCodeRepository<MovementReason> = mock()
  private val eventStatusRepository: ReferenceCodeRepository<EventStatus> = mock()
  private val fromPrison: AgencyLocation = mock()
  private val offender: Offender = mock()

  private val startOfEpochClock: Clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())

  private lateinit var offenderBooking: OffenderBooking

  private val courtHearingsService = CourtHearingsService(
    offenderBookingRepository,
    courtEventRepository,
    agencyLocationRepository,
    eventTypeRepository,
    eventStatusRepository,
    startOfEpochClock,
  )

  @Test
  fun scheduleHearing_for_court_case_schedules_a_court_hearing() {
    givenValidBookingLocationsAndCases(ACTIVE_COURT_CASE)

    assertThat(
      courtHearingsService.scheduleHearing(
        offenderBooking.bookingId,
        1L,
        PRISON_TO_COURT_HEARING,
      ),
    ).isEqualTo(
      CourtHearing.builder()
        .id(COURT_EVENT_ID)
        .dateTime(PRISON_TO_COURT_HEARING.courtHearingDateTime)
        .location(AgencyTransformer.transform(COURT_LOCATION, false, false))
        .build(),
    )

    verify(courtEventRepository).save<CourtEvent>(
      CourtEvent.builder()
        .courtLocation(COURT_LOCATION)
        .courtEventType(EVENT_TYPE)
        .directionCode("OUT")
        .eventDate(PRISON_TO_COURT_HEARING.courtHearingDateTime.toLocalDate())
        .eventStatus(EVENT_STATUS)
        .offenderBooking(offenderBooking)
        .offenderCourtCase(ACTIVE_COURT_CASE)
        .startTime(PRISON_TO_COURT_HEARING.courtHearingDateTime)
        .commentText(PRISON_TO_COURT_HEARING.comments)
        .build(),
    )
  }

  private fun givenValidBookingLocationsAndCases(vararg cases: OffenderCourtCase) {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(listOf(*cases))
      .offender(offender)
      .build()

    whenever(offender.id).thenReturn(-1L)
    whenever(offenderBookingRepository.findById(offenderBooking.bookingId))
      .thenReturn(Optional.of(offenderBooking))
    whenever(agencyLocationRepository.findById("PRISON"))
      .thenReturn(Optional.of(fromPrison))
    whenever(agencyLocationRepository.findById("COURT")).thenReturn(
      Optional.of(
        COURT_LOCATION,
      ),
    )
    whenever(courtEventRepository.save(any<CourtEvent>())).thenReturn(
      PERSISTED_COURT_EVENT,
    )
    whenever(eventTypeRepository.findById(MovementReason.COURT)).thenReturn(
      Optional.of(EVENT_TYPE),
    )
    whenever(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED))
      .thenReturn(
        Optional.of(EVENT_STATUS),
      )
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_no_matching_booking() {
    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.empty<OffenderBooking>(),
    )

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        OFFENDER_BOOKING_ID,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Offender booking with id %d not found.", OFFENDER_BOOKING_ID)
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_booking_is_not_active() {
    givenNoActiveBooking()

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        offenderBooking.bookingId,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Offender booking with id %d is not active.", offenderBooking.bookingId)
  }

  private fun givenNoActiveBooking() {
    offenderBooking = OffenderBooking
      .builder()
      .active(false)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .build()

    whenever(offenderBookingRepository.findById(offenderBooking.bookingId))
      .thenReturn(
        Optional.of<OffenderBooking>(offenderBooking),
      )
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_no_matching_court_case_for_booking() {
    givenNoMatchingCourtCaseForActiveBooking()

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        offenderBooking.bookingId,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Court case with id 1 not found.")
  }

  private fun givenNoMatchingCourtCaseForActiveBooking() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .build()

    whenever(offenderBookingRepository.findById(offenderBooking.bookingId))
      .thenReturn(
        Optional.of<OffenderBooking>(offenderBooking),
      )
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_court_case_is_not_active() {
    givenNoActiveCourtCase()

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        offenderBooking.bookingId,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Court case with id 1 is not active.")
  }

  private fun givenNoActiveCourtCase() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(
        listOf(
          OffenderCourtCase.builder()
            .id(1L)
            .caseStatus(INACTIVE_CASE_STATUS)
            .build(),
        ),
      )
      .build()

    whenever(offenderBookingRepository.findById(offenderBooking.bookingId))
      .thenReturn(
        Optional.of<OffenderBooking>(offenderBooking),
      )
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_prison_not_found() {
    givenPrisonNotFound()

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        offenderBooking.bookingId,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Prison with id PRISON not found.")
  }

  private fun givenPrisonNotFound() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(listOf(ACTIVE_COURT_CASE))
      .build()

    whenever(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(
      Optional.of<OffenderBooking>(offenderBooking),
    )
    whenever(agencyLocationRepository.findById("PRISON"))
      .thenReturn(Optional.empty())
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_court_not_found() {
    givenCourtNotFound()

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        offenderBooking.bookingId,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("Court with id COURT not found.")
  }

  private fun givenCourtNotFound() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(listOf(ACTIVE_COURT_CASE))
      .build()

    whenever(offenderBookingRepository.findById(offenderBooking.bookingId))
      .thenReturn(
        Optional.of<OffenderBooking>(offenderBooking),
      )
    whenever(agencyLocationRepository.findById("PRISON"))
      .thenReturn(Optional.of(fromPrison))
    whenever(agencyLocationRepository.findById("COURT"))
      .thenReturn(Optional.empty())
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_court_is_not_currently_active() {
    givenProvidedCourtIsNotActive()

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        OFFENDER_BOOKING_ID,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Supplied court location wih id %s is not active.", PRISON_TO_COURT_HEARING.toCourtLocation)
  }

  private fun givenProvidedCourtIsNotActive() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(listOf(ACTIVE_COURT_CASE))
      .build()

    whenever(offenderBookingRepository.findById(offenderBooking.bookingId))
      .thenReturn(
        Optional.of<OffenderBooking>(offenderBooking),
      )
    whenever(agencyLocationRepository.findById("PRISON"))
      .thenReturn(Optional.of(fromPrison))
    whenever(agencyLocationRepository.findById("COURT")).thenReturn(
      Optional.of(
        AgencyLocation.builder()
          .active(false)
          .description("Agency Description")
          .id("COURT")
          .type(AgencyLocationType.COURT_TYPE)
          .build(),
      ),
    )
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_supplied_court_is_not_court() {
    givenProvidedCourtLocationIsNotACourt()

    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        OFFENDER_BOOKING_ID,
        1L,
        PRISON_TO_COURT_HEARING,
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Supplied court location wih id COURT is not a valid court location.")
  }

  private fun givenProvidedCourtLocationIsNotACourt() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(listOf(ACTIVE_COURT_CASE))
      .build()

    whenever(offenderBookingRepository.findById(offenderBooking.bookingId))
      .thenReturn(
        Optional.of<OffenderBooking>(offenderBooking),
      )
    whenever(agencyLocationRepository.findById("PRISON"))
      .thenReturn(Optional.of(fromPrison))
    whenever(agencyLocationRepository.findById("COURT"))
      .thenReturn(Optional.of(fromPrison))
    whenever(fromPrison.type).thenReturn(AgencyLocationType("NOT_CRT"))
  }

  @Test
  fun scheduleHearing_for_court_case_errors_when_hearing_date_not_in_future() {
    assertThatThrownBy {
      courtHearingsService.scheduleHearing(
        OFFENDER_BOOKING_ID,
        1L,
        PrisonToCourtHearing.builder()
          .courtHearingDateTime(LocalDateTime.now(startOfEpochClock))
          .build(),
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Court hearing must be in the future.")
  }

  @Test
  fun getCourtHearingsFor_retrieves_single_hearing_for_booking() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(listOf())
      .offender(offender)
      .build()

    val hearing = CourtEvent.builder()
      .id(1L)
      .courtLocation(COURT_LOCATION)
      .eventDate(PRISON_TO_COURT_HEARING.courtHearingDateTime.toLocalDate())
      .offenderBooking(offenderBooking)
      .offenderCourtCase(ACTIVE_COURT_CASE)
      .startTime(PRISON_TO_COURT_HEARING.courtHearingDateTime)
      .build()

    givenValidBookingWithOneOrMoreCourtHearings(1L, hearing(hearing.id))

    val hearings = courtHearingsService.getCourtHearingsFor(1L, null, null)

    assertThat(hearings.hearings)
      .containsExactly(
        CourtHearing.builder()
          .id(hearing.id)
          .dateTime(hearing.eventDateTime)
          .location(AgencyTransformer.transform(hearing.courtLocation, false, false))
          .build(),
      )
  }

  @Test
  fun getCourtHearingsFor_retrieves_multiple_hearings_for_booking() {
    offenderBooking = OffenderBooking
      .builder()
      .active(true)
      .bookingId(OFFENDER_BOOKING_ID)
      .location(fromPrison)
      .courtCases(listOf())
      .offender(offender)
      .build()

    val hearing1 = CourtEvent.builder()
      .id(1L)
      .courtLocation(COURT_LOCATION)
      .eventDate(PRISON_TO_COURT_HEARING.courtHearingDateTime.toLocalDate())
      .offenderBooking(offenderBooking)
      .offenderCourtCase(ACTIVE_COURT_CASE)
      .startTime(PRISON_TO_COURT_HEARING.courtHearingDateTime)
      .build()

    val hearing2 = CourtEvent.builder()
      .id(2L)
      .courtLocation(COURT_LOCATION)
      .eventDate(PRISON_TO_COURT_HEARING.courtHearingDateTime.toLocalDate())
      .offenderBooking(offenderBooking)
      .offenderCourtCase(ACTIVE_COURT_CASE)
      .startTime(PRISON_TO_COURT_HEARING.courtHearingDateTime)
      .build()

    givenValidBookingWithOneOrMoreCourtHearings(2L, hearing(hearing1.id), hearing(hearing2.id))

    val hearings = courtHearingsService.getCourtHearingsFor(2L, null, null)

    assertThat(hearings.hearings)
      .containsExactly(
        CourtHearing.builder()
          .id(hearing1.id)
          .dateTime(hearing1.eventDateTime)
          .location(AgencyTransformer.transform(hearing1.courtLocation, false, false))
          .build(),
        CourtHearing.builder()
          .id(hearing2.id)
          .dateTime(hearing2.eventDateTime)
          .location(AgencyTransformer.transform(hearing2.courtLocation, false, false))
          .build(),
      )
  }

  @Test
  fun getCourtHearings_throws_service_exception_for_invalid_dates() {
    assertThatThrownBy {
      courtHearingsService.getCourtHearingsFor(
        -1L,
        LocalDate.of(2020, 3, 23),
        LocalDate.of(2020, 3, 22),
      )
    }
      .isInstanceOf(BadRequestException::class.java)
      .hasMessage("Invalid date range: toDate is before fromDate.")
  }

  @Test
  fun getCourtHearings_does_not_throw_service_exception_for_valid_dates() {
    assertThatCode {
      courtHearingsService.getCourtHearingsFor(
        -1L,
        LocalDate.of(2020, 3, 22),
        LocalDate.of(2020, 3, 23),
      )
    }
      .doesNotThrowAnyException()

    assertThatCode {
      courtHearingsService.getCourtHearingsFor(
        -1L,
        LocalDate.of(2020, 3, 22),
        LocalDate.of(2020, 3, 22),
      )
    }
      .doesNotThrowAnyException()
  }

  private fun givenValidBookingWithOneOrMoreCourtHearings(bookingId: Long?, vararg events: CourtEvent) {
    whenever(
      courtEventRepository.findAll(
        CourtEventFilter.builder().bookingId(bookingId).build(),
      ),
    ).thenReturn(listOf(*events))
  }

  private fun hearing(hearingId: Long?): CourtEvent = CourtEvent.builder()
    .id(hearingId)
    .courtLocation(COURT_LOCATION)
    .eventDate(PRISON_TO_COURT_HEARING.courtHearingDateTime.toLocalDate())
    .offenderBooking(offenderBooking)
    .offenderCourtCase(ACTIVE_COURT_CASE)
    .startTime(PRISON_TO_COURT_HEARING.courtHearingDateTime)
    .build()

  companion object {
    private const val OFFENDER_BOOKING_ID = 1L

    private const val COURT_EVENT_ID = 99L

    private val PRISON_TO_COURT_HEARING: PrisonToCourtHearing = PrisonToCourtHearing.builder()
      .fromPrisonLocation("PRISON")
      .toCourtLocation("COURT")
      .courtHearingDateTime(LocalDateTime.of(2020, 3, 13, 12, 0))
      .comments("some comments related to the court hearing.")
      .build()

    private val COURT_LOCATION: AgencyLocation = AgencyLocation.builder()
      .active(true)
      .description("Agency Description")
      .id("COURT")
      .type(AgencyLocationType.COURT_TYPE)
      .build()

    private val PERSISTED_COURT_EVENT: CourtEvent = CourtEvent.builder()
      .id(COURT_EVENT_ID)
      .courtLocation(COURT_LOCATION)
      .eventDate(LocalDate.of(2020, 3, 13))
      .startTime(LocalDateTime.of(2020, 3, 13, 12, 0))
      .build()

    private val ACTIVE_CASE_STATUS = CaseStatus("A", "Active")

    private val INACTIVE_CASE_STATUS = CaseStatus("I", "Inactive")

    private val ACTIVE_COURT_CASE: OffenderCourtCase = OffenderCourtCase.builder()
      .id(1L)
      .caseSeq(1)
      .caseStatus(ACTIVE_CASE_STATUS)
      .build()

    private val EVENT_TYPE = MovementReason("EVENT_TYPE_CODE", "EVENT_TYPE_DESCRIPTION")

    private val EVENT_STATUS = EventStatus("EVENT_STATUS_CODE", "EVENT_STATUS_DESCRIPTION")
  }
}
