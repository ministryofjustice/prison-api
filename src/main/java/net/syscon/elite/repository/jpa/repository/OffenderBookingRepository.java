package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.OffenderBooking;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderBookingRepository extends CrudRepository<OffenderBooking, Long> {
    List<OffenderBooking> findByOffenderNomsIdAndActiveFlag(String nomsId, String activeFlag);
}
