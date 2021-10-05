package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderAttendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AttendanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderActivitiesServiceTest {
    private static final String EXAMPLE_OFFENDER_NO = "A1234AA";
    private static final Long EXAMPLE_BOOKING_ID = -33L;
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 5);

    @Mock
    private OffenderProgramProfileRepository repository;
    @Mock
    private AttendanceRepository attendanceRepository;

    private OffenderActivitiesService service;

    @BeforeEach
    public void beforeEach() {
        service = new OffenderActivitiesService(repository, attendanceRepository);
    }

    @Nested
    class GetStartedWorkActivities {
        @Test
        public void returnsCorrectApiObject() {
            final var earliestEndDate = LocalDate.of(2020, 1, 1);

            final var currentWorkActivity = OffenderProgramProfile.builder()
                .offenderBooking(OffenderBooking.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .build())
                .agencyLocation(AgencyLocation.builder()
                    .id("MDI")
                    .description("MOORLAND (HMP & YOI)")
                    .build())
                .offenderProgramReferenceId(-6L)
                .programStatus("ALLOC")
                .startDate(LocalDate.of(2016, 11, 9))
                .courseActivity(CourseActivity.builder()
                    .activityId(-1L)
                    .description("Woodwork AM")
                    .code("WOOD")
                    .scheduleStartDate(LocalDate.of(2012, 2, 28))
                    .build())
                .endReason(new OffenderProgramEndReason("end code 1", "end description 1"))
                .endCommentText("end comment 1")
                .build();
            final var endedWorkActivity = OffenderProgramProfile.builder()
                .offenderBooking(OffenderBooking.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .build())
                .agencyLocation(AgencyLocation.builder()
                    .id("MDI")
                    .description("MOORLAND (HMP & YOI)")
                    .build())
                .offenderProgramReferenceId(-6L)
                .programStatus("END")
                .startDate(LocalDate.of(2016, 11, 9))
                .endDate(LocalDate.of(2021, 1, 1))
                .courseActivity(CourseActivity.builder()
                    .activityId(-2L)
                    .description("Woodwork PM")
                    .code("WOOD")
                    .scheduleStartDate(LocalDate.of(2012, 2, 28))
                    .build())
                .endReason(new OffenderProgramEndReason("end code 2", "end description 2"))
                .endCommentText("end comment 2")
                .build();

            final var returnedOffenderProgramProfiles = new PageImpl<>(List.of(currentWorkActivity, endedWorkActivity), PAGE_REQUEST, 0);
            when(repository.findByNomisIdAndProgramStatusAndEndDateAfter(EXAMPLE_OFFENDER_NO, List.of("ALLOC", "END"), earliestEndDate, PAGE_REQUEST))
                .thenReturn(returnedOffenderProgramProfiles);

            final var workActivitiesApiObject = service.getStartedActivities(EXAMPLE_OFFENDER_NO, earliestEndDate, PAGE_REQUEST);

            assertThat(workActivitiesApiObject).isEqualTo(new PageImpl<>(List.of(
                OffenderActivitySummary.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .agencyLocationId("MDI")
                    .agencyLocationDescription("Moorland (HMP & YOI)")
                    .description("Woodwork AM")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .endReasonCode("end code 1")
                    .endReasonDescription("end description 1")
                    .endCommentText("end comment 1")
                    .isCurrentActivity(true)
                    .build(),
                OffenderActivitySummary.builder()
                    .bookingId(EXAMPLE_BOOKING_ID)
                    .agencyLocationId("MDI")
                    .agencyLocationDescription("Moorland (HMP & YOI)")
                    .description("Woodwork PM")
                    .startDate(LocalDate.of(2016, 11, 9))
                    .endDate(LocalDate.of(2021, 1, 1))
                    .endReasonCode("end code 2")
                    .endReasonDescription("end description 2")
                    .endCommentText("end comment 2")
                    .isCurrentActivity(false)
                    .build()
            ), PAGE_REQUEST, 0));
        }

        @Test
        public void handlesMinimalNonNullValues() {
            final var earliestEndDate = LocalDate.of(2020, 1, 1);

            when(repository.findByNomisIdAndProgramStatusAndEndDateAfter(EXAMPLE_OFFENDER_NO, List.of("ALLOC", "END"), earliestEndDate, PAGE_REQUEST))
                .thenReturn(new PageImpl<>(List.of(), PAGE_REQUEST, 0));

            final var workActivitiesApiObject = service.getStartedActivities(EXAMPLE_OFFENDER_NO, earliestEndDate, PAGE_REQUEST);

            assertThat(workActivitiesApiObject.getContent()).isEmpty();
        }
    }

    @Nested
    class GetHistoricalAttendancies {
        @Test
        public void returnsCorrectApiObject() {
            final var earliestDate = LocalDate.of(2020, 1, 1);
            final var latestDate = LocalDate.of(2021, 2, 2);

            when(attendanceRepository.findByEventDateBetween(EXAMPLE_OFFENDER_NO, earliestDate, latestDate, PAGE_REQUEST))
                .thenReturn(new PageImpl<>(List.of(
                    new Attendance(1111L, null, earliestDate, "outcome1"),
                    new Attendance(2222L, null, latestDate, "outcome2")
                )));

            final var result = service.getHistoricalAttendancies(EXAMPLE_OFFENDER_NO, earliestDate, latestDate, PAGE_REQUEST);

            assertThat(result.getContent()).isEqualTo(List.of(
                OffenderAttendance.builder().eventDate(earliestDate).outcome("outcome1").build(),
                OffenderAttendance.builder().eventDate(latestDate).outcome("outcome2").build()
            ));
        }
    }
}
