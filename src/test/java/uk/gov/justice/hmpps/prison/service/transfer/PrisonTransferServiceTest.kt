package uk.gov.justice.hmpps.prison.service.transfer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.ConflictingRequestException
import uk.gov.justice.hmpps.prison.service.CourtHearingsService
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

internal class PrisonTransferServiceTest {
  private val externalMovementService: ExternalMovementTransferService = mock()
  private val bedAssignmentTransferService: BedAssignmentTransferService = mock()
  private val trustAccountService: TrustAccountService = mock()
  private val iepTransferService: IEPTransferService = mock()
  private val caseNoteTransferService: CaseNoteTransferService = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val activityTransferService: ActivityTransferService = mock()
  private val courtHearingService: CourtHearingsService = mock()
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository = mock()
  private val transformer: OffenderTransformer = OffenderTransformer(Clock.systemDefaultZone())

  private val fromPrison = AgencyLocation().apply { description = "HMPS Brixton"; id = "BXI"; }
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

  private val service = PrisonTransferService(
    externalMovementService,
    bedAssignmentTransferService,
    trustAccountService,
    iepTransferService,
    caseNoteTransferService,
    offenderBookingRepository,
    agencyInternalLocationRepository,
    activityTransferService,
    courtHearingService,
    transformer
  )
  lateinit var booking: OffenderBooking

  @Nested
  @DisplayName("transferFromPrison")
  inner class TransferFromPrison {
    private val request = RequestToTransferIn().apply {
      this.commentText = "😎"
      this.cellLocation = "WWI-1-1"
      this.receiveTime = LocalDateTime.parse("2022-04-19T00:00:00")
    }

    @BeforeEach
    internal fun setUp() {
      booking = OffenderBooking().apply {
        externalMovements = mutableListOf(bookingLastMovement)
        bookingId = 99
        inOutStatus = "TRN"
        isActive = false
        offender = Offender().apply {
          firstName = "John"
          lastName = "Smith"
          birthDate = LocalDate.now().minusYears(30)
          gender = Gender("M", "MALE")
        }
      }
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          booking
        )
      )
      whenever(agencyInternalLocationRepository.findOneByDescriptionAndAgencyId("WWI-1-1", "WWI")).thenReturn(
        Optional.of(
          AgencyInternalLocation().apply {
            this.description = "WWI-1-1"; this.agencyId = "WWI"; this.capacity = 4; this.currentOccupancy = 3
          }
        )
      )
    }

    @Nested
    inner class Success {
      private val newMovementReason =
        MovementReason().apply { code = "INT"; description = "Transfer In from Other Establishment" }
      private val newMovement = ExternalMovement().apply {
        movementTime = LocalDateTime.parse("2022-04-20T10:00:00")
        movementReason = newMovementReason
      }

      @BeforeEach
      internal fun setUp() {
        whenever(
          externalMovementService.updateMovementsForTransfer(
            request,
            booking,
            lastMovement = bookingLastMovement
          )
        ).thenReturn(newMovement)
      }

      @Test
      internal fun `will change booking to in the new prison`() {
        val details = service.transferFromPrison("A1234AK", request)
        assertThat(details.status).isEqualTo("ACTIVE IN")
        assertThat(details.inOutStatus).isEqualTo("IN")
        assertThat(details.assignedLivingUnit.description).isEqualTo("1-1")
        assertThat(details.agencyId).isEqualTo("WWI")
        assertThat(details.statusReason).isEqualTo("ADM-INT")
      }

      @Test
      internal fun `will use reception if cell not specified`() {
        val request = RequestToTransferIn().apply {
          this.commentText = "😎"
          this.receiveTime = LocalDateTime.parse("2022-04-19T00:00:00")
        }
        whenever(agencyInternalLocationRepository.findOneByDescriptionAndAgencyId("WWI-RECP", "WWI")).thenReturn(
          Optional.of(
            AgencyInternalLocation().apply {
              this.description = "WWI-RECP"; this.agencyId = "WWI"; this.capacity = 400; this.currentOccupancy = 3
            }
          )
        )
        whenever(
          externalMovementService.updateMovementsForTransfer(
            request,
            booking,
            lastMovement = bookingLastMovement
          )
        ).thenReturn(newMovement)

        val details = service.transferFromPrison(
          "A1234AK",
          RequestToTransferIn().apply {
            this.commentText = "😎"
            this.receiveTime = LocalDateTime.parse("2022-04-19T00:00:00")
          }
        )
        assertThat(details.assignedLivingUnit.description).isEqualTo("RECP")
      }

      @Test
      internal fun `will request movements are updated`() {
        service.transferFromPrison("A1234AK", request)

        verify(externalMovementService).updateMovementsForTransfer(request, booking, lastMovement = bookingLastMovement)
      }

      @Test
      internal fun `will request trust accounts are created`() {
        service.transferFromPrison("A1234AK", request)

        verify(trustAccountService).createTrustAccount(booking, lastMovement = bookingLastMovement, newMovementReason)
      }

      @Test
      internal fun `will request IEP level is reset`() {
        service.transferFromPrison("A1234AK", request)

        verify(iepTransferService).resetLevelForPrison(booking, newMovement)
      }

      @Test
      internal fun `will request case note is created`() {
        service.transferFromPrison("A1234AK", request)

        verify(caseNoteTransferService).createGenerateAdmissionNote(booking, newMovement)
      }
    }

    @Nested
    inner class Error {
      @Test
      internal fun `will throw exception of booking not found`() {
        whenever(
          offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(
            "A1234AK",
            1
          )
        ).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.transferFromPrison("A1234AK", request)
        }
      }
    }

    @Test
    internal fun `will throw an exception of the current location is not transfer`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(bookingLastMovement)
            bookingId = 99
            inOutStatus = "OUT"
            isActive = false
            offender = Offender().apply {
              firstName = "John"
              lastName = "Smith"
              birthDate = LocalDate.now().minusYears(30)
              gender = Gender("M", "MALE")
            }
          }
        )
      )
      assertThrows<BadRequestException> {
        service.transferFromPrison("A1234AK", request)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not present at all`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf()
            bookingId = 99
            inOutStatus = "TRN"
            isActive = false
            offender = Offender().apply {
              firstName = "John"
              lastName = "Smith"
              birthDate = LocalDate.now().minusYears(30)
              gender = Gender("M", "MALE")
            }
          }
        )
      )
      assertThrows<EntityNotFoundException> {
        service.transferFromPrison("A1234AK", request)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not active`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(
              ExternalMovement().apply {
                fromAgency = fromPrison
                toAgency = toPrison
                movementType = MovementType().apply { code = "TRN"; description = "Transfer" }
                movementReason = MovementReason().apply { code = "TRN"; description = "Transfer" }
                movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
                movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
                isActive = false
              }
            )
            bookingId = 99
            inOutStatus = "TRN"
            isActive = false
            offender = Offender().apply {
              firstName = "John"
              lastName = "Smith"
              birthDate = LocalDate.now().minusYears(30)
              gender = Gender("M", "MALE")
            }
          }
        )
      )
      assertThrows<BadRequestException> {
        service.transferFromPrison("A1234AK", request)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not a transfer`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(
              ExternalMovement().apply {
                fromAgency = fromPrison
                toAgency = toPrison
                movementType = MovementType().apply { code = "ADM"; description = "Admission" }
                movementReason = MovementReason().apply { code = "TRN"; description = "Transfer" }
                movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
                movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
                isActive = true
              }
            )
            bookingId = 99
            inOutStatus = "TRN"
            isActive = false
            offender = Offender().apply {
              firstName = "John"
              lastName = "Smith"
              birthDate = LocalDate.now().minusYears(30)
              gender = Gender("M", "MALE")
            }
          }
        )
      )
      assertThrows<BadRequestException> {
        service.transferFromPrison("A1234AK", request)
      }
    }

    @Test
    internal fun `will throw exception if cell not found`() {
      assertThrows<EntityNotFoundException> {
        service.transferFromPrison(
          "A1234AK",
          RequestToTransferIn().apply {
            this.commentText = "😎"
            this.cellLocation = "BANANAS"
            this.receiveTime = LocalDateTime.parse("2022-04-19T00:00:00")
          }
        )
      }
    }

    @Test
    internal fun `will throw exception if cell is full`() {
      whenever(agencyInternalLocationRepository.findOneByDescriptionAndAgencyId("WWI-1-1", "WWI")).thenReturn(
        Optional.of(
          AgencyInternalLocation().apply {
            this.description = "WWI-1-1"; this.agencyId = "WWI"; this.capacity = 4; this.currentOccupancy = 4
          }
        )
      )

      assertThrows<ConflictingRequestException> {
        service.transferFromPrison("A1234AK", request)
      }
    }
  }
}
