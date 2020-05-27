package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.CourtEvent.CourtEventBuilder;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.EventType;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static net.syscon.elite.repository.jpa.model.EventStatus.SCHEDULED_APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class CourtEventRepositoryTest {

    private static final long BOOKING_WITH_COURT_CASE = -1L;

    private static final long BOOKING_WITHOUT_COURT_CASE = -31L;

    @Autowired
    private CourtEventRepository courtEventRepository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private AgencyLocationRepository agencyRepository;

    @Autowired
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Autowired
    private ReferenceCodeRepository<EventType> eventTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final CourtEventBuilder builder = CourtEvent.builder();
    private CourtEvent persisted;
    private CourtEvent courtEvent;

    @BeforeEach
    void setup() {
        final var eventDate = LocalDate.now();
        final var startTime = eventDate.atTime(12, 0);
        final var bookingWithCourtCase = offenderBookingRepository.findById(BOOKING_WITH_COURT_CASE).orElseThrow();

        builder
                .commentText("Comment text for court event")
                .courtEventType(eventTypeRepository.findById(EventType.COURT).orElseThrow())
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
    void court_event_charges_are_carried_over_from_court_case_on_creation() {
        final var prePersistCourtEvent = builder.build();

        assertThat(prePersistCourtEvent.getCharges()).isEmpty();

        final var savedCourtEventWithCourtCase = courtEventRepository.save(prePersistCourtEvent);

        entityManager.flush();

        final var postPersistCourtEvent = courtEventRepository.findById(savedCourtEventWithCourtCase.getId()).orElseThrow();

        assertThat(postPersistCourtEvent.getCharges()).isNotEmpty();
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
}
