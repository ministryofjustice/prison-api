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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {
    private static final String OFFICIAL_CONTACT_TYPE = "O";  //TODO: Allow API to be specified in future work

    private final ContactRepository repository;
    private final BookingService bookingService;
    private final ReferenceDomainService referenceDomainService;

    @Autowired
    public ContactServiceImpl(ContactRepository contactRepository,
                              BookingService bookingService,
                              ReferenceDomainService referenceDomainService) {
        this.repository = contactRepository;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
    }

    @Override
    @VerifyBookingAccess
    @Transactional(readOnly = true)
    public ContactDetail getContacts(Long bookingId) {
        final List<Contact> list = repository.findNextOfKin(bookingId);

        return ContactDetail.builder().nextOfKin(list).build();
    }

    @Override
    @VerifyBookingAccess
    public List<Contact> getRelationships(Long bookingId, String relationshipType) {
        return repository.getOffenderRelationships(bookingId, relationshipType);
    }

    @Override
    public List<Contact> getRelationshipsByOffenderNo(String offenderNo, String relationshipType) {
        Long bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);
        return getRelationships(bookingId, relationshipType);
    }

    @Override
    public Contact createRelationshipByOffenderNo(String offenderNo, OffenderRelationship relationshipDetail) {
        Long bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);
        return createRelationship(bookingId, relationshipDetail);
    }

    @Override
    @VerifyBookingAccess
    public Contact createRelationship(Long bookingId, OffenderRelationship relationshipDetail) {

        // Check relationship type exists - TODO: Move to validator
        referenceDomainService.getReferenceCodeByDomainAndCode("RELATIONSHIP", relationshipDetail.getRelationshipType(), false);

        Person person = createPersonAndRef(relationshipDetail, relationshipDetail.getPersonId());

        // now check the relationship exists already
        final List<Contact> offenderRelationships = getOffenderRelationships(bookingId, relationshipDetail);

        // should only be one of this type
        final Optional<Contact> existingRelationship = offenderRelationships.stream()
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

    private List<Contact> getOffenderRelationships(Long bookingId, OffenderRelationship relationshipDetail) {
        return repository.getOffenderRelationships(bookingId, relationshipDetail.getRelationshipType());
    }

    private Person createPersonAndRef(final OffenderRelationship relationshipDetail, final Long personId) {
        boolean foundRef = false;
        Optional<Person> person = Optional.empty();
        // check if person ref set
        if (StringUtils.isNotBlank(relationshipDetail.getExternalRef())) {
            person = repository.getPersonByRef(relationshipDetail.getExternalRef(), EXTERNAL_REF);
            foundRef = person.isPresent();
        } else if (personId != null) {
            person = repository.getPersonById(personId);
        }

        Long newPersonId;
        // for now if person is null, create it!
        if (person.isPresent()) {
            newPersonId = person.get().getPersonId();
            repository.updatePerson(newPersonId, relationshipDetail.getFirstName(), relationshipDetail.getLastName());
        } else {
            newPersonId = repository.createPerson(relationshipDetail.getFirstName(), relationshipDetail.getLastName());
        }

        // if the external ref was not found add as identifier
        if (StringUtils.isNotBlank(relationshipDetail.getExternalRef()) && !foundRef) {
            repository.createExternalReference(newPersonId, relationshipDetail.getExternalRef(), EXTERNAL_REF);
        }

        return repository.getPersonById(newPersonId).orElseThrow(EntityNotFoundException.withId(newPersonId));
    }

}
