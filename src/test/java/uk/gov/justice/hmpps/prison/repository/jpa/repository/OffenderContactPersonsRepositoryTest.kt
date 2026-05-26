package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
class OffenderContactPersonsRepositoryTest(
  @Autowired private val repository: OffenderContactPersonsRepository,
) {
  @Test
  fun findAllByPersonIdAndOffenderBooking_BookingId() {
    val contacts = repository.findAllByPersonIdAndOffenderBooking_BookingId(-1L, -1L)

    assertThat(contacts).hasSize(2)
    assertThat(contacts)
      .extracting<RelationshipType, RuntimeException> { it.relationshipType }
      .containsExactlyInAnyOrder(RelationshipType("UN", "Uncle"), RelationshipType("FRI", "Friend"))
  }
}
