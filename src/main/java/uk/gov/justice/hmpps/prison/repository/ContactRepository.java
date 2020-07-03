package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.api.model.Person;

import java.util.List;
import java.util.Optional;

public interface ContactRepository {

    Long createPerson(String firstName, String lastName);

    void updatePerson(Long personId, String firstName, String lastName);

    Optional<Person> getPersonById(Long personId);

    Optional<Person> getPersonByRef(String externalRef, String identifierType);

    void createExternalReference(Long personId, String externalRef, String identifierType);

    List<Contact> getOffenderRelationships(Long bookingId, String relationshipType);

    Optional<Contact> getOffenderRelationship(final Long relationshipId);

    Long createRelationship(Long personId, Long bookingId, String relationshipType, String contactType);

    void updateRelationship(Long id, Long personId, boolean active);
}
