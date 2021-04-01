package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final AddressTranslator addressTranslator;

    @VerifyOffenderAccess
    public List<AddressDto> getAddressesByOffenderNo(@NotNull final String offenderNo) {
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(offenderNo, "Y");
        final var offenderBooking = optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage(String.format("No active offender bookings found for offender number %s\n", offenderNo)));

        return addressTranslator.translate(offenderBooking.getOffender().getRootOffender().getAddresses());
    }
}
