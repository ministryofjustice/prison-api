package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.AgencyLocation;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface AgencyLocationRepository extends CrudRepository<AgencyLocation, String>, JpaSpecificationExecutor<AgencyLocation> {
}
