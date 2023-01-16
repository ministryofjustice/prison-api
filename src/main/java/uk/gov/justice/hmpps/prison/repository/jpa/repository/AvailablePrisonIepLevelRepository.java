package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AvailablePrisonIepLevel;

import java.util.List;

public interface AvailablePrisonIepLevelRepository extends CrudRepository<AvailablePrisonIepLevel, AvailablePrisonIepLevel.PK> {
    List<AvailablePrisonIepLevel> findByAgencyLocation_IdAndActive(String agencyLocationId, boolean active );
}
