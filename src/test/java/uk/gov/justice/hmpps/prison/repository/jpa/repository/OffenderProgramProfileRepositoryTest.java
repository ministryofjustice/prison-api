package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
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
public class OffenderProgramProfileRepositoryTest {

    @Autowired
    private OffenderProgramProfileRepository repository;

    @Test
    void getActivitiesByBookingIdAndProgramStatus() {
        final var activities = repository.findByOffenderBooking_BookingIdAndProgramStatus(-3L, "ALLOC");

        activities.sort((o1, o2) -> (int) (o2.getOffenderProgramReferenceId() - o1.getOffenderProgramReferenceId()));
        assertThat(activities).usingElementComparatorIgnoringFields("offenderProgramReferenceId",
                "offenderBooking", "agencyLocation", "createUserId", "createDatetime")
            .isEqualTo(List.of(
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-6L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-2L)
                        .description("Woodwork")
                        .code("WOOD")
                        .scheduleStartDate(LocalDate.of(2012, 2, 28))
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

        final var locationIds = activities.stream().map(o -> o.getAgencyLocation().getId()).collect(Collectors.toSet());
        assertThat(locationIds).containsExactly("LEI");
    }

    @Test
    void getActivitiesByBookingIdAndProgramStatus_ReturnsNothing() {
        final var activities = repository.findByOffenderBooking_BookingIdAndProgramStatus(-3L, "WAIT");

        assertThat(activities).isEmpty();
    }

    @Test
    void findByNomisIdAndProgramStatusAndEndDateAfter() {
        final var activities = repository.findByNomisIdAndProgramStatusAndEndDateAfter("A1234AC", List.of("ALLOC", "END"), LocalDate.of(2021, 1, 1), Pageable.ofSize(100)).stream()
            .sorted((o1, o2) -> (int) (o2.getOffenderProgramReferenceId() - o1.getOffenderProgramReferenceId()))
            .collect(Collectors.toList());

        assertThat(activities).usingElementComparatorIgnoringFields("offenderProgramReferenceId",
            "offenderBooking", "agencyLocation", "createUserId", "createDatetime")
            .isEqualTo(List.of(
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-6L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-2L)
                        .description("Woodwork")
                        .code("WOOD")
                        .scheduleStartDate(LocalDate.of(2012, 2, 28))
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
                    .build(),
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-14L)
                    .programStatus("END")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .endDate(LocalDate.of(2021, 8, 21))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-4L)
                        .description("Core classes")
                        .code("CORE")
                        .scheduleStartDate(LocalDate.of(2009, 7, 4))
                        .build())
                    .build()
                )
            );

        final var locationIds = activities.stream().map(o -> o.getAgencyLocation().getId()).collect(Collectors.toSet());
        assertThat(locationIds).containsExactly("LEI");
    }

    @Test
    void findByNomisIdAndProgramStatusAndEndDateAfter_OnlyFiltersOutEarlierEndDates() {
        final var activities = repository.findByNomisIdAndProgramStatusAndEndDateAfter("A1234AC", List.of("PLAN"), LocalDate.of(2021, 1, 1), Pageable.ofSize(100)).stream()
            .sorted((o1, o2) -> (int) (o2.getOffenderProgramReferenceId() - o1.getOffenderProgramReferenceId()))
            .collect(Collectors.toList());

        assertThat(activities).usingElementComparatorIgnoringFields("offenderProgramReferenceId",
            "offenderBooking", "agencyLocation", "createUserId", "createDatetime")
            .isEqualTo(List.of(
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-3101L)
                    .programStatus("PLAN")
                    .courseActivity(CourseActivity.builder()
                        .activityId(-1L)
                        .description("Chapel Cleaner")
                        .code("CC1")
                        .scheduleStartDate(LocalDate.of(2016, 8, 8))
                        .build())
                    .build(),
                OffenderProgramProfile.builder()
                    .offenderProgramReferenceId(-3102L)
                    .programStatus("PLAN")
                    .endDate(LocalDate.of(2021, 1, 1))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-3001L)
                        .description("Gym session 1")
                        .code("ABS")
                        .scheduleStartDate(LocalDate.of(2009, 7, 4))
                        .build())
                    .build()
                )
            );
    }

    @Test
    void findByNomisIdAndProgramStatusAndEndDateAfter_Pagination() {
        final var firstPageRequest = Pageable.ofSize(2);
        final var activitiesFirstPage = repository.findByNomisIdAndProgramStatusAndEndDateAfter("A1234AC", List.of("ALLOC", "END"), LocalDate.of(2021, 1, 1), firstPageRequest).getContent();
        final var activitiesSecondPage = repository.findByNomisIdAndProgramStatusAndEndDateAfter("A1234AC", List.of("ALLOC", "END"), LocalDate.of(2021, 1, 1), firstPageRequest.next()).getContent();

        final var firstPageCourseIds = activitiesFirstPage.stream().map(o -> o.getCourseActivity().getActivityId()).collect(Collectors.toSet());
        final var secondPageCourseIds = activitiesSecondPage.stream().map(o -> o.getCourseActivity().getActivityId()).collect(Collectors.toSet());

        assertThat(secondPageCourseIds).isNotEmpty();
        assertThat(firstPageCourseIds).doesNotContainAnyElementsOf(secondPageCourseIds);
    }

    @Test
    void findByNomisIdAndProgramStatusAndEndDateAfter_ReturnsNothing() {
        final var activities = repository.findByNomisIdAndProgramStatusAndEndDateAfter("A1234AC", List.of("WAIT"), LocalDate.of(2020, 1, 1), Pageable.ofSize(1));

        assertThat(activities).isEmpty();
    }
}


