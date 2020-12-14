package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.IepLevel;

import java.util.List;

public interface IepLevelRepository extends CrudRepository<IepLevel, IepLevel.PK> {
    List<IepLevel> findByAgencyLocationIdAndDefaultFlag(String agencyLoctionId, String defaultFlag );
}
