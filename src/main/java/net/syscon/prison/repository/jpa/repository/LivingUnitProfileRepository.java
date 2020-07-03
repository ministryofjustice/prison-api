package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.LivingUnitProfile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LivingUnitProfileRepository extends CrudRepository<LivingUnitProfile, Long> {
    List<LivingUnitProfile> findAllByLivingUnitIdAndAgencyLocationIdAndDescription(Long livingUnitId, String agencyLocationId, String description);
}
