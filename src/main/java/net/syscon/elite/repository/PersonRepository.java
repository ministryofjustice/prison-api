package net.syscon.elite.repository;

import net.syscon.elite.api.model.PersonIdentifier;

import java.util.List;

public interface PersonRepository {
    List<PersonIdentifier> getPersonIdentifiers(long personId);
}
