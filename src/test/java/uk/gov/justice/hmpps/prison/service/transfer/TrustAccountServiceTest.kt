package uk.gov.justice.hmpps.prison.service.transfer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import java.time.LocalDateTime

internal class TrustAccountServiceTest {

  lateinit var booking: OffenderBooking
  private val fromPrison = AgencyLocation().apply { description = "HMPS Brixton"; id = "BXI" }
  private val toPrison = AgencyLocation().apply { description = "HMPS Wandsworth"; id = "WWI" }
  private val bookingLastMovement = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "TRN"; description = "Transfer" }
    movementReason = MovementReason().apply { code = "TRN"; description = "Transfer" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
  }
  val movementReason = MovementReason().apply { code = "INT"; description = "Transfer In from Other Establishment" }

  @BeforeEach
  internal fun setUp() {
    booking = OffenderBooking().apply {
      externalMovements = mutableListOf(bookingLastMovement); bookingId = 99; rootOffender =
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

    @Test
    internal fun `will call store procedure to setup trust accounts`() {
      trustAccountService.createTrustAccount(booking, bookingLastMovement, movementReason)
      verify(financeRepository).createTrustAccount("WWI", 99L, 55L, "BXI", "INT", null, null, "WWI")
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
      trustAccountService.createTrustAccount(booking, bookingLastMovement, movementReason)
      verifyNoInteractions(financeRepository)
    }
  }
}
