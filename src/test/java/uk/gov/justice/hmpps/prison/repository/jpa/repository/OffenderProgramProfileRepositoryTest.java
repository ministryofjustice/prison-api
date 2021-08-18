package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderProgramProfileRepositoryTest {

    @Autowired
    private OffenderProgramProfileRepository repository;

    @Test
    void getActivitiesByBookingIdAndProgramStatus() {
        final var activities = repository.findByOffenderBooking_BookingIdAndProgramStatus(-3L, "ALLOC");

        activities.sort((o1, o2) -> (int) (o2.getOffenderProgramReferenceId() - o1.getOffenderProgramReferenceId()));
        assertThat(activities).usingElementComparatorIgnoringFields("offenderProgramReferenceId",
                "offenderBooking", "createUserId", "createDatetime")
            .isEqualTo(List.of(
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-6L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .endDate(LocalDate.of(2021, 8, 8))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-2L)
                        .description("Woodwork")
                        .code("WOOD")
                        .scheduleStartDate(LocalDate.of(2012, 2, 28))
                        .scheduleEndDate(LocalDate.of(2021, 8, 9))
                        .build())
                    .build(),
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-9L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-5L)
                        .description("Weeding")
                        .code("FG1")
                        .scheduleStartDate(LocalDate.of(2009, 7, 4))
                        .build())
                    .build(),
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-10L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-6L)
                        .description("Address Testing")
                        .code("ABS")
                        .scheduleStartDate(LocalDate.of(2009, 7, 4))
                        .build())
                    .build(),
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-11L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-3L)
                        .description("Substance misuse course")
                        .code("SUBS")
                        .scheduleStartDate(LocalDate.of(2011, 1, 4))
                        .build())
                    .build()
                )
        );
    }

    @Test
    void getActivitiesByBookingIdAndProgramStatus_ReturnsNothing() {
        final var activities = repository.findByOffenderBooking_BookingIdAndProgramStatus(-3L, "WAIT");

        assertThat(activities).isEmpty();
    }
}


