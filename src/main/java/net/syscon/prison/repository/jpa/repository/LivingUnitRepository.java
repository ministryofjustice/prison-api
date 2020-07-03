package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.LivingUnit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LivingUnitRepository extends CrudRepository<LivingUnit, Long> {
    List<LivingUnit> findAllByAgencyLocationId(String agencyId);
}
