package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.ContactDetail;
import net.syscon.elite.api.model.OffenderRelationship;
import net.syscon.elite.repository.ContactRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {
    private final ContactRepository repository;

    @Autowired
    public ContactServiceImpl(ContactRepository contactRepository) {
        this.repository = contactRepository;
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
    public Contact createRelationship(Long bookingId, OffenderRelationship relationshipDetail) {
        return null;
    }

    @Override
    public Contact createRelationshipByOffenderNo(String offenderNo, OffenderRelationship relationshipDetail) {
        return null;
    }
}
