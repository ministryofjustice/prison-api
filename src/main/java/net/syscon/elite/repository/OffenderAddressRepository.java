package net.syscon.elite.repository;

import net.syscon.elite.api.model.OffenderAddress;

import java.util.List;

public interface OffenderAddressRepository {

    List<OffenderAddress> getAddresses(String offenderNumber);
}
