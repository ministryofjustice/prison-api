package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.OffenderCase;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderCaseRepository extends CrudRepository<OffenderCase, Long> {
    List<OffenderCase> findByBookingId(final Long bookingId);
}
