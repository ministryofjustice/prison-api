package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonAddress;

import java.util.List;

@Repository
public interface PersonAddressRepository extends CrudRepository<PersonAddress, Long> {

    @Query("Select pa from PersonAddress pa where pa.person.id = :personId")
    List<PersonAddress> findAllByPersonId(Long personId);
}