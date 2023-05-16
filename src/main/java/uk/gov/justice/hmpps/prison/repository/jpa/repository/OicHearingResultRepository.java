package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.FindingCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.PK;

import java.util.List;

public interface OicHearingResultRepository extends CrudRepository<OicHearingResult, PK> {
    List<OicHearingResult> findByAgencyIncidentIdAndFindingCode(Long agencyIncidentId, FindingCode findingCode);

    List<OicHearingResult> findByOicHearingId(Long oicHearingId);
}
