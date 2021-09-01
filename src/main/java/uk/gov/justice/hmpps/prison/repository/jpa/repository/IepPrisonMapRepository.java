package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.IepPrisonMap;

import java.util.List;

public interface IepPrisonMapRepository extends CrudRepository<IepPrisonMap, IepPrisonMap.PK> {
    List<IepPrisonMap> findByAgencyLocation_IdAndDefaultFlag(String agencyLocationId, String defaultFlag );
}
