package uk.gov.justice.hmpps.prison.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderAttendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AttendanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OffenderActivitiesService {
    private final OffenderProgramProfileRepository offenderProgramProfileRepository;
    private final AttendanceRepository attendanceRepository;

    public OffenderActivitiesService(final OffenderProgramProfileRepository offenderProgramProfileRepository, final AttendanceRepository attendanceRepository) {
        this.offenderProgramProfileRepository = offenderProgramProfileRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public Page<OffenderActivitySummary> getStartedActivities(final String offenderNo, final LocalDate earliestEndDate, final Pageable pageable) {
        final var startedActivities = offenderProgramProfileRepository.findByNomisIdAndProgramStatusAndEndDateAfter(
            offenderNo, List.of("ALLOC", "END"),
            earliestEndDate,
            pageable);
        return startedActivities.map(this::transformActivity);
    }

    private OffenderActivitySummary transformActivity(final OffenderProgramProfile activity) {
        return OffenderActivitySummary.builder()
            .bookingId(activity.getOffenderBooking().getBookingId())
            .agencyLocationId(activity.getAgencyLocation().getId())
            .agencyLocationDescription(LocationProcessor.formatLocation(activity.getAgencyLocation().getDescription()))
            .description(activity.getCourseActivity().getDescription())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate())
            .endReasonCode(ReferenceCode.getCodeOrNull(activity.getEndReason()))
            .endReasonDescription(ReferenceCode.getDescriptionOrNull(activity.getEndReason()))
            .endCommentText(activity.getEndCommentText())
            .isCurrentActivity(activity.isCurrentActivity())
            .build();
    }

    public Page<OffenderAttendance> getHistoricalAttendancies(final String offenderNo, final LocalDate earliestDate, final LocalDate latestDate,
                                                              final String outcome, final Pageable pageable) {
        return attendanceRepository.findByEventDateBetweenAndOutcome(offenderNo, earliestDate, latestDate, outcome, pageable).map(this::transformAttendance);
    }

    private OffenderAttendance transformAttendance(final Attendance attendance) {
        return OffenderAttendance.builder()
            .eventDate(attendance.getEventDate())
            .outcome(attendance.getEventOutcome())
            .description(attendance.getCourseActivity().getDescription())
            .prisonId(attendance.getCourseActivity().getPrisonId())
            .activity(attendance.getProgramService() == null? null : attendance.getProgramService().getActivity())
            .code(attendance.getCourseActivity().getCode())
            .bookingId(attendance.getOffenderBooking().getBookingId())
            .comment(attendance.getComment())
            .build();
    }
}
