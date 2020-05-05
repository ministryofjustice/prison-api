package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {

}
