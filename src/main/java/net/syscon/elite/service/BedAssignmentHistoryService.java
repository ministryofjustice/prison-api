package net.syscon.elite.service;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.jpa.model.BedAssignmentHistory;
import net.syscon.elite.repository.jpa.repository.BedAssignmentHistoriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
public class BedAssignmentHistoryService {

    private final BedAssignmentHistoriesRepository repository;

    public BedAssignmentHistoryService(BedAssignmentHistoriesRepository repository) {
        this.repository = repository;
    }

    public void add(Long bookingId, Long livingUnitId, String reasonCode, LocalDateTime time) {
        final var maxSequence = repository.getMaxSeqForBookingId(bookingId);
        final var bookingAndSequence = new BedAssignmentHistory.BookingAndSequence(bookingId, maxSequence + 1);
        final var bedAssignmentHistory =
                BedAssignmentHistory.builder()
                        .bookingAndSequence(bookingAndSequence)
                        .livingUnitId(livingUnitId)
                        .assignmentDate(time.toLocalDate())
                        .assignmentDateTime(time)
                        .assignmentReason(reasonCode)
                        .build();
        repository.save(bedAssignmentHistory);
        log.info("Added bed assignment history for offender booking id {} to living unit id {}", bookingId, livingUnitId);
    }
}
