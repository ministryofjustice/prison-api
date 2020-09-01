package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnit;

import java.util.List;
import java.util.Optional;

public interface LivingUnitRepository extends CrudRepository<LivingUnit, Long> {
    List<LivingUnit> findAllByAgencyLocationId(String agencyId);
    Optional<LivingUnit> findOneByLivingUnitId(Long livingUnitId);
}
