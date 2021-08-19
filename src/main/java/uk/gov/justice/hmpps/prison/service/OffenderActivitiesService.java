package uk.gov.justice.hmpps.prison.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OffenderActivitiesService {
    private final OffenderProgramProfileRepository offenderProgramProfileRepository;
    private final BookingService bookingService;

    public OffenderActivitiesService(final OffenderProgramProfileRepository offenderProgramProfileRepository, final BookingService bookingService) {
        this.offenderProgramProfileRepository = offenderProgramProfileRepository;
        this.bookingService = bookingService;
    }

    public OffenderActivities getCurrentWorkActivities(String offenderNo) {
        final var currentBookingId = bookingService.getLatestBookingByOffenderNo(offenderNo).getBookingId();
        final var allAllocatedWorkActivities = offenderProgramProfileRepository.findByOffenderBooking_BookingIdAndProgramStatus(currentBookingId, "ALLOC");
        final var workActivities =  allAllocatedWorkActivities.stream().filter(this::isValidCurrentActivity).map(this::transform).collect(Collectors.toList());
        return OffenderActivities.builder()
            .offenderNo(offenderNo)
            .bookingId(currentBookingId)
            .workActivities(workActivities)
            .build();
    }

    private boolean isValidCurrentActivity(OffenderProgramProfile offenderProgramProfile) {
        final var currentDate = LocalDate.now();
        final var courseActivity = offenderProgramProfile.getCourseActivity();
        final var isCurrentProgramProfile = offenderProgramProfile.getStartDate() != null && offenderProgramProfile.getStartDate().isBefore(currentDate.plusDays(1))
            && (offenderProgramProfile.getEndDate() == null || offenderProgramProfile.getEndDate().isAfter(currentDate));
        final var isValidCurrentActivity = courseActivity != null &&
            courseActivity.getScheduleStartDate() != null && courseActivity.getScheduleStartDate().isBefore(currentDate.plusDays(1))
            && (courseActivity.getScheduleEndDate() == null || courseActivity.getScheduleEndDate().isAfter(currentDate))
            && courseActivity.getCode() != null && !courseActivity.getCode().startsWith("EDU");
        return isCurrentProgramProfile && isValidCurrentActivity;
    }

    private OffenderActivitySummary transform(final OffenderProgramProfile allocatedWorkActivity) {
        return OffenderActivitySummary.builder()
            .description(allocatedWorkActivity.getCourseActivity().getDescription())
            .startDate(allocatedWorkActivity.getStartDate())
            .build();
    }
}
