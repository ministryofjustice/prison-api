package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;

import java.util.Optional;

public interface OffenderCaseNoteRepository extends PagingAndSortingRepository<OffenderCaseNote, Long>, JpaSpecificationExecutor<OffenderCaseNote> {

    Optional<OffenderCaseNote> findByIdAndOffenderBooking_BookingId(final Long id, final Long bookingId);
}
