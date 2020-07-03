package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnitProfile;

import java.util.List;

public interface LivingUnitProfileRepository extends CrudRepository<LivingUnitProfile, Long> {
    List<LivingUnitProfile> findAllByLivingUnitIdAndAgencyLocationIdAndDescription(Long livingUnitId, String agencyLocationId, String description);
}
