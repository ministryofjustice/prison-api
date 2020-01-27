package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PersonIdentifier;
import net.syscon.elite.api.resource.PersonResource;
import net.syscon.elite.service.PersonService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.base.path}/persons")
public class PersonResourceImpl implements PersonResource {
    private final PersonService service;

    public PersonResourceImpl(final PersonService service) {
        this.service = service;
    }

    @Override
    public List<PersonIdentifier> getPersonIdentifiers(final Long personId) {
        return service.getPersonIdentifiers(personId);
    }
}
