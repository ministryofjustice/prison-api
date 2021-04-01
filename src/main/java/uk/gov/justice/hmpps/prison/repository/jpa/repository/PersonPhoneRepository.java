package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonPhone;

import java.util.List;

public interface PersonPhoneRepository extends CrudRepository<PersonPhone, Long> {

    @Query("Select pa from PersonPhone pa where pa.person.id = :personId")
    List<PersonPhone> findAllByPersonId(Long personId);

}
