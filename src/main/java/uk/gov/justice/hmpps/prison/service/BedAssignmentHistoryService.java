package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class BedAssignmentHistoryService {

    private final BedAssignmentHistoriesRepository repository;
    private final AgencyInternalLocationRepository locationRepository;

    public BedAssignmentHistoryService(final BedAssignmentHistoriesRepository repository, final AgencyInternalLocationRepository locationRepository) {
        this.repository = repository;
        this.locationRepository = locationRepository;
    }

    @VerifyBookingAccess
    @HasWriteScope
    public void add(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime time) {
        final var maxSequence = repository.getMaxSeqForBookingId(bookingId);
        final var bookingAndSequence = new BedAssignmentHistory.BedAssignmentHistoryPK(bookingId, maxSequence + 1);
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
    }

        @VerifyBookingAccess
            public List<BedAssignment> getBedAssignmentsHistory(final Long bookingId) {
                    final var bedAssignmentsHistory = repository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId);
                    return bedAssignmentsHistory.stream().map(assignment -> {
                                final var agencyInternalLocation = locationRepository.findOneByLocationId(assignment.getLivingUnitId());
                                return BedAssignment.builder()
                                                .livingUnitId(assignment.getLivingUnitId())
                                                .description(agencyInternalLocation.map(AgencyInternalLocation::getDescription).orElse(null))
                                                .assignmentDate(assignment.getAssignmentDate())
                                                .assignmentEndDate(assignment.getAssignmentEndDate())
                                                .assignmentDateTime(assignment.getAssignmentDateTime())
                                                .assignmentEndDateTime(assignment.getAssignmentEndDateTime())
                                                .assignmentReason(assignment.getAssignmentReason())
                                                .bookingId(assignment.getOffenderBooking().getBookingId())
                                                .agencyId(agencyInternalLocation.map(AgencyInternalLocation::getAgencyId).orElse(null))
                                                .build();
                        })
                                    .collect(Collectors.toList());
                }

    }
