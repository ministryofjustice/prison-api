package net.syscon.prison.service;

import net.syscon.prison.api.model.AddressDto;
import net.syscon.prison.api.model.AddressUsageDto;
import net.syscon.prison.api.model.Email;
import net.syscon.prison.api.model.Telephone;
import net.syscon.prison.repository.PersonRepository;
import net.syscon.prison.repository.jpa.model.Address;
import net.syscon.prison.repository.jpa.model.AddressType;
import net.syscon.prison.repository.jpa.model.AddressUsage;
import net.syscon.prison.repository.jpa.model.AddressUsageType;
import net.syscon.prison.repository.jpa.model.City;
import net.syscon.prison.repository.jpa.model.Country;
import net.syscon.prison.repository.jpa.model.County;
import net.syscon.prison.repository.jpa.model.InternetAddress;
import net.syscon.prison.repository.jpa.model.Phone;
import net.syscon.prison.repository.jpa.repository.AddressRepository;
import net.syscon.prison.repository.jpa.repository.InternetAddressRepository;
import net.syscon.prison.repository.jpa.repository.PhoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PhoneRepository phoneRepository;

    @Mock
    private InternetAddressRepository internetAddressRepository;

    private PersonService personService;

    @BeforeEach
    void setUp() {
        personService = new PersonService(personRepository, addressRepository, phoneRepository, internetAddressRepository);
    }


    @Test
    public void canRetrieveAddresses() {
        when(addressRepository.findAllByOwnerClassAndOwnerId("PER", -8L)).thenReturn(List.of(
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
                        .addressUsages(List.of(
                                AddressUsage.builder().activeFlag("Y").addressUsage("HDC").addressUsageType(new AddressUsageType("HDC", "HDC address")).build(),
                                AddressUsage.builder().activeFlag("Y").addressUsage("HDC").build()
                        ))
                        .endDate(null)
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

        when(phoneRepository.findAllByOwnerClassAndOwnerId("ADDR", -16L)).thenReturn(List.of());

        List<AddressDto> results = personService.getAddresses(-8L);

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
                        .addressUsages(List.of())
                        .build())
        );
    }

    @Test
    public void canRetrievePhones() {

        when(phoneRepository.findAllByOwnerClassAndOwnerId("PER", -8L)).thenReturn(
                List.of(
                        Phone.builder()
                                .phoneId(-7L)
                                .ownerId(-8L)
                                .ownerClass("PER")
                                .phoneNo("0114 2345345")
                                .phoneType("HOME")
                                .extNo("345")
                                .build(),
                        Phone.builder()
                                .phoneId(-8L)
                                .ownerId(-8L)
                                .ownerClass("PER")
                                .phoneNo("0114 2345346")
                                .phoneType("BUS")
                                .extNo(null)
                                .build())
        );

        List<Telephone> results = personService.getPhones(-8L);

        assertThat(results).isEqualTo(List.of(
                Telephone.builder().ext("345").number("0114 2345345").type("HOME").build(),
                Telephone.builder().ext(null).number("0114 2345346").type("BUS").build()
        ));

    }

    @Test
    public void canRetrieveEmails() {
        when(internetAddressRepository.findByOwnerClassAndOwnerIdAndInternetAddressClass("PER", -8L, "EMAIL")).thenReturn(List.of(
                InternetAddress.builder().internetAddress("person1@other.com").internetAddressId(-1L).internetAddressClass("EMAIL").build()
        ));

        List<Email> results = personService.getEmails(-8L);

        assertThat(results).isEqualTo(List.of(Email.builder().email("person1@other.com").build()));

    }
}
