package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.OffenderNonAssociation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderNonAssociationRepository extends CrudRepository<OffenderNonAssociation, OffenderNonAssociation.Pk> {
    List<OffenderNonAssociation> findAllByOffenderBooking_BookingId(final long bookingId);
}
