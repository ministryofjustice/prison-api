package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.dao.DataAccessException
import org.springframework.dao.QueryTimeoutException
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgyPrisonerPayProfile
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyPrisonerPayProfileRepository
import java.math.BigDecimal
import java.time.LocalDate

class AgencyPrisonerPayProfileServiceTest {
  val repository: AgencyPrisonerPayProfileRepository = mock()
  val service = AgencyPrisonerPayProfileService(repository)

  @BeforeEach
  fun setUp() {
    reset(repository)
  }

  @Test
  fun shouldReturnTheActiveAgencyPayProfile() {
    val today = LocalDate.now()
    whenever(
      repository.findAgencyPrisonerPayProfileByAgyLocId("MDI"),
    ).thenReturn(
      listOf(getFakeEntity("MDI", today.minusDays(1), today.plusDays(1))), // Active
    )

    val payProfile = service.getAgencyPrisonerPayProfile("MDI")

    with(payProfile) {
      assertThat(agencyId).isEqualTo("MDI")
      assertThat(startDate).isEqualTo(today.minusDays(1))
      assertThat(endDate).isEqualTo(today.plusDays(1))
      assertThat(autoPayFlag).isTrue
      assertThat(minHalfDayRate).isEqualTo(BigDecimal("1.25"))
      assertThat(maxHalfDayRate).isEqualTo(BigDecimal("5.25"))
      assertThat(maxBonusRate).isEqualTo(BigDecimal("4.00"))
      assertThat(maxPieceWorkRate).isEqualTo(BigDecimal("8.00"))
      assertThat(payFrequency).isEqualTo(1)
      assertThat(backdateDays).isEqualTo(7)
      assertThat(defaultPayBandCode).isEqualTo("1")
      assertThat(weeklyAbsenceLimit).isEqualTo(12)
    }
  }

  @Test
  fun shouldChooseTheActiveProfileWhenOthersExist() {
    val today = LocalDate.now()
    whenever(
      repository.findAgencyPrisonerPayProfileByAgyLocId("MDI"),
    ).thenReturn(
      listOf(
        getFakeEntity("MDI", today.minusDays(10), today.minusDays(8)), // Expired
        getFakeEntity("MDI", today.minusDays(8), today), // Active today
        getFakeEntity("MDI", today.plusDays(1), null), // Planned
      ),
    )

    val payProfile = service.getAgencyPrisonerPayProfile("MDI")

    with(payProfile) {
      assertThat(agencyId).isEqualTo("MDI")
      assertThat(startDate).isEqualTo(today.minusDays(8))
      assertThat(endDate).isEqualTo(today)
    }
  }

  @Test
  fun notFoundExceptionForAgencyPayProfile() {
    whenever(repository.findAgencyPrisonerPayProfileByAgyLocId("XXX")).thenReturn(emptyList())
    assertThatThrownBy { service.getAgencyPrisonerPayProfile("XXX") }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Resource with id [XXX] not found.")
  }

  @Test
  fun dataAccessExceptionForAgencyPayProfile() {
    whenever(
      repository.findAgencyPrisonerPayProfileByAgyLocId("XXX"),
    ).thenThrow(QueryTimeoutException("Error"))
    assertThatThrownBy { service.getAgencyPrisonerPayProfile("XXX") }
      .isInstanceOf(DataAccessException::class.java)
      .hasMessage("Error")
  }

  private fun getFakeEntity(agencyId: String, startDate: LocalDate, endDate: LocalDate?) =
    AgyPrisonerPayProfile(
      agyLocId = agencyId,
      startDate = startDate,
      endDate = endDate,
      autoPayFlag = "Y",
      minHalfDayRate = BigDecimal("1.25"),
      maxHalfDayRate = BigDecimal("5.25"),
      maxBonusRate = BigDecimal("4.00"),
      maxPieceWorkRate = BigDecimal("8.00"),
      payFrequency = 1,
      backdateDays = 7,
      defaultPayBandCode = "1",
      weeklyAbsenceLimit = 12,
    )
}
