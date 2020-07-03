package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BedAssignmentHistoryServiceTest {

    private final BedAssignmentHistoriesRepository repository = mock(BedAssignmentHistoriesRepository.class);
    private final BedAssignmentHistoryService service = new BedAssignmentHistoryService(repository);

    @Test
    void add() {
        final var now = LocalDateTime.now();
        service.add(1L, 2L, "RSN", now);

        verify(repository).save(argThat(bedAssignment ->
                bedAssignment.getBedAssignmentHistoryPK().getOffenderBookingId() == 1L
                        && bedAssignment.getLivingUnitId() == 2L
                        && bedAssignment.getAssignmentReason().equals("RSN")
                        && bedAssignment.getAssignmentDate().isEqual(now.toLocalDate())
                        && bedAssignment.getAssignmentDateTime().isEqual(now)));
    }
}
