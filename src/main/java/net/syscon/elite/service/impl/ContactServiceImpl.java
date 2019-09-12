package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.ContactDetail;
import net.syscon.elite.api.model.OffenderRelationship;
import net.syscon.elite.api.model.Person;
import net.syscon.elite.repository.ContactRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.ContactService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ReferenceDomainService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class ContactServiceImpl implements ContactService {
    private static final String OFFICIAL_CONTACT_TYPE = "O";  //TODO: Allow API to be specified in future work

    private final ContactRepository repository;
    private final BookingService bookingService;
    private final ReferenceDomainService referenceDomainService;

    @Autowired
    public ContactServiceImpl(final ContactRepository contactRepository,
                              final BookingService bookingService,
                              final ReferenceDomainService referenceDomainService) {
        this.repository = contactRepository;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public ContactDetail getContacts(final Long bookingId) {
        final var contacts = repository.getOffenderRelationships(bookingId, null);

        Comparator<Contact> sortCriteria = (c1, c2) -> Boolean.compare(
                c2.isEmergencyContact(), c1.isEmergencyContact());

        sortCriteria = sortCriteria.thenComparing(Contact::getLastName);

        final var list = contacts.stream()
                .filter(Contact::isActiveFlag)
                .filter(Contact::isNextOfKin)
                .sorted(sortCriteria)
                .collect(toList());
        return ContactDetail.builder().nextOfKin(list).build();
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<Contact> getRelationships(final Long bookingId, final String relationshipType, final boolean activeOnly) {
        return repository.getOffenderRelationships(bookingId, relationshipType)
                .stream()
                .filter(r -> !activeOnly || r.isActiveFlag())
                .collect(Collectors.toList());
    }

    @Override
    public List<Contact> getRelationshipsByOffenderNo(final String offenderNo, final String relationshipType, final boolean activeOnly) {
        final var bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);
        return getRelationships(bookingId, relationshipType, true);
    }

    @Override
    @PreAuthorize("hasRole('CONTACT_CREATE')")
    @Transactional
    public Contact createRelationshipByOffenderNo(final String offenderNo, final OffenderRelationship relationshipDetail) {
        final var bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);
        return createRelationship(bookingId, relationshipDetail);
    }

    @Override
    @PreAuthorize("hasRole('CONTACT_CREATE')")
    @Transactional
    public Contact createRelationship(final Long bookingId, final OffenderRelationship relationshipDetail) {

        // Check relationship type exists - TODO: Move to validator
        referenceDomainService.getReferenceCodeByDomainAndCode("RELATIONSHIP", relationshipDetail.getRelationshipType(), false);

        final var person = createPersonAndRef(relationshipDetail, relationshipDetail.getPersonId());

        final var activeRel = new AtomicLong();

        // now check the relationship exists already and mark active and turn others inactive.
        getRelationships(bookingId, relationshipDetail.getRelationshipType(), false)
                .forEach(rel -> {
                    if (rel.getPersonId().equals(person.getPersonId()) && activeRel.get() == 0) {
                        // set the contact with same person to active.
                        if (!rel.isActiveFlag()) {
                            repository.updateRelationship(rel.getRelationshipId(), rel.getPersonId(), true);
                        }
                        activeRel.set(rel.getRelationshipId());
                    } else {
                        // set all the others to inactive
                        repository.updateRelationship(rel.getRelationshipId(), rel.getPersonId(), false);
                    }
                });

        // if we didn't find a relationship for that person, all others are now inactive, create a new record with this person/offender relationship
        if (activeRel.get() == 0) {
            activeRel.set(repository.createRelationship(person.getPersonId(), bookingId, relationshipDetail.getRelationshipType(), OFFICIAL_CONTACT_TYPE));
        }

        return repository.getOffenderRelationship(activeRel.get()).orElseThrow(EntityNotFoundException.withId(bookingId));
    }


    private Person createPersonAndRef(final OffenderRelationship relationshipDetail, final Long personId) {
        var foundRef = false;
        Optional<Person> person = Optional.empty();
        // check if person ref set
        if (StringUtils.isNotBlank(relationshipDetail.getExternalRef())) {
            person = repository.getPersonByRef(relationshipDetail.getExternalRef(), EXTERNAL_REL);
            foundRef = person.isPresent();
        } else if (personId != null) {
            person = repository.getPersonById(personId);
        }

        final Long newPersonId;
        // for now if person is null, create it!
        if (person.isPresent()) {
            newPersonId = person.get().getPersonId();
            repository.updatePerson(newPersonId, relationshipDetail.getFirstName(), relationshipDetail.getLastName());
        } else {
            newPersonId = repository.createPerson(relationshipDetail.getFirstName(), relationshipDetail.getLastName());
        }

        // if the external ref was not found add as identifier
        if (StringUtils.isNotBlank(relationshipDetail.getExternalRef()) && !foundRef) {
            repository.createExternalReference(newPersonId, relationshipDetail.getExternalRef(), EXTERNAL_REL);
        }

        return repository.getPersonById(newPersonId).orElseThrow(EntityNotFoundException.withId(newPersonId));
    }

}
