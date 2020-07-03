package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.OffenderSentenceAdjustment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OffenderSentenceAdjustmentRepository extends CrudRepository<OffenderSentenceAdjustment, Long> {
    List<OffenderSentenceAdjustment> findAllByOffenderBookId(Long bookingId);
}
