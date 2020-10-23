package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation;

import java.util.List;

public interface OffenderDamageObligationRepository extends CrudRepository<OffenderDamageObligation, Long> {
    List<OffenderDamageObligation> findOffenderDamageObligationByOffender_NomsId(String offenderNo);
    List<OffenderDamageObligation> findOffenderDamageObligationByOffender_NomsIdAndStatus(String offenderNo, String status);
}
