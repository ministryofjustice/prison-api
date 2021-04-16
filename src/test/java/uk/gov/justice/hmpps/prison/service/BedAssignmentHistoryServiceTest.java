package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BedAssignmentHistoryServiceTest {

    private final BedAssignmentHistoriesRepository repository = mock(BedAssignmentHistoriesRepository.class);
    private final AgencyInternalLocationRepository locationRepository = mock(AgencyInternalLocationRepository.class);
    private final BedAssignmentHistoryService service = new BedAssignmentHistoryService(repository, locationRepository);

    @Test
    void add() {
        final var now = LocalDateTime.now();
        final var pk = service.add(1L, 2L, "RSN", now);

        verify(repository).save(argThat(bedAssignment ->
                bedAssignment.getBedAssignmentHistoryPK().getOffenderBookingId() == 1L
                        && bedAssignment.getLivingUnitId() == 2L
                        && bedAssignment.getAssignmentReason().equals("RSN")
                        && bedAssignment.getAssignmentDate().isEqual(now.toLocalDate())
                        && bedAssignment.getAssignmentDateTime().isEqual(now)));

        assertThat(pk).isEqualTo(new BedAssignmentHistory.BedAssignmentHistoryPK(1L, 1));
    }

    @Test
    void getBedAssignmentsHistory() {
        var assignments = List.of(
                BedAssignmentHistory.builder()
                        .bedAssignmentHistoryPK(new BedAssignmentHistory.BedAssignmentHistoryPK(1L, 2))
                        .assignmentDate(LocalDate.of(2015, 5, 1))
                        .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
                        .assignmentEndDate(LocalDate.of(2016, 5, 1))
                        .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                        .assignmentReason("Needs moving")
                        .livingUnitId(1L)
                        .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
                        .build(),
                BedAssignmentHistory.builder()
                        .bedAssignmentHistoryPK(new BedAssignmentHistory.BedAssignmentHistoryPK(1L, 3))
                        .assignmentDate(LocalDate.of(2016, 5, 1))
                        .assignmentDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                        .assignmentEndDate(LocalDate.of(2017, 5, 1))
                        .assignmentEndDateTime(LocalDateTime.of(2017, 5, 1, 10, 10, 10))
                        .assignmentReason("Needs moving again")
                        .livingUnitId(2L)
                        .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
                        .build()
        );
        var page = new PageImpl<>(assignments);
        when(repository.findAllByBedAssignmentHistoryPKOffenderBookingId(1L, PageRequest.of(0, 20))).thenReturn(page);
        when(locationRepository.findOneByLocationId(anyLong())).thenReturn(Optional.of(AgencyInternalLocation.builder().description("MDI-1-2").agencyId("MDI").build()));
        final var response = service.getBedAssignmentsHistory(1L, PageRequest.of(0, 20));
        assertThat(response).containsOnly(
            BedAssignment.builder()
                .bookingId(1L)
                .livingUnitId(1L)
                .assignmentDate(LocalDate.of(2015, 5, 1))
                .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
                .assignmentEndDate(LocalDate.of(2016, 5, 1))
                .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                .assignmentReason("Needs moving")
                .description("MDI-1-2")
                .agencyId("MDI")
                .bedAssignmentHistorySequence(2)
                .build(),
            BedAssignment.builder()
                .bookingId(1L)
                .livingUnitId(2L)
                .assignmentDate(LocalDate.of(2016, 5, 1))
                .assignmentDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                .assignmentEndDate(LocalDate.of(2017, 5, 1))
                .assignmentEndDateTime(LocalDateTime.of(2017, 5, 1, 10, 10, 10))
                .assignmentReason("Needs moving again")
                .description("MDI-1-2")
                .agencyId("MDI")
                .bedAssignmentHistorySequence(3)
                .build()
        );

    }

    @Test
    void getBedAssignmentHistory_forLocationIdAndDateRange() {
        final var livingUnitId = 1L;
        final var bedHistoryAssignment = aBedAssignment(1L, livingUnitId);

        when(locationRepository.existsById(livingUnitId)).thenReturn(true);
        when(repository.findByLivingUnitIdAndDateTimeRange(anyLong(), any(), any()))
                .thenReturn(List.of(bedHistoryAssignment));

        when(locationRepository.findOneByLocationId(livingUnitId))
                .thenReturn(Optional.of(AgencyInternalLocation.builder()
                        .description("MDI-1-2")
                        .agencyId("MDI")
                        .build()));

        final var cellHistory = service.getBedAssignmentsHistory(livingUnitId, LocalDateTime.now(), LocalDateTime.now());

        assertThat(cellHistory).containsOnly(
                BedAssignment.builder()
                        .bookingId(1L)
                        .livingUnitId(livingUnitId)
                        .assignmentDate(LocalDate.of(2015, 5, 1))
                        .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
                        .assignmentEndDate(LocalDate.of(2016, 5, 1))
                        .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                        .assignmentReason("Needs moving")
                        .description("MDI-1-2")
                        .agencyId("MDI")
                        .bedAssignmentHistorySequence(2)
                        .build()
        );
    }

    @Test
    void getBedAssignmentHistory_forDateRange() {
        final var livingUnitId = 1L;
        final var bookingId = 1L;
        final var assignmentDate = LocalDate.now();

        when(locationRepository.findOneByLocationId(livingUnitId))
            .thenReturn(Optional.of(AgencyInternalLocation.builder()
                .description("MDI-1-2")
                .agencyId("MDI")
                .build()));

        when(repository.findBedAssignmentHistoriesByAssignmentDate(any()))
            .thenReturn(List.of(aBedAssignment(bookingId, livingUnitId)));

        final var cellHistory = service.getBedAssignmentsHistoryByDate(assignmentDate);

        verify(repository).findBedAssignmentHistoriesByAssignmentDate(assignmentDate);

        assertThat(cellHistory).containsOnly(
            BedAssignment.builder()
                .bookingId(bookingId)
                .livingUnitId(livingUnitId)
                .assignmentDate(LocalDate.of(2015, 5, 1))
                .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
                .assignmentEndDate(LocalDate.of(2016, 5, 1))
                .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                .assignmentReason("Needs moving")
                .description("MDI-1-2")
                .agencyId("MDI")
                .bedAssignmentHistorySequence(2)
                .build()
        );
    }

    @Test
    void getBedAssignmentHistory_cellNotFound() {
        when(locationRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> service.getBedAssignmentsHistory(1L, LocalDateTime.now(), LocalDateTime.now()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cell 1 not found");
    }

    @Test
    void getBedAssignmentHistory_checkDateOrder() {
        assertThatThrownBy(() -> service.getBedAssignmentsHistory(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The fromDate should be less then or equal to the toDate");
    }

    private BedAssignmentHistory aBedAssignment(final long bookingId, final long livingUnitId) {
        return BedAssignmentHistory.builder()
                .bedAssignmentHistoryPK(new BedAssignmentHistory.BedAssignmentHistoryPK(bookingId, 2))
                .assignmentDate(LocalDate.of(2015, 5, 1))
                .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
                .assignmentEndDate(LocalDate.of(2016, 5, 1))
                .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                .assignmentReason("Needs moving")
                .livingUnitId(livingUnitId)
                .offenderBooking(OffenderBooking.builder().bookingId(bookingId).build())
                .build();
    }

}
