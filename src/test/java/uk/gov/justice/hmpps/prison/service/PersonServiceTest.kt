package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.AddressDto
import uk.gov.justice.hmpps.prison.api.model.AddressUsageDto
import uk.gov.justice.hmpps.prison.api.model.Email
import uk.gov.justice.hmpps.prison.api.model.Telephone
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressType
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsage
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsageType
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.County
import uk.gov.justice.hmpps.prison.repository.jpa.model.Person
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonAddress
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonInternetAddress
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonPhone
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PersonRepository
import java.time.LocalDate
import java.util.Optional

class PersonServiceTest {
  private val personRepository: PersonRepository = mock()

  private val personService: PersonService = PersonService(personRepository)

  @Test
  fun canRetrieveAddresses() {
    val person = Person.builder().id(-8L)
      .addresses(
        listOf(
          PersonAddress.builder()
            .addressId(-15L)
            .addressType(AddressType("HOME", "Home Address"))
            .noFixedAddressFlag("N")
            .commentText(null)
            .primaryFlag("Y")
            .mailFlag("N")
            .flat("Flat 1")
            .premise("Brook Hamlets")
            .street("Mayfield Drive")
            .locality("Nether Edge")
            .postalCode("B5")
            .country(Country("ENG", "England"))
            .county(County("S.YORKSHIRE", "South Yorkshire"))
            .city(City("25343", "Sheffield"))
            .startDate(LocalDate.of(2016, 8, 2))
            .phones(
              setOf(
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
                  .build(),
              ),
            )
            .addressUsages(
              setOf(
                AddressUsage(null, true, AddressUsageType("HDC", "HDC address")),
              ),
            )
            .endDate(null)
            .build(),
          PersonAddress.builder()
            .addressId(-16L)
            .addressType(AddressType("BUS", "Business Address"))
            .noFixedAddressFlag("Y")
            .commentText(null)
            .primaryFlag("N")
            .mailFlag("N")
            .flat(null)
            .premise(null)
            .street(null)
            .locality(null)
            .postalCode(null)
            .country(Country("ENG", "England"))
            .county(null)
            .city(null)
            .startDate(LocalDate.of(2016, 8, 2))
            .endDate(null)
            .build(),
        ),
      )
      .internetAddresses(mutableSetOf<PersonInternetAddress?>()).build()

    whenever(personRepository.findAddressesById(person.id))
      .thenReturn(Optional.of(person))

    val results = personService.getAddresses(-8L)

    // ignore Set order for phone and addresses
    val configuration = RecursiveComparisonConfiguration
      .builder()
      .withIgnoreCollectionOrder(true)
      .build()

    assertThat(results)
      .usingRecursiveFieldByFieldElementComparator(configuration).containsExactlyInAnyOrder(
        AddressDto.builder()
          .addressType("Home Address")
          .noFixedAddress(false)
          .primary(true)
          .mail(false)
          .comment(null)
          .flat("Flat 1")
          .locality("Nether Edge")
          .premise("Brook Hamlets")
          .street("Mayfield Drive")
          .postalCode("B5")
          .country("England")
          .countryCode("ENG")
          .county("South Yorkshire")
          .countyCode("S.YORKSHIRE")
          .town("Sheffield")
          .townCode("25343")
          .startDate(LocalDate.of(2016, 8, 2))
          .addressId(-15L)
          .phones(
            listOf(
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
                .build(),
            ),
          )
          .addressUsages(
            listOf(
              AddressUsageDto.builder()
                .addressId(-15L)
                .activeFlag(true)
                .addressUsage("HDC")
                .addressUsageDescription("HDC address")
                .build(),
            ),
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
          .countryCode("ENG")
          .county(null)
          .countyCode(null)
          .town(null)
          .townCode(null)
          .startDate(LocalDate.of(2016, 8, 2))
          .addressId(-16L)
          .phones(mutableListOf<Telephone?>())
          .addressUsages(mutableListOf<AddressUsageDto?>())
          .build(),
      )
  }

  @Test
  fun canRetrievePhones() {
    val person = Person.builder().id(-8L)
      .phones(
        setOf(
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
            .build(),
        ),
      ).build()

    whenever(personRepository.findById(person.id))
      .thenReturn(Optional.of(person))

    val results = personService.getPhones(-8L)

    assertThat(results).containsExactlyInAnyOrder(
      Telephone.builder().phoneId(-7L).ext("345").number("0114 2345345").type("HOME").build(),
      Telephone.builder().phoneId(-8L).ext(null).number("0114 2345346").type("BUS").build(),
    )
  }

  @Test
  fun canRetrieveEmails() {
    val person = Person.builder().id(-8L)
      .internetAddresses(
        setOf(
          PersonInternetAddress.builder().internetAddress("person1@other.com").internetAddressId(-1L)
            .internetAddressClass("EMAIL").build(),
        ),
      ).build()

    whenever(personRepository.findById(person.id))
      .thenReturn(Optional.of(person))

    val results = personService.getEmails(-8L)

    assertThat(results)
      .isEqualTo(listOf(Email.builder().email("person1@other.com").build()))
  }
}
