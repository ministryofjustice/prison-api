package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.model.AddressDto;
import net.syscon.prison.api.model.Email;
import net.syscon.prison.api.model.PersonIdentifier;
import net.syscon.prison.api.model.Telephone;
import net.syscon.prison.api.resource.PersonResource;
import net.syscon.prison.service.PersonService;
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

    @Override
    public List<AddressDto> getPersonAddresses(final Long personId) {
        return service.getAddresses(personId);
    }

    @Override
    public List<Telephone> getPersonPhones(final Long personId) {
        return service.getPhones(personId);
    }

    @Override
    public List<Email> getPersonEmails(final Long personId) {
        return service.getEmails(personId);
    }
}
