package net.syscon.elite.service;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.ContactDetail;
import net.syscon.elite.api.model.OffenderRelationship;

public interface ContactService {
    ContactDetail getContacts(Long bookingId);

    Contact createRelationship(Long bookingId, OffenderRelationship relationshipDetail);

    Contact createRelationshipByOffenderNo(String offenderNo, OffenderRelationship relationshipDetail);
}
