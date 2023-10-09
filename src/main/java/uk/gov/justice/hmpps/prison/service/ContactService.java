package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.api.model.ContactDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderRelationship;
import uk.gov.justice.hmpps.prison.api.model.Person;
import uk.gov.justice.hmpps.prison.repository.ContactRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class ContactService {
    public static final String EXTERNAL_REL = "EXTERNAL_REL";

    private static final String OFFICIAL_CONTACT_TYPE = "O";  //TODO: Allow API to be specified in future work

    private final ContactRepository repository;
    private final BookingService bookingService;
    private final ReferenceDomainService referenceDomainService;

    @Autowired
    public ContactService(final ContactRepository contactRepository,
                          final BookingService bookingService,
                          final ReferenceDomainService referenceDomainService) {
        this.repository = contactRepository;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public ContactDetail getContacts(final Long bookingId) {
        final var contacts = repository.getOffenderRelationships(bookingId, null);

        Comparator<Contact> sortCriteria = (c1, c2) -> Boolean.compare(
                c2.isEmergencyContact(), c1.isEmergencyContact());

        sortCriteria = sortCriteria.thenComparing(Contact::getLastName);

        final Map<Boolean, List<Contact>> activeContactsMap = contacts.stream().filter(Contact::isActiveFlag).collect(Collectors.partitioningBy(Contact::isNextOfKin));
        return ContactDetail.builder()
                .nextOfKin(activeContactsMap.get(true).stream()
                        .sorted(sortCriteria)
                        .collect(toList()))
                .otherContacts(activeContactsMap.get(false).stream()
                        .sorted(sortCriteria)
                        .collect(toList())).build();
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<Contact> getRelationships(final Long bookingId, final String relationshipType, final boolean activeOnly) {
        return repository.getOffenderRelationships(bookingId, relationshipType)
                .stream()
                .filter(r -> !activeOnly || r.isActiveFlag())
                .toList();
    }

    public List<Contact> getRelationshipsByOffenderNo(final String offenderNo, final String relationshipType) {
        final var identifiers = bookingService.getOffenderIdentifiers(offenderNo, "SYSTEM_USER").getBookingAndSeq().orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNo));
        return getRelationships(identifiers.getBookingId(), relationshipType, true);
    }
    
    @PreAuthorize("hasRole('CONTACT_CREATE')")
    @Transactional
    public Contact createRelationshipByOffenderNo(final String offenderNo, final OffenderRelationship relationshipDetail) {
        final var identifiers = bookingService.getOffenderIdentifiers(offenderNo, "SYSTEM_USER").getBookingAndSeq().orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNo));
        return createRelationship(identifiers.getBookingId(), relationshipDetail);
    }

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
