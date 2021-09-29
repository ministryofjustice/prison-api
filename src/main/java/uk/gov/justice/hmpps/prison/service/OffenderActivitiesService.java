package uk.gov.justice.hmpps.prison.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivities;
import uk.gov.justice.hmpps.prison.api.model.OffenderActivitySummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

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

    public Page<OffenderActivitySummary> getStartedActivities(final String offenderNo, final LocalDate earliestEndDate, final PageRequest pageRequest) {
        final var startedActivities = offenderProgramProfileRepository.findByNomisIdAndProgramStatusAndEndDateAfter(offenderNo, List.of("ALLOC", "END"), earliestEndDate,
            pageRequest);
        return startedActivities.map(this::transform);
    }

    private OffenderActivitySummary transform(final OffenderProgramProfile activity) {
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
}
