package net.syscon.elite.service;

import net.syscon.elite.api.model.ContactDetail;

public interface ContactService {
    ContactDetail getContacts(Long bookingId);
}
