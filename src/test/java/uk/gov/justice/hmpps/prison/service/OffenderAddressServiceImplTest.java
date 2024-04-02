package uk.gov.justice.hmpps.prison.service;


import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.AddressUsageDto;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsage;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsageType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.County;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderAddressServiceImplTest {

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private OffenderAddressRepository offenderAddressRepository;

    private OffenderAddressService offenderAddressService;

    @BeforeEach
    public void setUp() {
        offenderAddressService = new OffenderAddressService(offenderRepository,offenderAddressRepository);
    }

    @Test
    public void canRetrieveAddresses() {

        final var offenderNo = "off-1";

        final var offender = Offender.builder().id(1L).rootOffenderId(1L).build();
        offender.setRootOffender(offender);
        when(offenderRepository.findOffenderWithLatestBookingByNomsId(any())).thenReturn(Optional.of(offender));
        var addresses = List.of(
                OffenderAddress.builder()
                        .addressId(-15L)
                        .addressType(new AddressType("HOME", "Home Address"))
                        .offender(offender)
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
                    .phones( Set.of(
                        AddressPhone.builder()
                            .phoneId(-7L)
                            .phoneNo("0114 2345345")
                            .phoneType("HOME")
                            .extNo("345")
                            .build(),
                        AddressPhone.builder()
                            .phoneId(-8L)
                            .phoneNo("0114 2345346")
                            .phoneType("BUS")
                            .extNo(null)
                            .build()))
                        .addressUsages(Set.of(
                                AddressUsage.builder().active(true).addressUsage("HDC").addressUsageType(new AddressUsageType("HDC", "HDC address")).build(),
                                AddressUsage.builder().active(true).addressUsage("HDC").build()
                        ))
                        .build(),
            OffenderAddress.builder()
                        .addressId(-16L)
                        .addressType(new AddressType("BUS", "Business Address"))
                        .offender(offender)
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
        );
        when(offenderAddressRepository.findByOffenderId(1L)).thenReturn(addresses);

        List<AddressDto> results = offenderAddressService.getAddressesByOffenderNo(offenderNo);

        verify(offenderRepository).findOffenderWithLatestBookingByNomsId(offenderNo);

        // ignore Set order for phone and addresses
        RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration
            .builder()
            .withIgnoreCollectionOrder(true)
            .build();

        assertThat(results)
            .usingRecursiveFieldByFieldElementComparator(configuration)
            .isEqualTo(List.of(
                AddressDto.builder()
                    .addressType("Home Address")
                    .noFixedAddress(false)
                    .primary(true)
                    .mail(false)
                    .comment(null)
                    .flat("Flat 1")
                    .premise("Brook Hamlets")
                    .street("Mayfield Drive")
                    .postalCode("B5")
                    .locality("Nether Edge")
                    .country("England")
                    .county("South Yorkshire")
                    .town("Sheffield")
                    .startDate(LocalDate.of(2016, 8, 2))
                    .addressId(-15L)
                    .phones(List.of(
                        Telephone.builder()
                            .phoneId(-7L)
                            .number("0114 2345345")
                            .ext("345")
                            .type("HOME")
                            .build(),
                        Telephone.builder()
                            .phoneId(-8L)
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
                    .mail(false)
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
                    .addressUsages(List.of())
                    .build()));

    }

    @Test
    public void testThatExceptionIsThrown_WhenOffenderIsFound() {
        when(offenderRepository.findOffenderWithLatestBookingByNomsId(any()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> offenderAddressService.getAddressesByOffenderNo("A12345"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("No offender found for offender number A12345\n");
    }
}
