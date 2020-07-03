package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.OffenderBooking;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderBookingRepository extends CrudRepository<OffenderBooking, Long> {
    List<OffenderBooking> findByOffenderNomsIdAndActiveFlag(String nomsId, String activeFlag);
}
