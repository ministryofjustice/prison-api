package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.api.model.Location;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgencyRepositoryJpa extends CrudRepository<Location, Long> {
    
    @Query(value = 
        "SELECT A.INTERNAL_LOCATION_ID LOCATION_ID," +
                "         A.AGY_LOC_ID AGENCY_ID," +
                "         A.INTERNAL_LOCATION_TYPE LOCATION_TYPE," +
                "         A.DESCRIPTION," +
                "         A.PARENT_INTERNAL_LOCATION_ID PARENT_LOCATION_ID," +
                "         A.NO_OF_OCCUPANT CURRENT_OCCUPANCY," +
                "         A.OPERATION_CAPACITY OPERATIONAL_CAPACITY," +
                "         A.USER_DESC USER_DESCRIPTION" +
                "  FROM AGENCY_INTERNAL_LOCATIONS A" +
                "  WHERE A.ACTIVE_FLAG = 'Y'" +
                "  AND A.AGY_LOC_ID = :agencyId" +
                "  AND A.INTERNAL_LOCATION_TYPE = :locationType",
        nativeQuery = true)
    List<Location> getAgencyLocationsByType(@Param("agencyId") final String agencyId, @Param("locationType") final String locationType);
    
}
