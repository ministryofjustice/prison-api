package uk.gov.justice.hmpps.prison.service.receiveandtransfer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AvailablePrisonIepLevel
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.IepLevel
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIepLevel
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AvailablePrisonIepLevelRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.BadRequestException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

internal class IEPTransferServiceTest {
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val availablePrisonIepLevelRepository: AvailablePrisonIepLevelRepository = mock()
  private val visitBalanceTransferService: VisitBalanceTransferService = mock()

  private val loggedInStaff = Staff().apply { staffId = 1L }

  private val fromPrison = AgencyLocation().apply { description = "HMPS Brixton"; id = "BXI" }
  private val toPrison = AgencyLocation().apply { description = "HMPS Wandsworth"; id = "WWI" }
  private val transferInMovement = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    movementReason = MovementReason().apply { code = "INT"; description = "Transfer In from Other Establishment" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    movementDirection = MovementDirection.IN
    isActive = true
  }

  @BeforeEach
  internal fun setUp() {
    whenever(authenticationFacade.currentUsername).thenReturn("TEST_USER")
    whenever(staffUserAccountRepository.findById("TEST_USER")).thenReturn(
      Optional.of(
        StaffUserAccount().apply {
          this.staff = loggedInStaff
          username = "TEST_USER"
        }
      )
    )
  }

  private val service = IEPTransferService(
    availablePrisonIepLevelRepository = availablePrisonIepLevelRepository,
    visitBalanceTransferService = visitBalanceTransferService,
    staffUserAccountRepository = staffUserAccountRepository,
    authenticationFacade = authenticationFacade
  )

  @Nested
  @DisplayName("resetLevelForPrison")
  inner class ResetLevelForPrison {
    @Nested
    inner class Success {
      lateinit var booking: OffenderBooking

      @BeforeEach
      internal fun setUp() {
        booking = OffenderBooking().apply {
          bookingId = 99; location = toPrison; iepLevels =
            mutableListOf(
              OffenderIepLevel.builder().iepLevel(IepLevel().apply { code = "ENH"; description = "Enhanced" })
                .sequence(1).iepDate(LocalDate.now().minusMonths(12)).build()
            )
        }

        whenever(availablePrisonIepLevelRepository.findByAgencyLocation_IdAndDefaultIep(toPrison.id, true)).thenReturn(
          listOf(
            AvailablePrisonIepLevel().apply {
              agencyLocation = toPrison
              iepLevel = IepLevel().apply { code = "E"; description = "Entry" }
            }
          )
        )
      }

      @Test
      internal fun `will add the default IEP level for the prison`() {
        assertThat(booking.iepLevels).hasSize(1)
        assertThat(booking.latestIepLevel.orElseThrow().iepLevel.description).isEqualTo("Enhanced")

        val updatedBooking = service.resetLevelForPrison(booking = booking, transferMovement = transferInMovement)

        assertThat(updatedBooking.iepLevels).hasSize(2)
        assertThat(updatedBooking.latestIepLevel.orElseThrow().iepLevel.description).isEqualTo("Entry")
      }

      @Test
      internal fun `will update visit balances`() {
        service.resetLevelForPrison(booking = booking, transferMovement = transferInMovement)
        verify(visitBalanceTransferService).adjustVisitBalances(booking)
      }
    }

    @Nested
    inner class Error {
      lateinit var booking: OffenderBooking

      @BeforeEach
      internal fun setUp() {
        booking = OffenderBooking().apply {
          bookingId = 99; location = toPrison; iepLevels =
            mutableListOf(
              OffenderIepLevel.builder().iepLevel(IepLevel().apply { code = "ENH"; description = "Enhanced" })
                .sequence(1).iepDate(LocalDate.now().minusMonths(12)).build()
            )
        }

        whenever(availablePrisonIepLevelRepository.findByAgencyLocation_IdAndDefaultIep(toPrison.id, true)).thenReturn(
          listOf()
        )
      }

      @Test
      internal fun `will throw exception if prison has no default IEP level`() {
        assertThrows<BadRequestException> {
          service.resetLevelForPrison(booking = booking, transferMovement = transferInMovement)
        }
      }
    }
  }
}
