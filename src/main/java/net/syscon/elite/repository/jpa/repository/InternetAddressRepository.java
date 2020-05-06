package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.InternetAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InternetAddressRepository extends CrudRepository<InternetAddress, Long> {
    List<InternetAddress> findByOwnerClassAndOwnerIdAndInternetAddressClass(String ownerClass, Long ownerId, String addressClass);

}
