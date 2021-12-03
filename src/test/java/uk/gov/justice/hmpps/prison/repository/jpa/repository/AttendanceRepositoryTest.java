package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;

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
        final var activities = repository.findByEventDateBetween("A1234AB", LocalDate.of(2010, 1, 1),
            LocalDate.now(), PageRequest.of(1, 4, Direction.ASC, "eventId"));

        assertThat(activities.getTotalElements()).isEqualTo(7);
        assertThat(activities.getContent()).asList().extracting("eventId",
            "eventDate",
            "eventOutcome",
            "courseActivity.activityId",
            "courseActivity.description",
            "courseActivity.code",
            "courseActivity.scheduleStartDate",
            "courseActivity.scheduleEndDate",
            "offenderBooking.bookingId",
            "programService.programId").contains(
            Tuple.tuple(-13L, LocalDate.of(2017, 9, 13), "UNACAB", -1L, "Chapel Cleaner", "CC1", LocalDate.of(2016, 8, 8), LocalDate.of(2016, 9, 8), -2L, null),
            Tuple.tuple(-12L, LocalDate.now(), null, -3L, "Substance misuse course", "SUBS", LocalDate.of(2011, 01, 04), LocalDate.of(2012, 01, 8), -2L, null),
            Tuple.tuple(-11L, LocalDate.now(), null, -2L, "Woodwork", "WOOD", LocalDate.of(2012, 02, 28), LocalDate.of(2012, 03, 01), -2L, null)
        );
    }
}
