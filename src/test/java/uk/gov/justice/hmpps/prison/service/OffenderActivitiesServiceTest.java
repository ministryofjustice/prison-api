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
        final var programProfileWithNoCourseActivity = OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-1L)
            .programStatus("ALLOC")
            .startDate(LocalDate.now().plusDays(1))
            .courseActivity(null)
            .build();
        final var programProfileWithValidData = OffenderProgramProfile.builder()
            .offenderProgramReferenceId(-1L)
            .programStatus("ALLOC")
            .startDate(LocalDate.now().minusDays(10))
            .courseActivity(CourseActivity.builder()
                .activityId(-2L)
                .description("A description")
                .code("VALID")
                .scheduleStartDate(LocalDate.now().minusDays(10))
                .build())
            .build();

        when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenReturn(OffenderSummary.builder()
            .bookingId(EXAMPLE_BOOKING_ID)
            .build());

        when(repository.findByOffenderBooking_BookingIdAndProgramStatus(EXAMPLE_BOOKING_ID, "ALLOC")).thenReturn(List.of(
            programProfileWithNoCourseActivity,
            programProfileWithValidData
        ));

        final var workActivitiesApiObject = service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO);

        assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
            .offenderNo(EXAMPLE_OFFENDER_NO)
            .bookingId(EXAMPLE_BOOKING_ID)
            .workActivities(List.of(
                OffenderActivitySummary.builder()
                    .description(programProfileWithValidData.getCourseActivity().getDescription())
                    .startDate(programProfileWithValidData.getStartDate())
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
}
