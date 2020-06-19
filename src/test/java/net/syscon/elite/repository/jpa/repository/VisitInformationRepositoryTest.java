package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.VisitInformation;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class VisitInformationRepositoryTest {

    @Autowired
    private VisitRepository repository;

    @Test
    public void findAllByBookingId() {
        var visits = repository.findAllByBookingId(-1L);

        assertThat(visits).hasSize(15);
        assertThat(visits).extracting(VisitInformation::getVisitId).containsOnly(-3L, -2L, -4L, -5L, -1L, -6L, -8L, -7L, -10L, -9L, -13L, -14L, -12L, -11L, -15L);
        assertThat(visits).extracting(VisitInformation::getCancellationReason).containsOnly("NSHOW",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  "NSHOW");
        assertThat(visits).extracting(VisitInformation::getCancelReasonDescription).containsOnly("Visitor Did Not Arrive",   null,   null,   null,   null,   null,   null,   null,   null,   null,   null,   null,   null,   null,   "Visitor Did Not Arrive");
        assertThat(visits).extracting(VisitInformation::getEventStatus).containsOnly("CANC", null, null, null, null, null, null, null, null, null, null, null, null, null, "CANC");
        assertThat(visits).extracting(VisitInformation::getEventStatusDescription).containsOnly("Cancelled", null, null, null, null, null, null, null, null, null, null, null, null, null, "Cancelled");
        assertThat(visits).extracting(VisitInformation::getEventOutcome).containsOnly("ABS", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ABS");
        assertThat(visits).extracting(VisitInformation::getEventOutcomeDescription).containsOnly("Absence",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Attended",  "Absence");
        assertThat(visits).extracting(VisitInformation::getStartTime).containsOnly(LocalDateTime.parse("2017-12-10T14:30"), LocalDateTime.parse("2017-11-13T14:30"), LocalDateTime.parse("2017-10-13T14:30"), LocalDateTime.parse("2017-09-15T14:00"), LocalDateTime.parse("2017-09-12T14:30"), LocalDateTime.parse("2017-09-10T14:30"), LocalDateTime.parse("2017-08-10T14:30"), LocalDateTime.parse("2017-07-10T14:30"), LocalDateTime.parse("2017-06-10T14:30"), LocalDateTime.parse("2017-05-10T14:30"), LocalDateTime.parse("2017-04-10T14:30"), LocalDateTime.parse("2017-03-10T14:30"), LocalDateTime.parse("2017-02-10T14:30"), LocalDateTime.parse("2017-01-10T14:30"), LocalDateTime.parse("2016-12-11T14:30"));
        assertThat(visits).extracting(VisitInformation::getEndTime).containsOnly(LocalDateTime.parse("2017-12-10T15:30"),LocalDateTime.parse("2017-11-13T15:30"),LocalDateTime.parse("2017-10-13T15:30"),LocalDateTime.parse("2017-09-15T16:00"),LocalDateTime.parse("2017-09-12T15:30"),LocalDateTime.parse("2017-09-10T15:30"),LocalDateTime.parse("2017-08-10T15:30"),LocalDateTime.parse("2017-07-10T15:30"),LocalDateTime.parse("2017-06-10T15:30"),LocalDateTime.parse("2017-05-10T16:30"),LocalDateTime.parse("2017-04-10T15:30"),LocalDateTime.parse("2017-03-10T16:30"),LocalDateTime.parse("2017-02-10T15:30"),LocalDateTime.parse("2017-01-10T15:30"),LocalDateTime.parse("2016-12-11T15:30"));
        assertThat(visits).extracting(VisitInformation::getLocation).containsOnly("Visiting Room", "Visiting Room", "Visiting Room", "Chapel", "Visiting Room", "Visiting Room", "Visiting Room", "Visiting Room", "Visiting Room", "Chapel", "Visiting Room", "Chapel", "Visiting Room", "Visiting Room", "Visiting Room");
        assertThat(visits).extracting(VisitInformation::getVisitType).containsOnly("SCON", "SCON", "SCON", "OFFI", "SCON", "SCON", "SCON", "SCON", "SCON", "OFFI", "SCON", "OFFI", "SCON", "SCON", "SCON");
        assertThat(visits).extracting(VisitInformation::getVisitTypeDescription).containsOnly("Social Contact", "Social Contact", "Social Contact", "Official Visit", "Social Contact", "Social Contact", "Social Contact", "Social Contact", "Social Contact", "Official Visit", "Social Contact", "Official Visit", "Social Contact", "Social Contact", "Social Contact");
        assertThat(visits).extracting(VisitInformation::getLeadVisitor).containsOnly("JESSY SMITH1", null, null, null, null, null, null, null, null, null, null, null, null, null, "JESSY SMITH1");
        assertThat(visits).extracting(VisitInformation::getRelationship).containsOnly("UN", null, null, null, null, null, null, null, null, null, null, null, null, null, "UN");
        assertThat(visits).extracting(VisitInformation::getRelationshipDescription).containsOnly("Uncle", null, null, null, null, null, null, null, null, null, null, null, null, null, "Uncle");

    }
}


