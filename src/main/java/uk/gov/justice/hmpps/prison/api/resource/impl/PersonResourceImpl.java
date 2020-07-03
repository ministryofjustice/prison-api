package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.api.resource.PersonResource;
import uk.gov.justice.hmpps.prison.service.PersonService;

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
