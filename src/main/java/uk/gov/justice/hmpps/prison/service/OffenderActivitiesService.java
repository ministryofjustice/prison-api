package uk.gov.justice.hmpps.prison.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;

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
        final var workActivities =  allAllocatedWorkActivities.stream().filter(OffenderProgramProfile::isCurrentWorkActivity).map(this::transform).collect(Collectors.toList());
        return OffenderActivities.builder()
            .offenderNo(offenderNo)
            .bookingId(currentBookingId)
            .workActivities(workActivities)
            .build();
    }

    private OffenderActivitySummary transform(final OffenderProgramProfile allocatedWorkActivity) {
        return OffenderActivitySummary.builder()
            .description(allocatedWorkActivity.getCourseActivity().getDescription())
            .startDate(allocatedWorkActivity.getStartDate())
            .build();
    }
}
