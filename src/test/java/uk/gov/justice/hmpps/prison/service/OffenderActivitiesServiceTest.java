package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity.CourseActivityBuilder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile.OffenderProgramProfileBuilder;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderActivitiesServiceTest {
    private static final String EXAMPLE_OFFENDER_NO = "A1234AA";
    private static final Long EXAMPLE_BOOKING_ID = -33L;

    private final Random randomNumberGenerator = new Random();

    @Mock
    private OffenderProgramProfileRepository repository;
    @Mock
    private BookingService bookingService;

    private OffenderActivitiesService service;

    @BeforeEach
    public void beforeEach() {
        service = new OffenderActivitiesService(repository, bookingService);
    }

    @Test
    public void getCurrentWorkActivities_returnsCorrectApiObject() {
        when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenReturn(OffenderSummary.builder()
            .bookingId(EXAMPLE_BOOKING_ID)
            .build());

        when(repository.findByOffenderBooking_BookingIdAndProgramStatus(EXAMPLE_BOOKING_ID, "ALLOC")).thenReturn(List.of(
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
                .build()
        ));

        final var workActivitiesApiObject = service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO);

        assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
            .offenderNo(EXAMPLE_OFFENDER_NO)
            .bookingId(EXAMPLE_BOOKING_ID)
            .workActivities(List.of(
                OffenderActivitySummary.builder()
                    .description("Woodwork")
                    .startDate(LocalDate.of(2016, 11, 9))
                .build()
            ))
            .build()
        );
    }

    @Test
    public void getCurrentWorkActivities_filtersOutInvalidOffenderProgramProfiles() {
        final var programProfileWithNoStartDate =
            programProfileBuilder()
                .startDate(null)
                .build();
        final var programProfileWithStartDateTomorrow =
            programProfileBuilder()
                .startDate(LocalDate.now().plusDays(1))
                .build();
        final var programProfileWithEndDateToday =
            programProfileBuilder()
                .endDate(LocalDate.now())
                .build();
        final var programProfileWithValidStartAndEndDate =
            programProfileBuilder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();
        final var programProfileWithValidStartAndNoEndDate =
            programProfileBuilder()
                .startDate(LocalDate.now())
                .build();
        final var programProfileWithoutCourseActivity =
            programProfile(null);

        when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenReturn(OffenderSummary.builder()
            .bookingId(EXAMPLE_BOOKING_ID)
            .build());

        when(repository.findByOffenderBooking_BookingIdAndProgramStatus(EXAMPLE_BOOKING_ID, "ALLOC")).thenReturn(List.of(
            programProfileWithNoStartDate,
            programProfileWithStartDateTomorrow,
            programProfileWithEndDateToday,
            programProfileWithValidStartAndEndDate,
            programProfileWithValidStartAndNoEndDate,
            programProfileWithoutCourseActivity
        ));

        final var workActivitiesApiObject = service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO);

        assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
            .offenderNo(EXAMPLE_OFFENDER_NO)
            .bookingId(EXAMPLE_BOOKING_ID)
            .workActivities(List.of(
                OffenderActivitySummary.builder()
                    .description(programProfileWithValidStartAndEndDate.getCourseActivity().getDescription())
                    .startDate(programProfileWithValidStartAndEndDate.getStartDate())
                    .build(),
                OffenderActivitySummary.builder()
                    .description(programProfileWithValidStartAndNoEndDate.getCourseActivity().getDescription())
                    .startDate(programProfileWithValidStartAndNoEndDate.getStartDate())
                    .build()
            ))
            .build()
        );
    }

    @Test
    public void getCurrentWorkActivities_filtersOutInvalidCourseActivities() {
        final var courseActivityWithNoStartDate =
            courseActivityBuilder("NO START DATE")
            .scheduleStartDate(null)
            .build();
        final var courseActivityWithStartDateToday =
            courseActivityBuilder("START DATE TODAY")
            .scheduleStartDate(LocalDate.now())
            .build();
        final var courseActivityWithStartDateAfterToday =
            courseActivityBuilder("START DATE AFTER TODAY")
            .scheduleStartDate(LocalDate.now().plusDays(1))
            .build();
        final var courseActivityWithNoEndDate =
            courseActivityBuilder("NO END DATE")
            .scheduleEndDate(null)
            .build();
        final var courseActivityWithEndDateAfterToday =
            courseActivityBuilder("END DATE AFTER TODAY")
            .scheduleEndDate(LocalDate.now().plusDays(1))
            .build();
        final var courseActivityWithEndDateToday =
            courseActivityBuilder("END DATE TODAY")
            .scheduleEndDate(LocalDate.now())
            .build();
        final var courseActivityWithNoCode =
            courseActivityBuilder("NO CODE")
            .code(null)
            .build();
        final var courseActivityWithEDUCode =
            courseActivityBuilder("EDU CODE")
            .code("EDUEXAMPLE")
            .build();

        when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenReturn(OffenderSummary.builder()
            .bookingId(EXAMPLE_BOOKING_ID)
            .build());

        when(repository.findByOffenderBooking_BookingIdAndProgramStatus(EXAMPLE_BOOKING_ID, "ALLOC")).thenReturn(List.of(
            programProfile(courseActivityWithNoStartDate),
            programProfile(courseActivityWithStartDateToday),
            programProfile(courseActivityWithStartDateAfterToday),
            programProfile(courseActivityWithNoEndDate),
            programProfile(courseActivityWithEndDateAfterToday),
            programProfile(courseActivityWithEndDateToday),
            programProfile(courseActivityWithNoCode),
            programProfile(courseActivityWithEDUCode)
        ));

        final var workActivitiesApiObject = service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO);

        assertThat(workActivitiesApiObject).usingRecursiveComparison()
            .ignoringFields("workActivities.startDate")
            .isEqualTo(OffenderActivities.builder()
            .offenderNo(EXAMPLE_OFFENDER_NO)
            .bookingId(EXAMPLE_BOOKING_ID)
            .workActivities(List.of(
                OffenderActivitySummary.builder()
                    .description(courseActivityWithStartDateToday.getDescription())
                    .build(),
                OffenderActivitySummary.builder()
                    .description(courseActivityWithNoEndDate.getDescription())
                    .build(),
                OffenderActivitySummary.builder()
                    .description(courseActivityWithEndDateAfterToday.getDescription())
                    .build()
            ))
            .build()
        );
    }

    @Test
    public void getCurrentWorkActivities_handlesMinimalNonNullValues() {
        when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenReturn(OffenderSummary.builder()
            .bookingId(EXAMPLE_BOOKING_ID)
            .build());

        when(repository.findByOffenderBooking_BookingIdAndProgramStatus(EXAMPLE_BOOKING_ID, "ALLOC")).thenReturn(List.of());

        final var workActivitiesApiObject = service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO);

        assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
            .offenderNo(EXAMPLE_OFFENDER_NO)
            .bookingId(EXAMPLE_BOOKING_ID)
            .workActivities(List.of())
            .build()
        );
    }

    @Test
    public void getCurrentWorkActivities_throwsEntityNotFoundIfNoMatch() {
        when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenThrow(new EntityNotFoundException("Not found"));

        assertThatThrownBy(() -> service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO)).isInstanceOf(EntityNotFoundException.class);
    }

    private OffenderProgramProfileBuilder programProfileBuilder() {
        return OffenderProgramProfile.builder()
            .offenderProgramReferenceId(nextLong())
            .programStatus("ALLOC")
            .startDate(LocalDate.now().minusDays(10))
            .courseActivity(courseActivityBuilder(String.format("A random desc %d", nextLong())).build());
    }

    private OffenderProgramProfile programProfile(final CourseActivity courseActivity) {
        return OffenderProgramProfile.builder()
            .offenderProgramReferenceId(nextLong())
            .programStatus("ALLOC")
            .startDate(LocalDate.now().minusDays(10))
            .courseActivity(courseActivity)
            .build();
    }

    private CourseActivityBuilder courseActivityBuilder(String description) {
        return CourseActivity.builder()
            .activityId(nextLong())
            .description(description)
            .code("VALID")
            .scheduleStartDate(LocalDate.now().minusDays(10));

    }

    private long nextLong() {
        return randomNumberGenerator.nextLong();
    }
}
