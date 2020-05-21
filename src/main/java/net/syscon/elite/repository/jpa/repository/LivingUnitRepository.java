package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.LivingUnit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LivingUnitRepository extends CrudRepository<LivingUnit, Long> {
    List<LivingUnit> findAllByAgencyId(String agencyId);
}
