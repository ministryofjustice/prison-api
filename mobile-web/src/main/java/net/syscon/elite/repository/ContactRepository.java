package net.syscon.elite.repository;

import net.syscon.elite.api.model.Contact;

import java.util.List;

public interface ContactRepository {

    List<Contact> findNextOfKin(long bookingId);
}
