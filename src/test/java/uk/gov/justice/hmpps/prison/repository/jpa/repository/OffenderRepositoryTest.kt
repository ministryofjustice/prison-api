@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(HmppsAuthenticationHolder::class, AuditorAwareImpl::class)
class OffenderRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderRepository

  @Nested
  inner class findRootOffenderByNomsId {
    @Test
    fun findByOffenderNomsIdUnique() {
      val offender = repository.findRootOffenderByNomsId("A1234AL").orElseThrow()
      assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1012L, -1012L)
      assertThat(offender.allBookings).hasSize(2)
      val latestBooking = offender.allBookings.minBy { it.bookingSequence }
      assertThat(latestBooking.bookingId).isEqualTo(-12L)
    }
  }

  @Nested
  inner class findRootOffenderByNomsIdForUpdate {
    @Test
    fun `test find root offender with bookings`() {
      // this prisoner has two offender records - ensure we get the right one
      val offender = repository.findRootOffenderByNomsIdForUpdate("A1234AL").orElseThrow()
      assertThat(offender.id).isEqualTo(-1012L)
      assertThat(offender.rootOffenderId).isEqualTo(-1012L)
      assertThat(offender.rootOffender.id).isEqualTo(-1012L)

      assertThat(offender.allBookings).hasSizeGreaterThan(0)
    }

    @Test
    fun `test find root offender with no bookings`() {
      // this prisoner has two offender records but no bookings
      val offender = repository.findRootOffenderByNomsIdForUpdate("A1234DD").orElseThrow()
      assertThat(offender.id).isEqualTo(-1056L)
      assertThat(offender.rootOffenderId).isEqualTo(-1056L)
      assertThat(offender.rootOffender.id).isEqualTo(-1056L)

      assertThat(offender.allBookings).hasSize(0)
    }
  }

  @Nested
  inner class updateBirthPlaceOfCurrentAlias {
    @Test
    fun `update birth place updates the offender record linked to latest booking`() {
      // this prisoner has two offender records - ensure we get the right one
      repository.updateBirthPlaceOfCurrentAlias("A1234AL", "LONDON")

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(repository.getReferenceById(-1012L).birthPlace).isEqualTo("LONDON")
      assertThat(repository.getReferenceById(-1013L).birthPlace).isEqualTo("SHEFFIELD")
    }

    @Test
    fun `update birth place does not update offender without linked booking`() {
      repository.updateBirthPlaceOfCurrentAlias("A9880GH", "LONDON")

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(repository.getReferenceById(-1059).birthPlace).isNull()
    }

    @Test
    fun `update birth place to null`() {
      // this prisoner has two offender records - ensure we get the right one
      repository.updateBirthPlaceOfCurrentAlias("A1234AL", null)

      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      assertThat(repository.getReferenceById(-1012L).birthPlace).isNull()
      assertThat(repository.getReferenceById(-1013L).birthPlace).isEqualTo("SHEFFIELD")
    }
  }
}
