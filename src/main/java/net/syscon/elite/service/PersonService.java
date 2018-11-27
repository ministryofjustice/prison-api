package net.syscon.elite.service;

import net.syscon.elite.api.model.PersonIdentifier;

import java.util.List;

public interface PersonService {
    List<PersonIdentifier> getPersonIdentifiers(long personId);
}
