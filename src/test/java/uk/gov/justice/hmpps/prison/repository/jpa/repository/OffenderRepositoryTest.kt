@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class OffenderRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderRepository

  @Nested
  inner class findOffenderByNomsId {
    @Test
    fun findByOffenderNomsIdUnique() {
      val offender = repository.findOffenderByNomsId("A1234AL").orElseThrow()
      assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1012L, -1012L)
      assertThat(offender.bookings).hasSize(2)
      val latestBooking = offender.latestBooking.get()
      assertThat(latestBooking.bookingId).isEqualTo(-12L)
    }
  }

  @Nested
  inner class findByNomsId {
    @Test
    fun findByOffendersNomsId() {
      val offenders = repository.findByNomsId("A1234AL")
      assertThat(offenders).extracting(Offender::getId).containsExactly(tuple(-1012L), tuple(-1013L))
    }
  }

  @Nested
  inner class findOffenderWithLatestBookingByNomsId {
    @Test
    fun findOffenderWithLatestBookingNoSentencesOrReleaseDetail() {
      val offender = repository.findOffenderWithLatestBookingByNomsId("A1060AA").orElseThrow()
      assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1060L, -1060L)
      assertThat(offender.bookings).hasSize(1)
      val latestBooking = offender.latestBooking.get()
      assertThat(latestBooking.bookingId).isEqualTo(-58L)
      assertThat(latestBooking.releaseDetail).isNull()
      assertThat(latestBooking.sentences.size).isEqualTo(0)
    }

    @Test
    fun findOffenderWithLatestBookingWithSentences() {
      val offender = repository.findOffenderWithLatestBookingByNomsId("A1234AL").orElseThrow()
      assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1012L, -1012L)
      assertThat(offender.bookings).hasSize(1)
      val latestBooking = offender.latestBooking.get()
      assertThat(latestBooking.bookingId).isEqualTo(-12L)
      assertThat(latestBooking.releaseDetail).isNull()
      assertThat(latestBooking.sentences.size).isEqualTo(1)
    }

    @Test
    fun findOffenderWithLatestBookingWithReleaseDetailAndSentences() {
      val offender = repository.findOffenderWithLatestBookingByNomsId("A1234AA").orElseThrow()
      assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1001L, -1001L)
      assertThat(offender.bookings).hasSize(1)
      val latestBooking = offender.latestBooking.get()
      assertThat(latestBooking.bookingId).isEqualTo(-1L)
      assertThat(latestBooking.releaseDetail.id).isEqualTo(-1L)
      assertThat(latestBooking.sentences.size).isEqualTo(1)
    }

    @Test
    fun findOffenderWithLatestBookingAliasedOffender() {
      val offender = repository.findOffenderWithLatestBookingByNomsId("A1234AI").orElseThrow()
      assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1009L, -1009L)
      assertThat(offender.bookings).hasSize(1)
      assertThat(offender.latestBooking.get().bookingId).isEqualTo(-9L)
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

      assertThat(offender.bookings).hasSizeGreaterThan(0)
    }

    @Test
    fun `test find root offender with no bookings`() {
      // this prisoner has two offender records but no bookings
      val offender = repository.findRootOffenderByNomsIdForUpdate("A1234DD").orElseThrow()
      assertThat(offender.id).isEqualTo(-1056L)
      assertThat(offender.rootOffenderId).isEqualTo(-1056L)
      assertThat(offender.rootOffender.id).isEqualTo(-1056L)

      assertThat(offender.bookings).hasSize(0)
    }
  }
}
