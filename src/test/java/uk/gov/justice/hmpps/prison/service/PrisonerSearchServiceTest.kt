package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderLanguageRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.time.LocalDate
import java.time.LocalDateTime

class PrisonerSearchServiceTest {

  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val offenderTransformer: OffenderTransformer = mock()
  private val inmateService: InmateService = mock()
  private val healthService: HealthService = mock()
  private val offenderLanguageRepository: OffenderLanguageRepository = mock()

  var service = PrisonerSearchService(
    offenderBookingRepository,
    offenderRepository,
    offenderTransformer,
    inmateService,
    healthService,
    offenderLanguageRepository,
  )

  @Test
  fun `should handle a missing fromAgency`() {
    val movementTimestamp = LocalDateTime.parse("2025-10-10T12:00")
    val result = OffenderBooking().apply {
      isActive = true
      location = AgencyLocation().apply {
        id = "MDI"
      }
      externalMovements = listOf(
        ExternalMovement().apply {
          movementSequence = 1
          movementDirection = MovementDirection.OUT
          movementDate = LocalDate.parse("2025-08-08")
          movementTime = LocalDateTime.parse("2025-08-08T08:00")
          fromAgency = null
        },
        ExternalMovement().apply {
          movementSequence = 2
          movementDirection = MovementDirection.OUT
          movementDate = LocalDate.parse("2025-10-10")
          movementTime = movementTimestamp
          fromAgency = AgencyLocation().apply {
            id = "SWI"
            type = AgencyLocationType.PRISON_TYPE
          }
        },
      )
    }.getPreviousPrisonTransfer()
    assertThat(result).isEqualTo("SWI" to movementTimestamp)
  }
}
