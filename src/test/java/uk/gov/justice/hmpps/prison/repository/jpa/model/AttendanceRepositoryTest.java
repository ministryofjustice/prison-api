package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AttendanceRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository repository;

    @Test
    void getActivitiesByBookingIdAndProgramStatus() {
        final var activities = repository.findByBookingIdsAndEventDate(List.of(-3L), LocalDate.of(2010, 1, 1), LocalDate.now());

        activities.sort((o1, o2) -> (int) (o2.getEventId() - o1.getEventId()));
        System.out.println("SIZE: " + activities.size());
        System.out.println("First Object: " + activities.get(0).getEventId());
        System.out.println("\tId: " + activities.get(0).getEventId());
        System.out.println("\tProgProfId: " + activities.get(0).getOffenderProgramProfile().getOffenderProgramReferenceId());
        System.out.println("\tProgProfStart: " + activities.get(0).getOffenderProgramProfile().getStartDate());
    }
}
