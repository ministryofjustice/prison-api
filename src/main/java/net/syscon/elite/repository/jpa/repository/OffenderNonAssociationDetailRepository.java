package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.OffenderNonAssociationDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderNonAssociationDetailRepository extends CrudRepository<OffenderNonAssociationDetail, OffenderNonAssociationDetail.Pk> {
    List<OffenderNonAssociationDetail> findAllByOffenderBooking_BookingId(final long bookingId);
}
