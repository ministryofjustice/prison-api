package net.syscon.elite.service;

import net.syscon.elite.api.model.PersonIdentifier;
import net.syscon.elite.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PersonService {
    private final PersonRepository personRepository;

    public PersonService(final PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<PersonIdentifier> getPersonIdentifiers(final long personId) {
        return personRepository.getPersonIdentifiers(personId);
    }
}
