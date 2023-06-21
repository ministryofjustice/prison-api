package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
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

  @Test
  fun findByOffenderNomsIdUnique() {
    val offender = repository.findOffenderByNomsId("A1234AL").orElseThrow()
    assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1012L, -1012L)
    assertThat(offender.bookings).hasSize(2)
    val latestBooking = offender.latestBooking.get()
    assertThat(latestBooking.bookingId).isEqualTo(-12L)
  }

  @Test
  fun findByOffendersNomsId() {
    val offenders = repository.findByNomsId("A1234AL")
    assertThat(offenders).isNotEmpty()
  }

  @Test
  fun findOffenderWithLatestBookingNoSentencesOrReleaseDetail() {
    val offender = repository.findOffendersWithLatestBookingByNomsId("A1060AA").orElseThrow()
    assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1060L, -1060L)
    assertThat(offender.bookings).hasSize(1)
    val latestBooking = offender.latestBooking.get()
    assertThat(latestBooking.bookingId).isEqualTo(-58L)
    assertThat(latestBooking.releaseDetail).isNull()
    assertThat(latestBooking.sentences.size).isEqualTo(0)
  }

  @Test
  fun findOffenderWithLatestBookingWithSentences() {
    val offender = repository.findOffendersWithLatestBookingByNomsId("A1234AL").orElseThrow()
    assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1012L, -1012L)
    assertThat(offender.bookings).hasSize(1)
    val latestBooking = offender.latestBooking.get()
    assertThat(latestBooking.bookingId).isEqualTo(-12L)
    assertThat(latestBooking.releaseDetail).isNull()
    assertThat(latestBooking.sentences.size).isEqualTo(1)
  }

  @Test
  fun findOffenderWithLatestBookingWithReleaseDetailAndSentences() {
    val offender = repository.findOffendersWithLatestBookingByNomsId("A1234AA").orElseThrow()
    assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1001L, -1001L)
    assertThat(offender.bookings).hasSize(1)
    val latestBooking = offender.latestBooking.get()
    assertThat(latestBooking.bookingId).isEqualTo(-1L)
    assertThat(latestBooking.releaseDetail.id).isEqualTo(-1L)
    assertThat(latestBooking.sentences.size).isEqualTo(1)
  }

  @Test
  fun findOffenderWithLatestBookingAliasedOffender() {
    val offender = repository.findOffendersWithLatestBookingByNomsId("A1234AI").orElseThrow()
    assertThat(offender).extracting({ it.id }, { it.rootOffender.id }).containsExactly(-1009L, -1009L)
    assertThat(offender.bookings).hasSize(1)
    assertThat(offender.latestBooking.get().bookingId).isEqualTo(-9L)
  }
}
