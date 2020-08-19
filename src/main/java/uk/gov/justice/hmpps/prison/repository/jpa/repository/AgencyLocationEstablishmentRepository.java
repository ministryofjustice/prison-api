package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationEstablishment;

import java.util.List;

public interface AgencyLocationEstablishmentRepository extends CrudRepository<AgencyLocationEstablishment, AgencyLocationEstablishment.Pk> {
    List<AgencyLocationEstablishment> findAgencyLocationEstablishmentByAgencyLocId(final String id);
}
