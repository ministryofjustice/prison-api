package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;

import java.util.List;

public interface OffenderCourtCaseRepository extends CrudRepository<OffenderCourtCase, Long> {
    List<OffenderCourtCase> findAllByOffenderBooking_BookingId(Long offenderBookingId);
}
