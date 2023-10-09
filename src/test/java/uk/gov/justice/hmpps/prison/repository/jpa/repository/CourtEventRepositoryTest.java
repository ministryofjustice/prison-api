package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent.CourtEventBuilder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.COMPLETED;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.SCHEDULED_APPROVED;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
@ContextConfiguration(classes = CourtEventRepositoryTest.TestClock.class)
public class CourtEventRepositoryTest {

    @TestConfiguration
    static class TestClock {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.now(), ZoneId.systemDefault());
        }
    }

    @Autowired
    private Clock clock;

    private static final long BOOKING_WITH_COURT_CASE = -1L;

    private static final long BOOKING_WITHOUT_COURT_CASE = -31L;

    @Autowired
    private CourtEventRepository courtEventRepository;

    @Autowired
    private CourtEventChargeRepository courtEventChargeRepository;

    @Autowired
    private OffenceResultRepository offenceResultRepository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private AgencyLocationRepository agencyRepository;

    @Autowired
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Autowired
    private ReferenceCodeRepository<MovementReason> eventTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final CourtEventBuilder builder = CourtEvent.builder();

    @BeforeEach
    void setup() {
        final var eventDate = LocalDate.now(clock).plusDays(1);
        final var startTime = eventDate.atTime(12, 0);
        final var bookingWithCourtCase = offenderBookingRepository.findById(BOOKING_WITH_COURT_CASE).orElseThrow();

        builder
                .commentText("Comment text for court event")
                .courtEventType(eventTypeRepository.findById(MovementReason.COURT).orElseThrow())
                .courtLocation(agencyRepository.findById("COURT1").orElseThrow())
                .directionCode("OUT")
                .eventDate(eventDate)
                .eventStatus(eventStatusRepository.findById(SCHEDULED_APPROVED).orElseThrow())
                .offenderBooking(bookingWithCourtCase)
                .offenderCourtCase(bookingWithCourtCase.getCourtCases().stream().findFirst().orElseThrow())
                .startTime(startTime);
    }

    @Test
    void court_event_can_be_saved_and_retrieved_with_defaults_populated() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder.build());

        entityManager.flush();

        assertThat(courtEventRepository.findById(savedCourtEventWithCourtCase.getId()).orElseThrow()).isEqualTo(savedCourtEventWithCourtCase);

        // defaults populated
        assertThat(savedCourtEventWithCourtCase.getNextEventRequestFlag()).isEqualTo("N");
        assertThat(savedCourtEventWithCourtCase.getOrderRequestedFlag()).isEqualTo("N");
    }

    @Test
    void court_event_retrieved_by_booking_and_hearing_id() {
        final var persistedCourtEvent = courtEventRepository.save(builder.build());

        entityManager.flush();

        assertThat(courtEventRepository.findByOffenderBooking_BookingIdAndId(persistedCourtEvent.getOffenderBooking().getBookingId(), persistedCourtEvent.getId())).isNotEmpty();
    }

    @Test
    void court_event_can_be_saved_and_retrieved_with_defaults_overridden() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder
                .nextEventRequestFlag("X")
                .orderRequestedFlag("Y")
                .build());

        entityManager.flush();

        assertThat(courtEventRepository.findById(savedCourtEventWithCourtCase.getId()).orElseThrow()).isEqualTo(savedCourtEventWithCourtCase);

        // defaults overridden
        assertThat(savedCourtEventWithCourtCase.getNextEventRequestFlag()).isEqualTo("X");
        assertThat(savedCourtEventWithCourtCase.getOrderRequestedFlag()).isEqualTo("Y");
    }

    @Test
    void court_event_only_active_charges_are_carried_over_from_court_case_on_creation() {
        final var prePersistCourtEvent = builder.build();

        addInactiveChargeTo(prePersistCourtEvent.getOffenderCourtCase().get());

        assertThat(prePersistCourtEvent.getCharges()).isEmpty();
        assertThat(prePersistCourtEvent.getOffenderCourtCase().get().getCharges()).hasSize(2);

        final var savedCourtEventWithCourtCase = courtEventRepository.save(prePersistCourtEvent);

        entityManager.flush();

        final var postPersistCourtEvent = courtEventRepository.findById(savedCourtEventWithCourtCase.getId()).orElseThrow();

        assertThat(postPersistCourtEvent.getCharges()).hasSize(1);
        assertThat(postPersistCourtEvent.getCharges())
                .extracting(charge -> charge.getEventAndCharge().getOffenderCharge().isActive())
                .containsExactly(true);
    }

    private void addInactiveChargeTo(final OffenderCourtCase courtCase) {
        final var inactiveCharge = OffenderCharge.builder().chargeStatus("I").build();

        assertThat(inactiveCharge.isActive()).isFalse();

        courtCase.add(inactiveCharge);
    }

    @Test
    void court_event_without_court_case_retrieved() {
        final var bookingWithoutCourtCase = offenderBookingRepository.findById(BOOKING_WITHOUT_COURT_CASE).orElseThrow();

        assertThat(bookingWithoutCourtCase.getCourtCases()).isEmpty();

        final var savedCourtEventWithoutCourtCase = courtEventRepository.save(builder
                .offenderBooking(bookingWithoutCourtCase)
                .offenderCourtCase(null)
                .build());

        assertThat(savedCourtEventWithoutCourtCase.getOffenderBooking().getCourtCases()).isEmpty();
        assertThat(savedCourtEventWithoutCourtCase.getOffenderCourtCase()).isEmpty();
        assertThat(savedCourtEventWithoutCourtCase.getCharges()).isEmpty();

        assertThat(courtEventRepository.findById(savedCourtEventWithoutCourtCase.getId()).orElseThrow()).isEqualTo(savedCourtEventWithoutCourtCase);
    }

    @Test
    void court_event_in_future_and_charges_deleted() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder.build());

        entityManager.flush();

        final var chargeIdentifier = savedCourtEventWithCourtCase.getCharges().stream().findFirst().orElseThrow().getEventAndCharge();

        assertThat(courtEventChargeRepository.findById(chargeIdentifier)).isNotEmpty();

        final var id = savedCourtEventWithCourtCase.getId();

        courtEventRepository.delete(savedCourtEventWithCourtCase);

        entityManager.flush();

        assertThat(courtEventRepository.findById(id)).isEmpty();

        assertThat(courtEventChargeRepository.findById(chargeIdentifier)).isEmpty();
    }

    @Test
    void court_event_in_past_cannot_be_deleted() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder
                .eventDate(LocalDate.now(clock).minusDays(1))
                .build());

        entityManager.flush();

        final var id = savedCourtEventWithCourtCase.getId();

        assertThatThrownBy(() -> courtEventRepository.deleteById(id))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Court hearing '%s' cannot be deleted as its start date/time is in the past.", id);
    }

    @Test
    void court_event_that_is_not_scheduled_cannot_be_deleted() {
        final var savedCourtEventWithCourtCase = courtEventRepository.save(builder
                .eventStatus(eventStatusRepository.findById(COMPLETED).orElseThrow())
                .build());

        entityManager.flush();

        final var id = savedCourtEventWithCourtCase.getId();

        assertThatThrownBy(() -> courtEventRepository.deleteById(id))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Court hearing '%s' must be in a scheduled state to delete.", id);
    }

    @Test
    void court_event_upcoming() {
        final var events = courtEventRepository.getCourtEventsUpcoming(LocalDateTime.of(2016, 1, 1, 0, 0));

        assertThat(events).asList().extracting("offenderNo", "startTime", "court", "courtDescription", "eventSubType", "eventDescription", "holdFlag")
                .contains(Tuple.tuple("A1234AB", LocalDateTime.of(2017, 2, 20, 10, 11), "ABDRCT", "court 2", "CA", "Court Appearance", "N"),
                        Tuple.tuple("A1234AH", LocalDateTime.of(2050, 1, 1, 11, 0), "ABDRCT", "court 2", "DC", "Discharged to Court", "Y"));

    }

    @Test
    void court_events_by_booking_id() {
        final var offenceResult = new OffenceResult()
            .withCode("4016")
            .withDescription("Imprisonment")
            .withDispositionCode("F");
        offenceResultRepository.save(offenceResult);

        final var bookingId = -2L;
        final var courtEvents = courtEventRepository.findByOffenderBooking_BookingIdAndOffenderCourtCase_CaseStatus_Code(bookingId, "A");
        assertThat(courtEvents.size()).isEqualTo(2);
        courtEvents.forEach(event -> {
            assertThat(event.getOffenderBooking().getBookingId()).isEqualTo(bookingId);
            assertThat(event.getOutcomeReasonCode().getCode()).isEqualTo(offenceResult.getCode());
        });
    }
}
