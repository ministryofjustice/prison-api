package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyAddress;

import java.util.List;

@Repository
public interface AgencyAddressRepository extends CrudRepository<AgencyAddress, Long> {
    @Query("Select aa from AgencyAddress aa where aa.agency.id = :agencyId")
    List<AgencyAddress> findAllByAgencyId(String agencyId);
}