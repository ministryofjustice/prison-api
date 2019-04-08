package net.syscon.elite.service.impl;

import lombok.RequiredArgsConstructor;
import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.repository.OffenderAddressRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.OffenderAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressServiceImpl implements OffenderAddressService{

    private final OffenderAddressRepository repository;
    private final BookingService bookingService;

    @Override
    public List<OffenderAddress> getAddressesByOffenderNo(@NotNull String offenderNo) {
        bookingService.verifyCanViewLatestBooking(offenderNo);
        return repository.getAddresses(offenderNo);
    }
}
