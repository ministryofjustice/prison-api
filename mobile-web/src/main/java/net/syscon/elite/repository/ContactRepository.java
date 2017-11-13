package net.syscon.elite.repository;

import net.syscon.elite.api.model.NextOfKin;

import java.util.List;

public interface ContactRepository {

    List<NextOfKin> findNextOfKin(long bookingId);
}
