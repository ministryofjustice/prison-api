package uk.gov.justice.hmpps.prison.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.api.model.ContactDetail;
import uk.gov.justice.hmpps.prison.repository.ContactRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class ContactService {
    private final ContactRepository repository;

    @Autowired
    public ContactService(final ContactRepository contactRepository) {
        this.repository = contactRepository;
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

}
