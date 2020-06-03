package net.syscon.elite.service;

import lombok.RequiredArgsConstructor;
import net.syscon.elite.api.model.AddressDto;
import net.syscon.elite.api.model.AddressUsageDto;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.jpa.repository.AddressRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.repository.jpa.repository.PhoneRepository;
import net.syscon.elite.security.VerifyOffenderAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final AddressRepository addressRepository;
    private final PhoneRepository phoneRepository;

    @VerifyOffenderAccess
    public List<AddressDto> getAddressesByOffenderNo(@NotNull String offenderNo) {
        final var offenderBookings = offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(offenderNo, "Y");
        if(offenderBookings.size() > 1) throw new IllegalStateException(String.format("More than one active booking was returned for offender number %s\n", offenderNo));

        final var offenderBooking = offenderBookings.stream().findFirst().orElseThrow(EntityNotFoundException.withMessage(String.format("No active offender bookings found for offender number %s\n",offenderNo)));
        final var offenderRootId = offenderBooking.getOffender().getRootOffenderId();

        return addressRepository.findAllByOwnerClassAndOwnerId("OFF", offenderRootId).stream().map(address -> {
            final var country = address.getCountry() != null ? address.getCountry().getDescription() : null;
            final var county = address.getCounty() != null ? address.getCounty().getDescription() : null;
            final var town = address.getCity() != null ? address.getCity().getDescription() : null;

            return AddressDto.builder()
                    .addressId(address.getAddressId())
                    .addressType(address.getAddressType())
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
                    .addressUsages(address.getAddressUsages() == null ? null : address.getAddressUsages().stream()
                            .map(addressUsage ->
                                    AddressUsageDto.builder()
                                            .addressId(address.getAddressId())
                                            .activeFlag("Y".equalsIgnoreCase(addressUsage.getActiveFlag()))
                                            .addressUsage(addressUsage.getAddressUsage())
                                            .build()).collect(Collectors.toList()))
                    .phones(phoneRepository.findAllByOwnerClassAndOwnerId("ADDR", address.getAddressId()).stream().map(phone ->
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
