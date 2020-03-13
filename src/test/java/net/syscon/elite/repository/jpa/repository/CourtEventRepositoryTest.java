package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.CourtEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class CourtEventRepositoryTest {

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

    private final CourtEvent.CourtEventBuilder builder = CourtEvent.builder();

    @BeforeEach
    void setup() {
        final var eventDate = LocalDate.now();
        final var startTime = eventDate.atTime(12, 0);
        final var offenderBooking = offenderBookingRepository.findById(-1L).orElseThrow();

        builder
                .commentText("Comment text for court event")
                .courtEventType(eventTypeRepository.findById(EventType.COURT).orElseThrow())
                .courtLocation(agencyRepository.findById("COURT1").orElseThrow())
                .directionCode("OUT")
                .eventDate(eventDate)
                .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED).orElseThrow())
                .offenderBooking(offenderBooking)
                .offenderCourtCase(offenderBooking.getCourtCases().stream().findFirst().orElseThrow())
                .startTime(startTime);
    }

    @Test
    void court_event_can_be_saved_and_retrieved_with_defaults_populated() {
        final var savedCourtEvent = courtEventRepository.save(builder.build());

        entityManager.flush();

        assertThat(courtEventRepository.findById(savedCourtEvent.getId()).orElseThrow()).isEqualTo(savedCourtEvent);

        // defaults populated
        assertThat(savedCourtEvent.getNextEventRequestFlag()).isEqualTo("N");
        assertThat(savedCourtEvent.getOrderRequestedFlag()).isEqualTo("N");
    }

    @Test
    void court_event_can_be_saved_and_retrieved_with_defaults_overridden() {
        final var savedCourtEvent = courtEventRepository.save(builder
                .nextEventRequestFlag("X")
                .orderRequestedFlag("Y")
                .build());

        entityManager.flush();

        assertThat(courtEventRepository.findById(savedCourtEvent.getId()).orElseThrow()).isEqualTo(savedCourtEvent);

        // defaults overridden
        assertThat(savedCourtEvent.getNextEventRequestFlag()).isEqualTo("X");
        assertThat(savedCourtEvent.getOrderRequestedFlag()).isEqualTo("Y");
    }
}
