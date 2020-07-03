package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.Phone;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PhoneRepository extends CrudRepository<Phone, Long> {
    List<Phone> findAllByOwnerClassAndOwnerId(String ownerClass, Long ownerId);

}
