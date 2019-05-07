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
    @VerifyBookingAccess
    public ContactDetail getContacts(final Long bookingId) {
        final var contacts = repository.getOffenderRelationships(bookingId, null);

        Comparator<Contact> sortCriteria = (c1, c2) -> Boolean.compare(
                c2.getEmergencyContact(), c1.getEmergencyContact());

        sortCriteria = sortCriteria.thenComparing(Contact::getLastName);

        final var list = contacts.stream()
                .filter(Contact::getNextOfKin)
                .sorted(sortCriteria)
                .collect(toList());
        return ContactDetail.builder().nextOfKin(list).build();
    }

    @Override
    @VerifyBookingAccess
    public List<Contact> getRelationships(final Long bookingId, final String relationshipType) {
        return repository.getOffenderRelationships(bookingId, relationshipType);
    }

    @Override
    public List<Contact> getRelationshipsByOffenderNo(final String offenderNo, final String relationshipType) {
        final var bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);
        return getRelationships(bookingId, relationshipType);
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

        // now check the relationship exists already
        final var offenderRelationships = getOffenderRelationships(bookingId, relationshipDetail);

        // should only be one of this type
        final var existingRelationship = offenderRelationships.stream()
                .filter(r -> r.getPersonId().equals(person.getPersonId()))
                .findFirst();

        if (existingRelationship.isPresent()) {
            return existingRelationship.get();
        }

        if (offenderRelationships.isEmpty()) {
            repository.createRelationship(person.getPersonId(), bookingId, relationshipDetail.getRelationshipType(), OFFICIAL_CONTACT_TYPE);
        } else {
            repository.updateRelationship(person.getPersonId(), offenderRelationships.get(0).getRelationshipId());
        }
        // reload
        return getOffenderRelationships(bookingId, relationshipDetail).stream()
                .filter(r -> r.getPersonId().equals(person.getPersonId()))
                .findFirst().orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    private List<Contact> getOffenderRelationships(final Long bookingId, final OffenderRelationship relationshipDetail) {
        return repository.getOffenderRelationships(bookingId, relationshipDetail.getRelationshipType());
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
