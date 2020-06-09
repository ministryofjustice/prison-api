package net.syscon.elite.service;


import net.syscon.elite.api.model.AddressDto;
import net.syscon.elite.api.model.AddressUsageDto;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.repository.jpa.model.Address;
import net.syscon.elite.repository.jpa.model.AddressType;
import net.syscon.elite.repository.jpa.model.AddressUsageType;
import net.syscon.elite.repository.jpa.model.AddressUsage;
import net.syscon.elite.repository.jpa.model.City;
import net.syscon.elite.repository.jpa.model.Country;
import net.syscon.elite.repository.jpa.model.County;
import net.syscon.elite.repository.jpa.model.Offender;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.Phone;
import net.syscon.elite.repository.jpa.repository.AddressRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.repository.jpa.repository.PhoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderAddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    @Mock
    private PhoneRepository phoneRepository;

    private OffenderAddressService offenderAddressService;

    @BeforeEach
    public void setUp() {
        offenderAddressService = new OffenderAddressService(offenderBookingRepository, addressRepository, phoneRepository);
    }

    @Test
    public void canRetrieveAddresses() {

        final var offenderNo = "off-1";

        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(any(), any())).thenReturn(List.of(OffenderBooking.builder().offender(Offender.builder().rootOffenderId(1L).build()).build()));
        when(addressRepository.findAllByOwnerClassAndOwnerId(any(), anyLong())).thenReturn(List.of(
                Address.builder()
                        .addressId(-15L)
                        .addressType(new AddressType("HOME", "Home Address"))
                        .ownerClass("PER")
                        .ownerId(-8L)
                        .noFixedAddressFlag("N")
                        .commentText(null)
                        .primaryFlag("Y")
                        .mailFlag("N")
                        .flat("Flat 1")
                        .premise("Brook Hamlets")
                        .street("Mayfield Drive")
                        .locality("Nether Edge")
                        .postalCode("B5")
                        .country(new Country("ENG", "England"))
                        .county(new County("S.YORKSHIRE", "South Yorkshire"))
                        .city(new City("25343", "Sheffield"))
                        .startDate(LocalDate.of(2016, 8, 2))
                        .endDate(null)
                        .addressUsages(List.of(
                                AddressUsage.builder().activeFlag("Y").addressUsage("HDC").addressUsageType(new AddressUsageType("HDC", "HDC address")).build(),
                                AddressUsage.builder().activeFlag("Y").addressUsage("HDC").build()
                        ))
                        .build(),
                Address.builder()
                        .addressId(-16L)
                        .addressType(new AddressType("BUS", "Business Address"))
                        .ownerClass("PER")
                        .ownerId(-8L)
                        .noFixedAddressFlag("Y")
                        .commentText(null)
                        .primaryFlag("N")
                        .mailFlag("N")
                        .flat(null)
                        .premise(null)
                        .street(null)
                        .locality(null)
                        .postalCode(null)
                        .country(new Country("ENG", "England"))
                        .county(null)
                        .city(null)
                        .startDate(LocalDate.of(2016, 8, 2))
                        .endDate(null)
                        .build()
        ));

        when(phoneRepository.findAllByOwnerClassAndOwnerId("ADDR", -15L)).thenReturn(
                List.of(
                        Phone.builder()
                                .phoneId(-7L)
                                .ownerId(-15L)
                                .ownerClass("ADDR")
                                .phoneNo("0114 2345345")
                                .phoneType("HOME")
                                .extNo("345")
                                .build(),
                        Phone.builder()
                                .phoneId(-8L)
                                .ownerId(-15L)
                                .ownerClass("ADDR")
                                .phoneNo("0114 2345346")
                                .phoneType("BUS")
                                .extNo(null)
                                .build())
        );
        List<AddressDto> results = offenderAddressService.getAddressesByOffenderNo(offenderNo);

        verify(offenderBookingRepository).findByOffenderNomsIdAndActiveFlag(offenderNo, "Y");
        verify(addressRepository).findAllByOwnerClassAndOwnerId("OFF", 1L);
        verify(phoneRepository).findAllByOwnerClassAndOwnerId("ADDR", -15L);

        assertThat(results).isEqualTo(List.of(
                AddressDto.builder()
                        .addressType("Home Address")
                        .noFixedAddress(false)
                        .primary(true)
                        .comment(null)
                        .flat("Flat 1")
                        .premise("Brook Hamlets")
                        .street("Mayfield Drive")
                        .postalCode("B5")
                        .country("England")
                        .county("South Yorkshire")
                        .town("Sheffield")
                        .startDate(LocalDate.of(2016, 8, 2))
                        .addressId(-15L)
                        .phones(List.of(
                                Telephone.builder()
                                        .number("0114 2345345")
                                        .ext("345")
                                        .type("HOME")
                                        .build(),
                                Telephone.builder()
                                        .number("0114 2345346")
                                        .ext(null)
                                        .type("BUS")
                                        .build()))
                        .addressUsages(List.of(AddressUsageDto.builder()
                                        .addressId(-15L)
                                        .activeFlag(true)
                                        .addressUsage("HDC")
                                        .addressUsageDescription("HDC address")
                                        .build(),
                                AddressUsageDto.builder()
                                        .addressId(-15L)
                                        .activeFlag(true)
                                        .addressUsage("HDC")
                                        .addressUsageDescription(null)
                                        .build()
                                )
                        )
                        .build(),
                AddressDto.builder()
                        .addressType("Business Address")
                        .noFixedAddress(true)
                        .primary(false)
                        .comment(null)
                        .flat(null)
                        .premise(null)
                        .street(null)
                        .postalCode(null)
                        .country("England")
                        .county(null)
                        .town(null)
                        .startDate(LocalDate.of(2016, 8, 2))
                        .addressId(-16L)
                        .phones(List.of())
                        .build())
        );
    }

    @Test
    public void testThatExceptionIsThrown_WhenMoreThanOneBookingIsFound() {
        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(any(), any()))
                .thenReturn(List.of(OffenderBooking.builder().bookingId(1L).build(), OffenderBooking.builder().bookingId(2L).build()));

        assertThatThrownBy(() -> {
            offenderAddressService.getAddressesByOffenderNo("A12345");
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("More than one active booking was returned for offender number A12345\n");
    }

    @Test
    public void testThatExceptionIsThrown_WhenNoActiveOffenderBookingsAreFound() {
        when(offenderBookingRepository.findByOffenderNomsIdAndActiveFlag(any(), any()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> {
            offenderAddressService.getAddressesByOffenderNo("A12345");
        }).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No active offender bookings found for offender number A12345\n");
    }
}
