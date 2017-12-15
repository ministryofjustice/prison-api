package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.ContactDetail;
import net.syscon.elite.api.model.NextOfKin;
import net.syscon.elite.repository.ContactRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContactServiceImpl implements ContactService {

    private final ContactRepository repository;

    @Autowired
    public ContactServiceImpl(ContactRepository contactRepository, BookingService bookingService) {
        this.repository = contactRepository;
    }

    @Override
    @VerifyBookingAccess
    public ContactDetail getContacts(Long bookingId) {
        final List<NextOfKin> list = repository.findNextOfKin(bookingId);

        return ContactDetail.builder().nextOfKin(list).build();
    }
}
