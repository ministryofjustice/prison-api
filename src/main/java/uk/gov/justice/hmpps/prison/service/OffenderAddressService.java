package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressService {

    private final OffenderRepository offenderRepository;
    private final OffenderAddressRepository offenderAddressRepository;

    public List<AddressDto> getAddressesByOffenderNo(@NotNull final String offenderNo) {
        final var offender = offenderRepository.findOffenderWithLatestBookingByNomsId(offenderNo)
            .orElseThrow(EntityNotFoundException.withMessage(String.format("No offender found for offender number %s\n", offenderNo)));
        final var addresses = offenderAddressRepository.findByOffenderId(offender.getRootOffender().getId());
        return AddressTransformer.translate(addresses);
    }
}
