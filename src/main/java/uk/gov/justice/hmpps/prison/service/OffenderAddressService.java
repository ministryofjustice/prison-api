package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final OffenderAddressRepository offenderAddressRepository;

    public List<AddressDto> getAddressesByOffenderNo(@NotNull final String offenderNo) {
        final var offenderBooking = offenderBookingRepository.findByOffenderNomsId(offenderNo)
            .orElseThrow(EntityNotFoundException.withMessage(String.format("No active offender found for offender number %s\n", offenderNo)));
        final var addresses = offenderAddressRepository.findByOffenderId(offenderBooking.getOffender().getRootOffender().getId());
        return AddressTransformer.translate(addresses);
    }
}
