package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.prison.repository.FinanceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import java.time.LocalDateTime

internal class TrustAccountServiceTest {

  lateinit var booking: OffenderBooking
  private val fromPrison = AgencyLocation().apply { description = "HMPS Brixton"; id = "BXI" }
  private val toPrison = AgencyLocation().apply { description = "HMPS Wandsworth"; id = "WWI" }
  private val toCourt = AgencyLocation().apply { description = "SHEFFIELD Crown Court"; id = "SHFCC" }

  private val transferOutToPrison = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "TRN"; description = "Transfer" }
    movementReason = MovementReason().apply { code = "TRN"; description = "Transfer" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
    movementDirection = MovementDirection.OUT
  }
  private val transferOutToCourt = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toCourt
    movementType = MovementType().apply { code = "CRT"; description = "Court" }
    movementReason = MovementReason().apply { code = "19"; description = "Witness" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
    movementDirection = MovementDirection.OUT
  }
  private val transferInToPrison = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    movementReason = MovementReason().apply { code = "INT"; description = "Inter prison transfer" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
    movementDirection = MovementDirection.IN
  }
  private val transferInToPrisonViaCourt = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    movementReason = MovementReason().apply { code = "TRNCRT"; description = "Transfer via court" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
    movementDirection = MovementDirection.IN
  }
  private val transferInToAwaitHospital = ExternalMovement().apply {
    fromAgency = toPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    movementReason = MovementReason().apply { code = MovementReason.AWAIT_REMOVAL_TO_PSY_HOSPITAL.code; description = "Await removal to hospital" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
    movementDirection = MovementDirection.IN
  }
  val movementReason = MovementReason().apply { code = "INT"; description = "Transfer In from Other Establishment" }

  @BeforeEach
  internal fun setUp() {
    booking = OffenderBooking().apply {
      externalMovements = mutableListOf(transferOutToPrison); bookingId = 99; rootOffender =
        Offender().apply { id = 55L }
    }
  }

  @Nested
  @DisplayName("When has NOMIS database")
  @ExtendWith(SpringExtension::class)
  @Import(TrustAccountSPService::class, TrustAccountNoopService::class, FinanceRepository::class)
  @ActiveProfiles(value = ["nomis"])
  inner class NOMIS {

    @Autowired
    private lateinit var trustAccountService: TrustAccountService

    @MockBean
    private lateinit var financeRepository: FinanceRepository

    @Nested
    @DisplayName("For prison to prison transfer BXI to WWI")
    inner class PrisonToPrison {
      @BeforeEach
      internal fun setUp() {
        trustAccountService.createTrustAccount(booking, transferOutToPrison.fromAgency, transferInToPrison)
      }

      @Test
      internal fun `will call store procedure to setup trust accounts`() {
        verify(financeRepository).createTrustAccount("WWI", 99L, 55L, "BXI", "INT", null, null, "WWI")
      }
    }

    @Nested
    @DisplayName("For prison to prison via court transfer BXI to Court to WWI")
    inner class PrisonToPrisonViaCourt {
      @BeforeEach
      internal fun setUp() {
        trustAccountService.createTrustAccount(booking, transferOutToCourt.fromAgency, transferInToPrisonViaCourt)
      }

      @Test
      internal fun `will call store procedure to setup trust accounts`() {
        verify(financeRepository).createTrustAccount("WWI", 99L, 55L, "BXI", "TRNCRT", null, null, "WWI")
      }
    }

    @Nested
    @DisplayName("For admission to prison to await removal to hospital")
    inner class AdmissionToPrisonAwaitHospital {
      @BeforeEach
      internal fun setUp() {
        trustAccountService.createTrustAccount(booking, transferInToAwaitHospital.fromAgency, transferInToAwaitHospital)
      }

      @Test
      internal fun `will not call store procedure to setup trust accounts`() {
        verify(financeRepository, never()).createTrustAccount(anyString(), anyLong(), anyLong(), anyString(), anyString(), anyOrNull(), anyOrNull(), anyString())
      }
    }
  }

  @Nested
  @DisplayName("When has H2 database")
  @ExtendWith(SpringExtension::class)
  @Import(TrustAccountSPService::class, TrustAccountNoopService::class, FinanceRepository::class)
  @ActiveProfiles(value = ["h2"])
  inner class H2 {
    @Autowired
    private lateinit var trustAccountService: TrustAccountService

    @MockBean
    private lateinit var financeRepository: FinanceRepository

    @Test
    internal fun `will do diddly squat`() {
      trustAccountService.createTrustAccount(booking, transferOutToPrison.fromAgency, transferInToPrison)
      verifyNoInteractions(financeRepository)
    }
  }
}
