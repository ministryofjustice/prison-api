package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.OffenderSentenceAdjustment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderSentenceAdjustmentRepository extends CrudRepository<OffenderSentenceAdjustment, Long> {
    List<OffenderSentenceAdjustment> findAllByOffenderBookId(Long bookingId);
}
