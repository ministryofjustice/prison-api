package net.syscon.prison.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.core.HasWriteScope;
import net.syscon.prison.repository.jpa.model.BedAssignmentHistory;
import net.syscon.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;
import net.syscon.prison.security.VerifyBookingAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
public class BedAssignmentHistoryService {

    private final BedAssignmentHistoriesRepository repository;

    public BedAssignmentHistoryService(final BedAssignmentHistoriesRepository repository) {
        this.repository = repository;
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
}
