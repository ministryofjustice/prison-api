package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail;

import java.util.List;

public interface OffenderNonAssociationDetailRepository extends CrudRepository<OffenderNonAssociationDetail, OffenderNonAssociationDetail.Pk> {
    List<OffenderNonAssociationDetail> findAllByOffenderBooking_BookingId(final long bookingId);
}
