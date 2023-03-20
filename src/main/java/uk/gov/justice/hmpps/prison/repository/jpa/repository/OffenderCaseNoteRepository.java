package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OffenderCaseNoteRepository extends
    PagingAndSortingRepository<OffenderCaseNote, Long>,
    CrudRepository<OffenderCaseNote, Long>,
    JpaSpecificationExecutor<OffenderCaseNote> {

    Optional<OffenderCaseNote> findByIdAndOffenderBooking_BookingId(final Long id, final Long bookingId);

    List<OffenderCaseNote> findByOffenderBooking_BookingIdInAndTypeInAndOccurrenceDateTimeGreaterThanEqual(
        List<Long> bookingIds, List<String> types, LocalDateTime cutoffTime
    );

}
