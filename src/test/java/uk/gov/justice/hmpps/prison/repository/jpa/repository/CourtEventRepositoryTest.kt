package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.dao.DataAccessException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
@ContextConfiguration(classes = [CourtEventRepositoryTest.TestClock::class])
class CourtEventRepositoryTest {
  @TestConfiguration
  internal class TestClock {
    @Bean
    fun clock(): Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
  }

  @Autowired
  private lateinit var clock: Clock

  @Autowired
  private lateinit var courtEventRepository: CourtEventRepository

  @Autowired
  private lateinit var courtEventChargeRepository: CourtEventChargeRepository

  @Autowired
  private lateinit var offenceResultRepository: OffenceResultRepository

  @Autowired
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @Autowired
  private lateinit var agencyRepository: AgencyLocationRepository

  @Autowired
  private lateinit var eventStatusRepository: ReferenceCodeRepository<EventStatus>

  @Autowired
  private lateinit var eventTypeRepository: ReferenceCodeRepository<MovementReason>

  @Autowired
  private lateinit var entityManager: TestEntityManager

  private val builder = CourtEvent.builder()

  @BeforeEach
  fun setup() {
    val eventDate = LocalDate.now(clock).plusDays(1)
    val startTime = eventDate.atTime(12, 0)
    val bookingWithCourtCase = offenderBookingRepository.findById(BOOKING_WITH_COURT_CASE).orElseThrow()
    builder
      .commentText("Comment text for court event")
      .courtEventType(eventTypeRepository.findById(MovementReason.COURT).orElseThrow())
      .courtLocation(agencyRepository.findById("COURT1").orElseThrow())
      .directionCode("OUT")
      .eventDate(eventDate)
      .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED_APPROVED).orElseThrow())
      .offenderBooking(bookingWithCourtCase)
      .offenderCourtCase(bookingWithCourtCase.courtCases.first())
      .startTime(startTime)
  }

  @Test
  fun court_event_can_be_saved_and_retrieved_with_defaults_populated() {
    val savedCourtEventWithCourtCase = courtEventRepository.save(builder.build())
    entityManager.flush()
    assertThat(
      courtEventRepository.findById(savedCourtEventWithCourtCase.id).orElseThrow(),
    ).isEqualTo(savedCourtEventWithCourtCase)

    // defaults populated
    assertThat(savedCourtEventWithCourtCase.nextEventRequestFlag).isEqualTo("N")
    assertThat(savedCourtEventWithCourtCase.orderRequestedFlag).isEqualTo("N")
  }

  @Test
  fun court_event_retrieved_by_booking_and_hearing_id() {
    val persistedCourtEvent = courtEventRepository.save(builder.build())
    entityManager.flush()
    assertThat(
      courtEventRepository.findByOffenderBooking_BookingIdAndId(
        persistedCourtEvent.offenderBooking.bookingId,
        persistedCourtEvent.id,
      ),
    ).isNotEmpty()
  }

  @Test
  fun court_event_can_be_saved_and_retrieved_with_defaults_overridden() {
    val savedCourtEventWithCourtCase = courtEventRepository.save(
      builder
        .nextEventRequestFlag("X")
        .orderRequestedFlag("Y")
        .build(),
    )
    entityManager.flush()
    assertThat(
      courtEventRepository.findById(savedCourtEventWithCourtCase.id).orElseThrow(),
    ).isEqualTo(savedCourtEventWithCourtCase)

    // defaults overridden
    assertThat(savedCourtEventWithCourtCase.nextEventRequestFlag).isEqualTo("X")
    assertThat(savedCourtEventWithCourtCase.orderRequestedFlag).isEqualTo("Y")
  }

  @Test
  fun court_event_only_active_charges_are_carried_over_from_court_case_on_creation() {
    val prePersistCourtEvent = builder.build()
    addInactiveChargeTo(prePersistCourtEvent.offenderCourtCase.get())
    assertThat(prePersistCourtEvent.charges).isEmpty()
    assertThat(prePersistCourtEvent.offenderCourtCase.get().charges).hasSize(2)
    val savedCourtEventWithCourtCase = courtEventRepository.save(prePersistCourtEvent)
    entityManager.flush()
    val postPersistCourtEvent = courtEventRepository.findById(savedCourtEventWithCourtCase.id).orElseThrow()
    assertThat(postPersistCourtEvent.charges).hasSize(1)
    assertThat(postPersistCourtEvent.charges)
      .extracting<Boolean, RuntimeException> { charge: CourtEventCharge -> charge.eventAndCharge.offenderCharge.isActive }
      .containsExactly(true)
  }

  private fun addInactiveChargeTo(courtCase: OffenderCourtCase) {
    val inactiveCharge = OffenderCharge.builder().chargeStatus("I").build()
    assertThat(inactiveCharge.isActive).isFalse()
    courtCase.add(inactiveCharge)
  }

  @Test
  fun court_event_without_court_case_retrieved() {
    val bookingWithoutCourtCase = offenderBookingRepository.findById(BOOKING_WITHOUT_COURT_CASE).orElseThrow()
    assertThat(bookingWithoutCourtCase.courtCases).isEmpty()
    val savedCourtEventWithoutCourtCase = courtEventRepository.save(
      builder
        .offenderBooking(bookingWithoutCourtCase)
        .offenderCourtCase(null)
        .build(),
    )
    assertThat(savedCourtEventWithoutCourtCase.offenderBooking.courtCases).isEmpty()
    assertThat(savedCourtEventWithoutCourtCase.offenderCourtCase).isEmpty()
    assertThat(savedCourtEventWithoutCourtCase.charges).isEmpty()
    assertThat(
      courtEventRepository.findById(savedCourtEventWithoutCourtCase.id).orElseThrow(),
    ).isEqualTo(savedCourtEventWithoutCourtCase)
  }

  @Test
  fun court_event_in_future_and_charges_deleted() {
    val savedCourtEventWithCourtCase = courtEventRepository.save(builder.build())
    entityManager.flush()
    val chargeIdentifier = savedCourtEventWithCourtCase.charges.first().eventAndCharge
    assertThat(courtEventChargeRepository.findById(chargeIdentifier)).isNotEmpty()
    val id = savedCourtEventWithCourtCase.id
    courtEventRepository.delete(savedCourtEventWithCourtCase)
    entityManager.flush()
    assertThat(
      courtEventRepository.findById(id),
    ).isEmpty()
    assertThat(courtEventChargeRepository.findById(chargeIdentifier)).isEmpty()
  }

  @Test
  fun court_event_in_past_cannot_be_deleted() {
    val savedCourtEventWithCourtCase = courtEventRepository.save(
      builder
        .eventDate(LocalDate.now(clock).minusDays(1))
        .build(),
    )
    entityManager.flush()
    val id = savedCourtEventWithCourtCase.id
    assertThatThrownBy { courtEventRepository.deleteById(id) }
      .isInstanceOf(DataAccessException::class.java)
      .hasMessageContaining("Court hearing '%s' cannot be deleted as its start date/time is in the past.", id)
  }

  @Test
  fun court_event_that_is_not_scheduled_cannot_be_deleted() {
    val savedCourtEventWithCourtCase = courtEventRepository.save(
      builder
        .eventStatus(eventStatusRepository.findById(EventStatus.COMPLETED).orElseThrow())
        .build(),
    )
    entityManager.flush()
    val id = savedCourtEventWithCourtCase.id
    assertThatThrownBy { courtEventRepository.deleteById(id) }
      .hasRootCauseInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Court hearing '%s' must be in a scheduled state to delete.", id)
  }

  @Test
  fun court_event_upcoming() {
    val events = courtEventRepository.getCourtEventsUpcoming(LocalDateTime.of(2016, 1, 1, 0, 0))
    assertThat(events).asList().extracting(
      "offenderNo",
      "startTime",
      "court",
      "courtDescription",
      "eventSubType",
      "eventDescription",
      "holdFlag",
    )
      .contains(
        Tuple.tuple(
          "A1234AB",
          LocalDateTime.of(2017, 2, 20, 10, 11),
          "ABDRCT",
          "court 2",
          "CA",
          "Court Appearance",
          "N",
        ),
        Tuple.tuple(
          "A1234AH",
          LocalDateTime.of(2050, 1, 1, 11, 0),
          "ABDRCT",
          "court 2",
          "DC",
          "Discharged to Court",
          "Y",
        ),
      )
  }

  @Test
  fun court_events_by_booking_id() {
    val offenceResult = OffenceResult()
      .withCode("4016")
      .withDescription("Imprisonment")
      .withDispositionCode("F")
    offenceResultRepository.save(offenceResult)
    val bookingIds = setOf(-2L)
    val courtEvents =
      courtEventRepository.findByOffenderBooking_BookingIdInAndOffenderCourtCase_CaseStatus_Code(bookingIds, "A")
    assertThat(courtEvents.size).isEqualTo(2)
    courtEvents.forEach { event: CourtEvent ->
      assertThat(event.offenderBooking.bookingId).isEqualTo(-2L)
      assertThat(event.outcomeReasonCode.code).isEqualTo(offenceResult.code)
    }
  }

  companion object {
    private const val BOOKING_WITH_COURT_CASE = -1L
    private const val BOOKING_WITHOUT_COURT_CASE = -31L
  }
}
