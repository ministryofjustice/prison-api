package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.PersonResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.PersonService;

import javax.ws.rs.Path;

@RestResource
@Path("persons")
public class PersonResourceImpl implements PersonResource {
    private final PersonService service;

    public PersonResourceImpl(PersonService service) {
        this.service = service;
    }

    @Override
    public GetPersonIdentifiersResponse getPersonIdentifiers(Long personId) {
        return GetPersonIdentifiersResponse.respond200WithApplicationJson(service.getPersonIdentifiers(personId));
    }
}
