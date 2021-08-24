package uk.gov.justice.hmpps.prison.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderAttendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AttendanceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WIPOffenderActivitiesService {
    private final AttendanceRepository attendanceRepository;
    private final OffenderBookingRepository bookingRepository;

    public WIPOffenderActivitiesService(final AttendanceRepository attendanceRepository, final OffenderBookingRepository bookingRepository) {
        this.attendanceRepository = attendanceRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<OffenderAttendance> getHistoricalActivities(final String offenderNo, final LocalDate earliestActivityDate, final LocalDate latestActivityDate) {
        final var relevantBookingIds = bookingRepository.findByDates(offenderNo, earliestActivityDate.atStartOfDay(), latestActivityDate.plusDays(1).atStartOfDay()).stream().map(o -> o.getBookingId()).collect(Collectors.toList());
        final var scheduledHistoricalActivities = attendanceRepository.findByBookingIdsAndEventDate(relevantBookingIds, earliestActivityDate, latestActivityDate);
        return scheduledHistoricalActivities.stream().map(this::transform).collect(Collectors.toList());
    }

    private OffenderAttendance transform(Attendance attendance) {
        return OffenderAttendance.builder()
            .bookingId(attendance.getOffenderBookingId())
            .eventDate(attendance.getEventDate())
            .outcome(attendance.getEventOutcome())
            .code(attendance.getCourseActivity().getCode())
            .description(attendance.getCourseActivity().getDescription())
            .build();
    }
}
