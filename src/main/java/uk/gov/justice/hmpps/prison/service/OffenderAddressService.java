package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final OffenderAddressRepository offenderAddressRepository;

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<AddressDto> getAddressesByOffenderNo(@NotNull final String offenderNo) {
        final var offenderBooking = offenderBookingRepository.findByOffenderNomsIdAndActive(offenderNo, true)
            .orElseThrow(EntityNotFoundException.withMessage(String.format("No active offender bookings found for offender number %s\n", offenderNo)));
        final var addresses = offenderAddressRepository.findByOffenderId(offenderBooking.getOffender().getRootOffender().getId());
        return AddressTransformer.translate(addresses);
    }
}
