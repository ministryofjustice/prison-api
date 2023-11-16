package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class BedAssignmentHistoryService {

    private final BedAssignmentHistoriesRepository repository;
    private final AgencyInternalLocationRepository locationRepository;
    private final int maxBatchSize;

    public BedAssignmentHistoryService(
        final BedAssignmentHistoriesRepository repository,
        final AgencyInternalLocationRepository locationRepository,
        @Value("${batch.max.size:1000}") final int maxBatchSize) {

        this.repository = repository;
        this.locationRepository = locationRepository;
        this.maxBatchSize = maxBatchSize;
    }

    public BedAssignmentHistoryPK add(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime time) {
        final var maxSequence = repository.getMaxSeqForBookingId(bookingId);
        final var bookingAndSequence = new BedAssignmentHistoryPK(bookingId, maxSequence + 1);
        final var bedAssignmentHistory =
            BedAssignmentHistory.builder()
                .bedAssignmentHistoryPK(bookingAndSequence)
                .livingUnitId(livingUnitId)
                .assignmentDate(time.toLocalDate())
                .assignmentDateTime(time)
                .assignmentReason(reasonCode)
                .build();
        repository.save(bedAssignmentHistory);
        log.info("Added bed assignment history for offender booking id {} to living unit id {}", bookingId, livingUnitId);

        return bookingAndSequence;
    }

    public Page<BedAssignment> getBedAssignmentsHistory(final Long bookingId, final PageRequest pageRequest) {
        final var bedAssignmentsHistory = repository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId, pageRequest);
        final var results = bedAssignmentsHistory.getContent()
            .stream()
            .map(this::transform)
            .toList();

        return new PageImpl<>(results, pageRequest, bedAssignmentsHistory.getTotalElements());
    }

    public List<BedAssignment> getBedAssignmentsHistory(final long livingUnitId, final LocalDateTime from, final LocalDateTime to) {
        if (from.isAfter(to))
            throw new IllegalArgumentException("The fromDate should be less then or equal to the toDate");
        if (!locationRepository.existsById(livingUnitId))
            throw new EntityNotFoundException(String.format("Cell %s not found", livingUnitId));

        return repository
            .findByLivingUnitIdAndDateTimeRange(livingUnitId, from, to)
            .stream()
            .map(this::transform)
            .toList();
    }

    public List<BedAssignment> getBedAssignmentsHistoryByDateForAgency(final String agencyId, final LocalDate assignmentDate) {
        final var livingUnitIdsForAgency = locationRepository.findAgencyInternalLocationsByAgencyIdAndLocationType(agencyId, "CELL")
            .stream()
            .map(AgencyInternalLocation::getLocationId)
            .collect(Collectors.toSet());

        final var livingUnitIdsBatched = Lists.partition(new ArrayList<>(livingUnitIdsForAgency), maxBatchSize);

        return livingUnitIdsBatched
            .stream()
            .flatMap(livingUnitIds -> repository.findBedAssignmentHistoriesByAssignmentDateAndLivingUnitIdIn(assignmentDate, new HashSet<>(livingUnitIds)).stream())
            .map(this::transform)
            .toList();
    }

    private BedAssignment transform(final BedAssignmentHistory assignment) {
        final var agencyInternalLocation = Optional.ofNullable(assignment.getLocation());
        final var agencyId = agencyInternalLocation.map(AgencyInternalLocation::getAgencyId).orElse(null);
        final var agencyDescription = agencyInternalLocation.map(AgencyInternalLocation::getDescription).orElse(null);

        final var offenderNo = Optional.ofNullable(assignment.getOffenderBooking())
            .map(OffenderBooking::getOffender)
            .map(uk.gov.justice.hmpps.prison.repository.jpa.model.Offender::getNomsId)
            .orElse(null);

        return BedAssignment.builder()
            .livingUnitId(assignment.getLivingUnitId())
            .agencyId(agencyId)
            .description(agencyDescription)
            .assignmentDate(assignment.getAssignmentDate())
            .assignmentEndDate(assignment.getAssignmentEndDate())
            .assignmentDateTime(assignment.getAssignmentDateTime())
            .assignmentEndDateTime(assignment.getAssignmentEndDateTime())
            .assignmentReason(assignment.getAssignmentReason())
            .bookingId(assignment.getOffenderBooking().getBookingId())
            .bedAssignmentHistorySequence(assignment.getBedAssignmentHistoryPK().getSequence())
            .movementMadeBy(assignment.movementMadeBy())
            .offenderNo(offenderNo)
            .build();
    }
}
