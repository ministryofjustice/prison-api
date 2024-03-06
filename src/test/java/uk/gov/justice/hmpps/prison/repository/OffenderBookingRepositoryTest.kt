package uk.gov.justice.hmpps.prison.repository

import lombok.extern.slf4j.Slf4j
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingFilter
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class, PersistenceConfigs::class)
@WithMockUser
@Slf4j
class OffenderBookingRepositoryTest {
  @Autowired
  private lateinit var repository: OffenderBookingRepository

  @Autowired
  private lateinit var agencyLocationRepository: AgencyLocationRepository

  @Nested
  @DisplayName("OffenderBookingRepositoryTest with OffenderBookingFilter")
  inner class OffenderBookingFilterTest {
    @Test
    @DisplayName("can find all for a booking")
    fun canFindAllForABooking() {
      val filter = OffenderBookingFilter
        .builder()
        .bookingSequence(1)
        .active(true)
        .build()

      val pageOfBookings = repository.findAll(
        filter,
        PageRequest.of(0, 10, Sort.by("bookingId")),
      )

      assertThat(pageOfBookings.content).hasSize(10)
    }

    @Test
    @DisplayName("can find all bookings in a prison")
    fun canFindAllForABookingInAPrison() {
      val filter = OffenderBookingFilter
        .builder()
        .bookingSequence(1)
        .active(true)
        .prisonId("LEI")
        .build()

      val pageOfBookings = repository.findAll(
        filter,
        PageRequest.of(0, 10, Sort.by("bookingId")),
      )

      assertThat(pageOfBookings.content).hasSize(10)
    }

    @Test
    @DisplayName("can find all bookings filtering by caseload")
    fun canFindAllForABookingFilteringByCaseloads() {
      val filter = OffenderBookingFilter
        .builder()
        .bookingSequence(1)
        .active(true)
        .caseloadIds(listOf("BXI"))
        .build()

      val pageOfBookings = repository.findAll(
        filter,
        PageRequest.of(0, 10, Sort.by("bookingId")),
      )

      assertThat(pageOfBookings.content).hasSize(2)
    }

    @Test
    @DisplayName("can find all bookings from a list of nomsIds")
    fun canFindAllForABookingInAListOfNomsIds() {
      val filter = OffenderBookingFilter
        .builder()
        .bookingSequence(1)
        .active(true)
        .offenderNos(listOf("A1234AA", "A1234AB"))
        .build()

      val pageOfBookings = repository.findAll(
        filter,
        PageRequest.of(0, 10, Sort.by("bookingId")),
      )

      assertThat(pageOfBookings.content).hasSize(2)
    }

    @Test
    @DisplayName("can find all bookings from a list of booking Ids")
    fun canFindAllBookingsInListOfBookingIds() {
      val filter = OffenderBookingFilter
        .builder()
        .bookingSequence(1)
        .active(true)
        .bookingIds(listOf(-1L, -2L))
        .build()

      val pageOfBookings = repository.findAll(
        filter,
        PageRequest.of(0, 10, Sort.by("bookingId")),
      )

      assertThat(pageOfBookings.content).hasSize(2)
    }

    @Test
    @DisplayName("can find by primary key")
    fun canFindByPrimaryKey() {
      val booking = repository.findById(-1L)

      assertThat(booking).isPresent()
    }
  }

  @Test
  fun `test findWithSentenceSummaryByOffenderNomsIdAndBookingSequence returns correct number of sentences`() {
    val bookings = repository.findAllByBookingIdIn(listOf(-3L))

    assertThat(bookings.first { it.bookingId == -3L }.sentences).extracting<LocalDate> { it.sentenceStartDate }.containsOnlyOnce(
      LocalDate.parse("2017-07-04"),
      LocalDate.parse("2017-07-05"),
    )
  }

  @Test
  fun `check things`() {
    val agencyLocation = agencyLocationRepository.getReferenceById("LEI")

    val ids = repository.findDistinctByActiveTrueAndLocationAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
      agencyLocation,
      "A",
      "BOB",
      "JOE",
      PageRequest.of(0, 2, Sort.by("bookingId")),
    )
    assertThat(ids.content).hasSize(2)
    assertThat(ids.totalElements).isGreaterThanOrEqualTo(20)

    val bookings = repository.findAllByBookingIdIn(ids.content.map { it.bookingId })
    assertThat(bookings).hasSize(2)
  }
}
