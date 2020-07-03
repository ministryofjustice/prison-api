package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Address;

import java.util.List;

public interface AddressRepository extends CrudRepository<Address, Long> {
    List<Address> findAllByOwnerClassAndOwnerId(String ownerClass, Long ownerId);
}
