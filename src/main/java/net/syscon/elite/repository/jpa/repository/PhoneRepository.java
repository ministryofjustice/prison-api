package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Phone;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PhoneRepository extends CrudRepository<Phone, Long> {
    List<Phone> findAllByOwnerClassAndOwnerId(String ownerClass, Long ownerId);

}
