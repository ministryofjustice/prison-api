package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.InternetAddress;

import java.util.List;

public interface InternetAddressRepository extends CrudRepository<InternetAddress, Long> {
    List<InternetAddress> findByOwnerClassAndOwnerIdAndInternetAddressClass(String ownerClass, Long ownerId, String addressClass);

}
