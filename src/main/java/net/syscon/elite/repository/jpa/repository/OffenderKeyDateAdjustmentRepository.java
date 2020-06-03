package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.OffenderKeyDateAdjustment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderKeyDateAdjustmentRepository extends CrudRepository<OffenderKeyDateAdjustment, Long> {
    List<OffenderKeyDateAdjustment> findAllByOffenderBookId(Long bookingId);
}
