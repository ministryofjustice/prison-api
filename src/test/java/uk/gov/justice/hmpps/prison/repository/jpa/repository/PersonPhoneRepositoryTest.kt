package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonPhone

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonPhoneRepositoryTest {
  @Autowired
  private lateinit var repository: PersonPhoneRepository

  @Autowired
  private lateinit var addressPhoneRepository: AddressPhoneRepository

  @Test
  fun findAllForAddress() {
    val expected = listOf(
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
    )

    val phones = addressPhoneRepository.findAllByAddressId(-12L)

    assertThat(phones).usingRecursiveComparison().ignoringFields("address", "createDatetime", "createUserId").isEqualTo(expected)
  }

  @Test
  fun findAllForPerson() {
    val expected = listOf(
      PersonPhone.builder()
        .phoneId(-12L)
        .phoneNo("0114 2345346")
        .phoneType("HOME")
        .extNo("345")
        .build(),
      PersonPhone.builder()
        .phoneId(-13L)
        .phoneNo("07878 7556677")
        .phoneType("MOB")
        .build(),
    )

    val phones = repository.findAllByPersonId(-8L)

    assertThat(phones).usingRecursiveComparison().ignoringFields("person", "createDatetime", "createUserId").isEqualTo(expected)
  }
}
