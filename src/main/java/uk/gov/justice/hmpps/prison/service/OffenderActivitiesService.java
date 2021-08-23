package uk.gov.justice.hmpps.prison.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;

import java.time.LocalDate;
import java.util.List;
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

    public OffenderActivities getCurrentWorkActivities(final String offenderNo) {
        final var currentBookingId = bookingService.getLatestBookingByOffenderNo(offenderNo).getBookingId();
        final var allAllocatedActivities = offenderProgramProfileRepository.findByOffenderBooking_BookingIdAndProgramStatus(currentBookingId, "ALLOC");
        final var workActivities =  allAllocatedActivities.stream().filter(OffenderProgramProfile::isCurrentWorkActivity).map(this::transform).collect(Collectors.toList());
        return OffenderActivities.builder()
            .offenderNo(offenderNo)
            .workActivities(workActivities)
            .build();
    }

    public OffenderActivities getStartedWorkActivities(final String offenderNo, final LocalDate earliestEndDate) {
        final var startedActivities = offenderProgramProfileRepository.findByNomisIdAndProgramStatusAndEndDateAfter(offenderNo, List.of("ALLOC", "END"), earliestEndDate);
        final var startedWorkActivities =  startedActivities.stream().filter(OffenderProgramProfile::isWorkActivity).map(this::transform).collect(Collectors.toList());
        return OffenderActivities.builder()
            .offenderNo(offenderNo)
            .workActivities(startedWorkActivities)
            .build();
    }

    private OffenderActivitySummary transform(final OffenderProgramProfile activity) {
        return OffenderActivitySummary.builder()
            .bookingId(activity.getOffenderBooking().getBookingId())
            .agencyLocationId(activity.getAgencyLocation().getId())
            .description(activity.getCourseActivity().getDescription())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate())
            .build();
    }
}
