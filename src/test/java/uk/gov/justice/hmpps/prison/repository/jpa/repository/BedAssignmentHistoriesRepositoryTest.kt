package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class BedAssignmentHistoriesRepositoryTest {
  @Autowired
  private lateinit var repository: BedAssignmentHistoriesRepository

  @Test
  fun maxSeqForBookingId_noRecords() {
    assertThat(repository.getMaxSeqForBookingId(-2L)).isEqualTo(0)
  }

  @Test
  fun maxSeqForBookingId_singleRecord() {
    createBedAssignmentHistories(-3L, 1)
    assertThat(repository.getMaxSeqForBookingId(-3L)).isEqualTo(1)
  }

  @Test
  fun maxSeqForBookingId_severalRecords() {
    createBedAssignmentHistories(-4L, 4)
    assertThat(repository.getMaxSeqForBookingId(-4L)).isEqualTo(4)
  }

  @Test
  fun findBedAssignmentHistory_forLocationAndDatePeriod() {
    val cellHistory = repository.findByLivingUnitIdAndDateTimeRange(
      -16,
      LocalDateTime.of(2000, 10, 16, 0, 0, 0),
      LocalDateTime.of(2020, 10, 10, 0, 0, 0),
    )
    assertThat(cellHistory)
      .extracting<Long> { it.livingUnitId }
      .containsOnly(-16L)
    assertThat(cellHistory)
      .extracting<LocalDate> { it.assignmentDate }
      .containsExactlyInAnyOrder(
        LocalDate.of(2019, 10, 17),
        LocalDate.of(2020, 4, 3),
        LocalDate.of(1985, 4, 3),
      )
  }

  @Test
  fun findBedAssignmentHistory_byDate() {
    val cellHistory = repository.findBedAssignmentHistoriesByAssignmentDateAndLivingUnitIdIn(
      LocalDate.of(2040, 10, 17),
      setOf(-16L, -17L, -18L),
    )
    assertThat(cellHistory)
      .extracting<Long> { it.livingUnitId }
      .containsOnly(-16L, -17L, -18L)
    assertThat(cellHistory)
      .extracting<LocalDate> { it.assignmentDate }
      .containsOnly(LocalDate.parse("2040-10-17"))
  }

  @Test
  fun findBedAssignmentHistory_checksTime() {
    val cellHistory = repository.findByLivingUnitIdAndDateTimeRange(
      -16,
      LocalDateTime.of(2020, 1, 1, 12, 0, 0),
      LocalDateTime.of(2020, 10, 10, 12, 12, 12),
    )
    assertThat(cellHistory)
      .extracting<Long> { it.livingUnitId }
      .containsExactlyInAnyOrder(-16L)
    assertThat(cellHistory)
      .extracting<LocalDate> { it.assignmentDate }
      .containsOnly(LocalDate.parse("2020-04-03"))
  }

  @Test
  fun findBedAssignmentHistory_mapLocation() {
    val history = repository.findByBedAssignmentHistoryPKOffenderBookingIdAndBedAssignmentHistoryPKSequence(-35L, 2)
    assertThat(history).get().extracting { it.location.locationCode }.isEqualTo("2")
  }

  private fun createBedAssignmentHistories(bookingId: Long, numberRecords: Int) {
    (1..numberRecords).forEach { seq: Int ->
      val bookingAndSequence = BedAssignmentHistoryPK(bookingId, seq)
      val bedAssignmentHistory = BedAssignmentHistory.builder()
        .bedAssignmentHistoryPK(bookingAndSequence)
        .livingUnitId(2L)
        .assignmentDate(LocalDate.now())
        .assignmentDateTime(LocalDateTime.now())
        .build()
      repository.save(bedAssignmentHistory)
    }
  }
}
