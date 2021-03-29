package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.AddressUsageDto;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressService {

    private final OffenderBookingRepository offenderBookingRepository;

    @VerifyOffenderAccess
    public List<AddressDto> getAddressesByOffenderNo(@NotNull final String offenderNo) {
        final var optionalOffenderBooking = offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(offenderNo, "Y");
        final var offenderBooking = optionalOffenderBooking.orElseThrow(EntityNotFoundException.withMessage(String.format("No active offender bookings found for offender number %s\n", offenderNo)));

        return offenderBooking.getOffender().getRootOffender().getAddresses().stream().map(address -> {
            final var country = address.getCountry() != null ? address.getCountry().getDescription() : null;
            final var county = address.getCounty() != null ? address.getCounty().getDescription() : null;
            final var town = address.getCity() != null ? address.getCity().getDescription() : null;
            final var addressType = address.getAddressType() != null ? address.getAddressType().getDescription() : null;

            return AddressDto.builder()
                    .addressId(address.getAddressId())
                    .addressType(addressType)
                    .flat(address.getFlat())
                    .comment(address.getCommentText())
                    .country(country)
                    .county(county)
                    .town(town)
                    .postalCode(address.getPostalCode())
                    .noFixedAddress("Y".equalsIgnoreCase(address.getNoFixedAddressFlag()))
                    .premise(address.getPremise())
                    .primary("Y".equalsIgnoreCase(address.getPrimaryFlag()))
                    .startDate(address.getStartDate())
                    .endDate(address.getEndDate())
                    .street(address.getStreet())
                    .addressUsages(address.getAddressUsages().stream()
                            .map(addressUsage ->
                                    AddressUsageDto.builder()
                                            .addressId(address.getAddressId())
                                            .activeFlag("Y".equalsIgnoreCase(addressUsage.getActiveFlag()))
                                            .addressUsage(addressUsage.getAddressUsage())
                                            .addressUsageDescription(addressUsage.getAddressUsageType() == null ? null : addressUsage.getAddressUsageType().getDescription())
                                            .build()).collect(Collectors.toList()))
                    .phones(address.getPhones().stream().map(phone ->
                            Telephone.builder()
                                    .ext(phone.getExtNo())
                                    .type(phone.getPhoneType())
                                    .number(phone.getPhoneNo())
                                    .build())
                            .collect(toList()))
                    .build();

        }).collect(toList());
    }
}
