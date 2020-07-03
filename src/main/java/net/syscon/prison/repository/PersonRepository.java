package net.syscon.prison.repository;

import net.syscon.prison.api.model.PersonIdentifier;

import java.util.List;

public interface PersonRepository {
    List<PersonIdentifier> getPersonIdentifiers(long personId);
}
