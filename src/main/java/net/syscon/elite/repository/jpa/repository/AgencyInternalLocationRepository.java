package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AgencyInternalLocationRepository extends CrudRepository<AgencyInternalLocation, Long> {

    List<AgencyInternalLocation> findAgencyInternalLocationsByAgencyIdAndLocationTypeAndActiveFlag(final String agencyId, final String locationType, final ActiveFlag activeFlag);
}
