package uk.gov.justice.hmpps.prison.service;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.AddressUsageDto;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsage;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsageType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.County;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Person;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonInternetAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonPhone;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PersonRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    private PersonService personService;

    @BeforeEach
    void setUp() {
        personService = new PersonService(personRepository);
    }

    @Test
    public void canRetrieveAddresses() {
        final var person = Person.builder().id(-8L)
            .addresses(List.of(PersonAddress.builder()
            .addressId(-15L)
            .addressType(new AddressType("HOME", "Home Address"))
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
            .phones(Set.of(
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
                    .build())
            )
            .addressUsages(Set.of(
                AddressUsage.builder().active(true).addressUsage("HDC").addressUsageType(new AddressUsageType("HDC", "HDC address")).build(),
                AddressUsage.builder().active(true).addressUsage("HDC").build()
            ))
            .endDate(null)
                    .build(),
                PersonAddress.builder()
            .addressId(-16L)
            .addressType(new AddressType("BUS", "Business Address"))
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
            ))
            .internetAddresses(Collections.emptySet()).build();

        when(personRepository.findAddressesById(person.getId())).thenReturn(Optional.of(person));

        List<AddressDto> results = personService.getAddresses(-8L);

        // ignore Set order for phone and addresses
        RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration
            .builder()
            .withIgnoreCollectionOrder(true)
            .build();

        assertThat(results)
            .usingRecursiveFieldByFieldElementComparator(configuration).containsExactlyInAnyOrder(
            AddressDto.builder()
                .addressType("Home Address")
                .noFixedAddress(false)
                .primary(true)
                .comment(null)
                .flat("Flat 1")
                .locality("Nether Edge")
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
                .build()
        );
    }

    @Test
    public void canRetrievePhones() {

        final var person = Person.builder().id(-8L)
            .phones(
                Set.of(
                    PersonPhone.builder()
                        .phoneId(-7L)
                        .phoneNo("0114 2345345")
                        .phoneType("HOME")
                        .extNo("345")
                        .build(),
                    PersonPhone.builder()
                        .phoneId(-8L)
                        .phoneNo("0114 2345346")
                        .phoneType("BUS")
                        .extNo(null)
                        .build())
            ).build();

        when(personRepository.findById(person.getId())).thenReturn(Optional.of(person));

        List<Telephone> results = personService.getPhones(-8L);

        assertThat(results).containsExactlyInAnyOrder(
            Telephone.builder().phoneId(-7L).ext("345").number("0114 2345345").type("HOME").build(),
            Telephone.builder().phoneId(-8L).ext(null).number("0114 2345346").type("BUS").build()
        );
    }

    @Test
    public void canRetrieveEmails() {
        final var person = Person.builder().id(-8L)
            .internetAddresses(Set.of(
                PersonInternetAddress.builder().internetAddress("person1@other.com").internetAddressId(-1L).internetAddressClass("EMAIL").build()
            )).build();

        when(personRepository.findById(person.getId())).thenReturn(Optional.of(person));

        List<Email> results = personService.getEmails(-8L);

        assertThat(results).isEqualTo(List.of(Email.builder().email("person1@other.com").build()));

    }
}
