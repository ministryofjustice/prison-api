package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;

import java.util.List;
import java.util.Set;

public interface OffenderChargeRepository extends CrudRepository<OffenderCharge, Long> {
    List<OffenderCharge> findByOffenderBooking_BookingId(long bookingId);

    @Query("Select oc from OffenderCharge oc where oc.offenderBooking.bookingId in :bookingIds and oc.chargeStatus = 'A' and oc.offenderCourtCase.caseStatus.code = 'A'")
    List<OffenderCharge> findActiveOffencesByBookingIds(Set<Long> bookingIds);
}
