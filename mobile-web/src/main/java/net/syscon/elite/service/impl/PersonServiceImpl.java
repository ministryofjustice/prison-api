package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PersonIdentifier;
import net.syscon.elite.repository.PersonRepository;
import net.syscon.elite.service.PersonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Boiler-plate that follows the pattern. Doesn't add anything yet.
 */
@Service
@Transactional(readOnly = true)
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public List<PersonIdentifier> getPersonIdentifiers(long personId) {
        return personRepository.getPersonIdentifiers(personId);
    }
}
