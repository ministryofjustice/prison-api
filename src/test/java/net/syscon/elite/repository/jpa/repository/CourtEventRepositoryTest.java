package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.EventType;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
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
    private TestEntityManager entityManager;

    @Test
    public void court_event_can_be_saved_and_retrieved() {
        var id = 999L;
        var commentText = "Comment text for court event";
        var courtLocation = agencyRepository.findById("COURT1").orElseThrow();
        var courtEventType = new EventType("CRT", "Court Action");
        var directionCode = "OUT";
        var eventDate = LocalDate.now();
        var eventStatus = new EventStatus("SCH", "Scheduled (Approved)");
        var startTime = eventDate.atTime(12, 0);
        var nextEventRequestFlag = "X";
        var offenderBooking = offenderBookingRepository.findById(-1L).orElseThrow();
        var offenderCourtCase = offenderBooking.getCourtCases().stream().findFirst().orElseThrow();
        var orderRequestedFlag = "Y";

        courtEventRepository.save(CourtEvent.builder()
                .id(id)
                .commentText(commentText)
                .courtEventType(courtEventType)
                .courtLocation(courtLocation)
                .directionCode(directionCode)
                .eventDate(eventDate)
                .eventStatus(eventStatus)
                .nextEventRequestFlag(nextEventRequestFlag)
                .offenderBooking(offenderBooking)
                .offenderCourtCase(offenderCourtCase)
                .orderRequestedFlag(orderRequestedFlag)
                .startTime(startTime)
                .build());

        entityManager.flush();

        var persistedCourtEvent = courtEventRepository.findById(999L).orElseThrow();

        assertThat(persistedCourtEvent)
                .extracting(
                        CourtEvent::getId,
                        CourtEvent::getCommentText,
                        CourtEvent::getCourtEventType,
                        CourtEvent::getCourtLocation,
                        CourtEvent::getDirectionCode,
                        CourtEvent::getEventDate,
                        CourtEvent::getEventStatus,
                        CourtEvent::getNextEventRequestFlag,
                        CourtEvent::getOffenderBooking,
                        CourtEvent::getOffenderCourtCase,
                        CourtEvent::getOrderRequestedFlag,
                        CourtEvent::getStartTime)
                .containsOnly(
                        id,
                        commentText,
                        courtEventType,
                        courtLocation,
                        directionCode,
                        eventDate,
                        nextEventRequestFlag,
                        eventStatus,
                        offenderBooking,
                        offenderCourtCase,
                        orderRequestedFlag,
                        startTime);
    }
}
