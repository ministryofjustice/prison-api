package uk.gov.justice.hmpps.prison.service.transfer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
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
  private val agencyLocationRepository: AgencyLocationRepository = mock()
  private val teamWorkflowNotificationService: TeamWorkflowNotificationService = mock()
  private val transformer: OffenderTransformer = OffenderTransformer(Clock.systemDefaultZone())

  private val fromPrison = AgencyLocation().apply { description = "HMPS Brixton"; id = "BXI"; }
  private val toPrison = AgencyLocation().apply { description = "HMPS Wandsworth"; id = "WWI" }
  private val toCourt =
    AgencyLocation().apply { description = "Court1"; id = "CA"; type = AgencyLocationType.COURT_TYPE }

  private val bookingLastMovementTransfer = getMovement()

  private val bookingLastMovementCourt =
    getMovement(toAgencyIn = toCourt, movementReasonCode = "CRT", movementTypeCode = "CRT")

  private val bookingLastMovementCourtWithEventId =
    getMovement(toAgencyIn = toCourt, movementReasonCode = "CRT", movementTypeCode = "CRT", eventIdIn = 123)

  private val service = PrisonTransferService(
    externalMovementService,
    bedAssignmentTransferService,
    trustAccountService,
    iepTransferService,
    caseNoteTransferService,
    offenderBookingRepository,
    agencyInternalLocationRepository,
    agencyLocationRepository,
    activityTransferService,
    courtHearingService,
    teamWorkflowNotificationService,
    transformer,
  )
  lateinit var booking: OffenderBooking

  private fun getMovement(
    movementTypeCode: String = "TRN",
    movementReasonCode: String = "TRN",
    toAgencyIn: AgencyLocation = toPrison,
    eventIdIn: Long? = null,
    active: Boolean = true
  ): ExternalMovement {
    return ExternalMovement().apply {
      fromAgency = fromPrison
      toAgency = toAgencyIn
      movementType = MovementType().apply { code = movementTypeCode; description = "type description" }
      movementReason = MovementReason().apply { code = movementReasonCode; description = "code description" }
      movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
      movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
      isActive = active
      eventId = eventIdIn
    }
  }

  @BeforeEach
  internal fun setUp() {
    whenever(teamWorkflowNotificationService.sendTransferViaCourtNotification(any(), any())).thenAnswer {
      // make sure lambda is called
      it.getArgument<() -> ExternalMovement>(1)()
    }
  }

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
        externalMovements = mutableListOf(bookingLastMovementTransfer)
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
      private val newMovementType =
        MovementType().apply { code = "ADM"; description = "Admission" }
      private val newMovement = ExternalMovement().apply {
        movementTime = LocalDateTime.parse("2022-04-20T10:00:00")
        movementReason = newMovementReason
        movementType = newMovementType
      }

      @BeforeEach
      internal fun setUp() {
        whenever(
          externalMovementService.updateMovementsForTransfer(
            request,
            booking,
            lastMovement = bookingLastMovementTransfer
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
            lastMovement = bookingLastMovementTransfer
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

        verify(externalMovementService).updateMovementsForTransfer(
          request,
          booking,
          lastMovement = bookingLastMovementTransfer
        )
      }

      @Test
      internal fun `will request trust accounts are created`() {
        service.transferFromPrison("A1234AK", request)

        verify(trustAccountService).createTrustAccount(
          booking,
          movementOut = bookingLastMovementTransfer,
          movementIn = newMovement
        )
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
            externalMovements = mutableListOf(bookingLastMovementTransfer)
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
              getMovement(active = false)
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
              bookingLastMovementCourt
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

  @Nested
  @DisplayName("transferFromCourtToSamePrison")
  inner class TransferFromCourtToSamePrison {
    private val requestCourtSamePrison = RequestForCourtTransferIn().apply {
      this.commentText = "😎"
      this.agencyId = fromPrison.id
      this.movementReasonCode = "CRT"
      this.dateTime = LocalDateTime.parse("2022-04-19T00:00:00")
    }

    private val movementSamePrisonTime = LocalDateTime.parse("2022-04-20T10:00:00")

    @BeforeEach
    internal fun setUp() {
      booking = OffenderBooking().apply {
        externalMovements = mutableListOf(bookingLastMovementCourt)
        bookingId = 99
        inOutStatus = "OUT"
        isActive = true
        location = fromPrison
        statusReason = "CRT-CRT"
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
        MovementReason().apply { code = "CRT"; description = "Return to same prison after court" }
      private val newMovementType =
        MovementType().apply { code = "CRT"; description = "Court" }
      private val newMovement = ExternalMovement().apply {
        movementTime = movementSamePrisonTime
        movementReason = newMovementReason
        movementType = newMovementType
      }

      @BeforeEach
      internal fun setUp() {
        whenever(
          externalMovementService.updateMovementsForCourtTransferToSamePrison(
            "CRT",
            movementSamePrisonTime,
            booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
            commentText = "😎"
          )
        ).thenReturn(newMovement)
      }

      @Test
      internal fun `will request movements are updated`() {
        service.transferViaCourt("A1234AK", requestCourtSamePrison)

        verify(externalMovementService).updateMovementsForCourtTransferToSamePrison(
          requestCourtSamePrison.movementReasonCode,
          requestCourtSamePrison.dateTime,
          booking,
          lastMovement = bookingLastMovementCourt,
          courtEvent = null,
          commentText = requestCourtSamePrison.commentText
        )
      }

      @Test
      internal fun `will request court events are updated if event id present`() {
        val bookingWithCourtEventId = OffenderBooking().apply {
          externalMovements = mutableListOf(bookingLastMovementCourtWithEventId)
          bookingId = 99
          inOutStatus = "OUT"
          isActive = true
          location = fromPrison
          statusReason = "CRT-CRT"
          offender = Offender().apply {
            firstName = "John"
            lastName = "Smith"
            birthDate = LocalDate.now().minusYears(30)
            gender = Gender("M", "MALE")
          }
        }

        whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
          Optional.of(
            bookingWithCourtEventId
          )
        )

        whenever(
          externalMovementService.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = requestCourtSamePrison.movementReasonCode,
            movementDateTime = requestCourtSamePrison.dateTime,
            booking = bookingWithCourtEventId,
            lastMovement = bookingLastMovementCourtWithEventId,
            courtEvent = null,
            commentText = requestCourtSamePrison.commentText
          )
        ).thenReturn(newMovement)
        service.transferViaCourt("A1234AK", requestCourtSamePrison)

        verify(courtHearingService).completeScheduledChildHearingEvent(booking.bookingId, 123)
      }

      @Test
      internal fun `will not request court events are updated if event id present`() {
        service.transferViaCourt("A1234AK", requestCourtSamePrison)
        verifyNoInteractions(courtHearingService)
      }

      @Test
      internal fun `will update booking status`() {
        val inmateDetails = service.transferViaCourt("A1234AK", requestCourtSamePrison)
        with(inmateDetails) {
          assertThat(inOutStatus).isEqualTo(MovementDirection.IN.name)
          assertThat(statusReason).isEqualTo("CRT-CRT")
        }
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
          service.transferViaCourt("A1234AK", requestCourtSamePrison)
        }
      }
    }

    @Test
    internal fun `will throw an exception of the current location is not transfer`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(bookingLastMovementTransfer)
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
        service.transferViaCourt("A1234AK", requestCourtSamePrison)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not present at all`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf()
            bookingId = 99
            inOutStatus = "OUT"
            isActive = true
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
        service.transferViaCourt("A1234AK", requestCourtSamePrison)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not active`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(
              getMovement(active = false)
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
        service.transferViaCourt("A1234AK", requestCourtSamePrison)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not a court transfer`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(bookingLastMovementTransfer)
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
        service.transferViaCourt("A1234AK", requestCourtSamePrison)
      }
    }
  }

  @Nested
  @DisplayName("transferFromCourtToDifferentPrison")
  inner class TransferFromCourtToDifferentPrison {
    private val requestCourtDifferentPrison = RequestForCourtTransferIn().apply {
      this.commentText = "😎"
      this.agencyId = toPrison.id
      this.dateTime = LocalDateTime.parse("2022-04-19T00:00:00")
    }

    @BeforeEach
    internal fun setUp() {
      booking = OffenderBooking().apply {
        externalMovements = mutableListOf(bookingLastMovementCourt)
        bookingId = 99
        inOutStatus = "OUT"
        isActive = true
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
      whenever(agencyInternalLocationRepository.findOneByDescriptionAndAgencyId("WWI-RECP", "WWI")).thenReturn(
        Optional.of(
          AgencyInternalLocation().apply {
            this.description = "WWI-RECP"; this.agencyId = "WWI"; this.capacity = 4; this.currentOccupancy = 3
          }
        )
      )
    }

    @Nested
    inner class Success {
      private val newMovementReason =
        MovementReason().apply { code = "TRNCRT"; description = "Transfer Ifrom court" }
      private val newMovementType =
        MovementType().apply { code = "ADM"; description = "Admission" }
      private val newMovement = ExternalMovement().apply {
        movementTime = LocalDateTime.parse("2022-04-20T10:00:00")
        movementDate = LocalDate.parse("2022-04-20")
        movementReason = newMovementReason
        movementType = newMovementType
      }

      @BeforeEach
      internal fun setUp() {
        whenever(
          externalMovementService.updateMovementsForCourtTransferToDifferentPrison(
            movementDateTime = requestCourtDifferentPrison.dateTime,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            toAgency = toPrison,
            commentText = requestCourtDifferentPrison.commentText
          )
        ).thenReturn(newMovement)

        whenever(agencyLocationRepository.findById("WWI")).thenReturn(
          Optional.of(
            AgencyLocation().apply {
              this.id = "WWI"
              this.description = "HMPS Wandsworth"
            }
          )
        )
      }

      @Test
      internal fun `will change booking to in the new prison`() {
        val details = service.transferViaCourt("A1234AK", requestCourtDifferentPrison)
        assertThat(details.status).isEqualTo("ACTIVE IN")
        assertThat(details.inOutStatus).isEqualTo("IN")
        assertThat(details.assignedLivingUnit.description).isEqualTo("RECP")
        assertThat(details.agencyId).isEqualTo("WWI")
        assertThat(details.statusReason).isEqualTo("ADM-TRNCRT")
      }

      @Test
      internal fun `will allocate reception cell`() {
        val details = service.transferViaCourt("A1234AK", requestCourtDifferentPrison)
        assertThat(details.assignedLivingUnit.description).isEqualTo("RECP")
      }

      @Test
      internal fun `will request movements are updated`() {
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)

        verify(externalMovementService).updateMovementsForCourtTransferToDifferentPrison(
          requestCourtDifferentPrison.dateTime,
          booking,
          lastMovement = bookingLastMovementCourt,
          commentText = requestCourtDifferentPrison.commentText,
          toAgency = toPrison
        )
      }

      @Test
      internal fun `will request trust accounts are created`() {
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)

        verify(trustAccountService).createTrustAccount(
          booking,
          movementOut = bookingLastMovementCourt,
          movementIn = newMovement
        )
      }

      @Test
      internal fun `will request to end activities and wait list at previous prison`() {
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)

        verify(activityTransferService).endActivitiesAndWaitlist(
          booking,
          fromAgency = fromPrison,
          endDate = newMovement.movementDate,
          endReason = OffenderProgramEndReason.TRF.code
        )
      }

      @Test
      internal fun `will request IEP level is reset`() {
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)

        verify(iepTransferService).resetLevelForPrison(booking, newMovement)
      }

      @Test
      internal fun `will request case note is created`() {
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)

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
          service.transferViaCourt("A1234AK", requestCourtDifferentPrison)
        }
      }
    }

    @Test
    internal fun `will throw an exception of the current location is not court transfer`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(bookingLastMovementTransfer)
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
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not present at all`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf()
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
      assertThrows<EntityNotFoundException> {
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)
      }
    }

    @Test
    internal fun `will throw an exception if latest movement is not active`() {
      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence("A1234AK", 1)).thenReturn(
        Optional.of(
          OffenderBooking().apply {
            externalMovements = mutableListOf(
              getMovement(active = false)
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
        service.transferViaCourt("A1234AK", requestCourtDifferentPrison)
      }
    }
  }
}
