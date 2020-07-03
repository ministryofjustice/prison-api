package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;

import java.util.List;

public interface PersonRepository {
    List<PersonIdentifier> getPersonIdentifiers(long personId);
}
