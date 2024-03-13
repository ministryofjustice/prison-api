package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.County
import uk.gov.justice.hmpps.prison.repository.jpa.model.Person
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonAddress
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonAddressRepositoryTest {
  @Autowired
  private lateinit var repository: PersonAddressRepository

  @Test
  fun findAllForPerson() {
    val person = Person.builder().id(-8L).build()
    val expected = listOf(
      PersonAddress.builder()
        .addressId(-15L)
        .person(person)
        .noFixedAddressFlag("N")
        .commentText(null)
        .primaryFlag("Y")
        .mailFlag("N")
        .flat("Flat 1")
        .premise("Brook Hamlets")
        .street("Mayfield Drive")
        .locality("Nether Edge")
        .postalCode("B5")
        .country(Country("ENG", "England", 1))
        .county(County("S.YORKSHIRE", "South Yorkshire"))
        .city(City("25343", "Sheffield"))
        .startDate(LocalDate.of(2016, 8, 2))
        .endDate(null)
        .addressUsages(emptySet())
        .build(),
      PersonAddress.builder()
        .addressId(-16L)
        .person(person)
        .noFixedAddressFlag("Y")
        .commentText(null)
        .primaryFlag("N")
        .mailFlag("N")
        .flat(null)
        .premise(null)
        .street(null)
        .locality(null)
        .postalCode(null)
        .country(Country("ENG", "England", 1))
        .county(null)
        .city(null)
        .startDate(LocalDate.of(2016, 8, 2))
        .endDate(null)
        .addressUsages(emptySet())
        .build(),
    )

    val addresses = repository.findAllByPersonId(person.id)

    assertThat(addresses).usingRecursiveComparison().ignoringFields("phones", "addressUsages", "person", "createDatetime", "createUserId").isEqualTo(expected)

    assertThat(
      addresses.map { address: PersonAddress -> ArrayList(address.addressUsages) },
    ).isEqualTo(listOf(emptyList(), emptyList<Any>()))
  }
}
