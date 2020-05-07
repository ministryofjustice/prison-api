package net.syscon.elite.repository;

import net.syscon.elite.api.model.AddressDto;

import java.util.List;

public interface OffenderAddressRepository {

    List<AddressDto> getAddresses(String offenderNumber);
}
