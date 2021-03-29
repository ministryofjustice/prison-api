package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAddress;

@Repository
public interface OffenderAddressRepository extends CrudRepository<OffenderAddress, Long> {

}