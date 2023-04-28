package uk.gov.justice.hmpps.prison.repository.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffenderChargeRepositoryTest {

  @Autowired
  private lateinit var offenderChargeRepository: OffenderChargeRepository

  @Test
  fun `should find active offences by booking ids`() {
    val bookingIds = setOf(-4L, -59L, -7L)
    val charges = offenderChargeRepository.findActiveOffencesByBookingIds(bookingIds)
    assertThat(charges).hasSize(4)
    charges.forEach {
      assertThat(it.isActive)
      assertThat(it.offenderBooking.bookingId).isIn(bookingIds)
      assertThat(it.isActive).isTrue()
    }
  }
}
