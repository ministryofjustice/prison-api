package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;

import java.time.LocalDate;
import java.util.List;

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

    @Nested
    class GetCurrentWorkActivities {
        @Test
        public void returnsCorrectApiObject() {
            when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenReturn(OffenderSummary.builder()
                .bookingId(EXAMPLE_BOOKING_ID)
                .build());

            when(repository.findByOffenderBooking_BookingIdAndProgramStatus(EXAMPLE_BOOKING_ID, "ALLOC")).thenReturn(List.of(
                OffenderProgramProfile.builder()
                    .offenderBooking(OffenderBooking.builder()
                        .bookingId(EXAMPLE_BOOKING_ID)
                        .build())
                    .agencyLocation(AgencyLocation.builder()
                        .id("MDI")
                        .build())
                    .offenderProgramReferenceId(-6L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .endDate(LocalDate.now().plusDays(2))
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
                .workActivities(List.of(
                    OffenderActivitySummary.builder()
                        .bookingId(EXAMPLE_BOOKING_ID)
                        .agencyLocationId("MDI")
                        .description("Woodwork")
                        .startDate(LocalDate.of(2016, 11, 9))
                        .endDate(LocalDate.now().plusDays(2))
                        .build()
                ))
                .build()
            );
        }

        @Test
        public void filtersOutInvalidOffenderProgramProfiles() {
            final var programProfileWithNoCourseActivity = OffenderProgramProfile.builder()
                .offenderBooking(OffenderBooking.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .build())
                .agencyLocation(AgencyLocation.builder()
                    .id("MDI")
                    .build())
                .offenderProgramReferenceId(-1L)
                .programStatus("ALLOC")
                .startDate(LocalDate.now().plusDays(1))
                .courseActivity(null)
                .build();
            final var programProfileWithValidData = OffenderProgramProfile.builder()
                .offenderBooking(OffenderBooking.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .build())
                .offenderProgramReferenceId(-2L)
                .agencyLocation(AgencyLocation.builder()
                    .id("MDI")
                    .build())
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
                .workActivities(List.of(
                    OffenderActivitySummary.builder()
                        .bookingId(EXAMPLE_BOOKING_ID)
                        .agencyLocationId(programProfileWithValidData.getAgencyLocation().getId())
                        .description(programProfileWithValidData.getCourseActivity().getDescription())
                        .startDate(programProfileWithValidData.getStartDate())
                        .endDate(programProfileWithValidData.getEndDate())
                        .build()
                ))
                .build()
            );
        }

        @Test
        public void handlesMinimalNonNullValues() {
            when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenReturn(OffenderSummary.builder()
                .bookingId(EXAMPLE_BOOKING_ID)
                .build());

            when(repository.findByOffenderBooking_BookingIdAndProgramStatus(EXAMPLE_BOOKING_ID, "ALLOC")).thenReturn(List.of());

            final var workActivitiesApiObject = service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO);

            assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
                .offenderNo(EXAMPLE_OFFENDER_NO)
                .workActivities(List.of())
                .build()
            );
        }

        @Test
        public void throwsEntityNotFoundIfNoMatch() {
            when(bookingService.getLatestBookingByOffenderNo(EXAMPLE_OFFENDER_NO)).thenThrow(new EntityNotFoundException("Not found"));

            assertThatThrownBy(() -> service.getCurrentWorkActivities(EXAMPLE_OFFENDER_NO)).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class GetStartedWorkActivities {
        @Test
        public void returnsCorrectApiObject() {
            final var earliestEndDate = LocalDate.of(2020, 1, 1);

            final var returnedOffenderProgramProfiles = List.of(
                OffenderProgramProfile.builder()
                    .offenderBooking(OffenderBooking.builder()
                        .bookingId(EXAMPLE_BOOKING_ID)
                        .build())
                    .agencyLocation(AgencyLocation.builder()
                        .id("MDI")
                        .build())
                    .offenderProgramReferenceId(-6L)
                    .programStatus("ALLOC")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .endDate(LocalDate.of(2021, 1, 1))
                    .courseActivity(CourseActivity.builder()
                        .activityId(-2L)
                        .description("Woodwork")
                        .code("WOOD")
                        .scheduleStartDate(LocalDate.of(2012, 2, 28))
                        .build())
                    .build()
            );
            when(repository.findByNomisIdAndProgramStatusAndEndDateAfter(EXAMPLE_OFFENDER_NO, List.of("ALLOC", "END"), earliestEndDate))
                .thenReturn(new PageImpl<>(returnedOffenderProgramProfiles));

            final var workActivitiesApiObject = service.getStartedWorkActivities(EXAMPLE_OFFENDER_NO, earliestEndDate);

            assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
                .offenderNo(EXAMPLE_OFFENDER_NO)
                .workActivities(List.of(
                    OffenderActivitySummary.builder()
                        .bookingId(EXAMPLE_BOOKING_ID)
                        .agencyLocationId("MDI")
                        .description("Woodwork")
                        .startDate(LocalDate.of(2016, 11, 9))
                        .endDate(LocalDate.of(2021, 1, 1))
                        .build()
                ))
                .build()
            );
        }

        @Test
        public void filtersOutInvalidOffenderProgramProfiles() {
            final var earliestEndDate = LocalDate.of(2020, 1, 1);

            final var programProfileWithEducationCourseActivity = OffenderProgramProfile.builder()
                .offenderBooking(OffenderBooking.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .build())
                .agencyLocation(AgencyLocation.builder()
                    .id("MDI")
                    .build())
                .offenderProgramReferenceId(-1L)
                .programStatus("ALLOC")
                .startDate(LocalDate.now().plusDays(1))
                .courseActivity(CourseActivity.builder()
                    .activityId(-1L)
                    .description("A description")
                    .code("EDU_CODE")
                    .scheduleStartDate(LocalDate.now().minusDays(10))
                    .build())
                .build();
            final var programProfileWithValidData = OffenderProgramProfile.builder()
                .offenderBooking(OffenderBooking.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .build())
                .offenderProgramReferenceId(-1L)
                .agencyLocation(AgencyLocation.builder()
                    .id("LEI")
                    .build())
                .programStatus("ALLOC")
                .startDate(LocalDate.now().minusDays(10))
                .courseActivity(CourseActivity.builder()
                    .activityId(-2L)
                    .description("A description")
                    .code("VALID")
                    .scheduleStartDate(LocalDate.now().minusDays(10))
                    .build())
                .build();

            final var returnedOffenderProgramProfiles = List.of(
                programProfileWithEducationCourseActivity,
                programProfileWithValidData
            );
            when(repository.findByNomisIdAndProgramStatusAndEndDateAfter(EXAMPLE_OFFENDER_NO, List.of("ALLOC", "END"), earliestEndDate))
                .thenReturn(new PageImpl<>(returnedOffenderProgramProfiles));

            final var workActivitiesApiObject = service.getStartedWorkActivities(EXAMPLE_OFFENDER_NO, earliestEndDate);

            assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
                .offenderNo(EXAMPLE_OFFENDER_NO)
                .workActivities(List.of(
                    OffenderActivitySummary.builder()
                        .bookingId(EXAMPLE_BOOKING_ID)
                        .agencyLocationId(programProfileWithValidData.getAgencyLocation().getId())
                        .description(programProfileWithValidData.getCourseActivity().getDescription())
                        .startDate(programProfileWithValidData.getStartDate())
                        .endDate(programProfileWithValidData.getEndDate())
                        .build()
                ))
                .build()
            );
        }

        @Test
        public void handlesMinimalNonNullValues() {
            final var earliestEndDate = LocalDate.of(2020, 1, 1);

            when(repository.findByNomisIdAndProgramStatusAndEndDateAfter(EXAMPLE_OFFENDER_NO, List.of("ALLOC", "END"), earliestEndDate))
                .thenReturn(new PageImpl<>(List.of()));

            final var workActivitiesApiObject = service.getStartedWorkActivities(EXAMPLE_OFFENDER_NO, earliestEndDate);

            assertThat(workActivitiesApiObject).isEqualTo(OffenderActivities.builder()
                .offenderNo(EXAMPLE_OFFENDER_NO)
                .workActivities(List.of())
                .build()
            );
        }
    }
}
