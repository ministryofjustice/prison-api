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
import java.time.LocalDateTime
import java.util.Optional

class AgencyPrisonerPayProfileServiceTest {
  val repository: AgencyPrisonerPayProfileRepository = mock()
  val service = AgencyPrisonerPayProfileService(repository)

  @BeforeEach
  fun setUp() {
    reset(repository)
  }

  @Test
  fun shouldReturnAgencyPayProfileForMoorland() {
    val fakeEntity = getFakeEntity("MDI")

    whenever(
      repository.findAgencyPrisonerPayProfileByAgyLocIdEqualsAndEndDateIsNullAndStartDateIsLessThanEqual("MDI", LocalDate.now()),
    ).thenReturn(Optional.of(fakeEntity))

    val payProfile = service.getAgencyPrisonerPayProfile("MDI")

    with(payProfile) {
      assertThat(agencyId).isEqualTo("MDI")
      assertThat(startDate).isEqualTo(LocalDateTime.now().minusDays(1).toLocalDate())
      assertThat(endDate).isEqualTo(LocalDateTime.now().plusDays(1).toLocalDate())
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
  fun notFoundExceptionForAgencyPayProfile() {
    whenever(
      repository.findAgencyPrisonerPayProfileByAgyLocIdEqualsAndEndDateIsNullAndStartDateIsLessThanEqual("XXX", LocalDate.now()),
    ).thenThrow(EntityNotFoundException("Error"))
    assertThatThrownBy { service.getAgencyPrisonerPayProfile("XXX") }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Error")
  }

  @Test
  fun dataAccessExceptionForAgencyPayProfile() {
    whenever(
      repository.findAgencyPrisonerPayProfileByAgyLocIdEqualsAndEndDateIsNullAndStartDateIsLessThanEqual("XXX", LocalDate.now()),
    ).thenThrow(QueryTimeoutException("Error"))
    assertThatThrownBy { service.getAgencyPrisonerPayProfile("XXX") }
      .isInstanceOf(DataAccessException::class.java)
      .hasMessage("Error")
  }

  private fun getFakeEntity(agencyId: String) =
    AgyPrisonerPayProfile(
      agyLocId = agencyId,
      startDate = LocalDateTime.now().minusDays(1).toLocalDate(),
      endDate = LocalDateTime.now().plusDays(1).toLocalDate(),
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
