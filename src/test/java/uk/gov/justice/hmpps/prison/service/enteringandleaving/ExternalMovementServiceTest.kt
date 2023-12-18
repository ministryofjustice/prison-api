package uk.gov.justice.hmpps.prison.service.enteringandleaving

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

internal class ExternalMovementServiceTest {
  private val movementReasonRepository: ReferenceCodeRepository<MovementReason> = mock()
  private val externalMovementRepository: ExternalMovementRepository = mock()
  private val movementTypeRepository: ReferenceCodeRepository<MovementType> = mock()
  private val entityManager: EntityManager = mock()

  private val fromPrison = AgencyLocation().apply { description = "HMPS Brixton"; id = "BXI" }
  private val toPrison = AgencyLocation().apply { description = "HMPS Wandsworth"; id = "WWI" }
  private val fromCourt =
    AgencyLocation().apply { description = "Court2"; id = "CB"; type = AgencyLocationType.COURT_TYPE }
  private val toCourt =
    AgencyLocation().apply { description = "Court1"; id = "CA"; type = AgencyLocationType.COURT_TYPE }
  private val out = AgencyLocation().apply { description = "Out"; id = "OUT" }
  private val toCorporateAddressId = 99L
  private val toHomeCity = City().apply { description = "Sheffield"; code = "SHEF" }

  private val bookingLastMovement = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toPrison
    movementType = MovementType().apply { code = "TRN"; description = "Transfer" }
    movementReason = MovementReason().apply { code = "TRN"; description = "Transfer" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
  }

  private val bookingLastMovementCourt = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toCourt
    movementType = MovementType().apply { code = "CRT"; description = "Court" }
    movementReason = MovementReason().apply { code = "CRT"; description = "Court" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
  }
  private val bookingLastMovementForTAP = ExternalMovement().apply {
    fromAgency = fromPrison
    toAgency = toCourt
    toCity = toHomeCity
    movementType = MovementType().apply { code = "TAP"; description = "Temporary Absence" }
    movementReason = MovementReason().apply { code = "C3"; description = "Funeral" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
    toAddressId = toCorporateAddressId
  }
  private val bookingLastMovementAdmission = ExternalMovement().apply {
    fromAgency = fromCourt
    toAgency = toPrison
    movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    movementReason = MovementReason().apply { code = "I"; description = "Imprisonment" }
    movementTime = LocalDateTime.parse("2022-04-19T00:00:00")
    movementDate = LocalDateTime.parse("2022-04-19T00:00:00").toLocalDate()
    isActive = true
  }

  private val service = ExternalMovementService(
    movementReasonRepository = movementReasonRepository,
    externalMovementRepository = externalMovementRepository,
    movementTypeRepository = movementTypeRepository,
    entityManager = entityManager,
  )

  @DisplayName("updateMovementsForTransferIn")
  @Nested
  inner class UpdateMovementsForTransferIn {
    val movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    val movementReason = MovementReason().apply { code = "INT"; description = "Transfer In from Other Establishment" }

    @Nested
    inner class Success {
      lateinit var booking: OffenderBooking
      private val request =
        RequestToTransferIn().apply { receiveTime = LocalDateTime.parse("2022-04-20T10:00:00"); commentText = "😩" }

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("INT"))).thenReturn(
          Optional.ofNullable(
            movementReason,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovement,
          ),
        )
        booking = OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovement); bookingId = 99 }
      }

      @Test
      internal fun `should make any active movements inactive and write to database`() {
        assertThat(booking.externalMovements.first().isActive).isTrue
        service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        assertThat(booking.externalMovements.first().isActive).isFalse

        verify(entityManager).flush()
      }

      @Test
      internal fun `should create a new transfer movement`() {
        assertThat(booking.externalMovements).hasSize(1)
        service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        assertThat(booking.externalMovements).hasSize(2)
      }

      @Test
      internal fun `new movement should specify the movement from and to prisons`() {
        val movement = service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        assertThat(movement.toAgency).isEqualTo(toPrison)
        assertThat(movement.fromAgency).isEqualTo(fromPrison)
      }

      @Test
      internal fun `new movement will contain reason and comment`() {
        val movement = service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        assertThat(movement.commentText).isEqualTo("😩")
        assertThat(movement.movementReason).isEqualTo(movementReason)
      }

      @Test
      internal fun `new movement will contain movement time from receive time`() {
        val movement = service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        assertThat(movement.movementDate).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00").toLocalDate())
        assertThat(movement.movementTime).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00"))
      }

      @Test
      internal fun `new movement will contain now when no receive time`() {
        val movement = service.updateMovementsForTransferIn(
          RequestToTransferIn().apply {
            commentText = "😩"
          },
          booking,
          bookingLastMovement,
        )
        assertThat(movement.movementDate).isEqualTo(LocalDate.now())
        assertThat(movement.movementTime).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `new movement will be an IN direction`() {
        val movement = service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        assertThat(movement.movementDirection).isEqualTo(MovementDirection.IN)
      }
    }

    @Nested
    inner class Exception {
      lateinit var booking: OffenderBooking
      private val request =
        RequestToTransferIn().apply { receiveTime = LocalDateTime.parse("2022-04-20T10:00:00"); commentText = "😩" }

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("INT"))).thenReturn(
          Optional.ofNullable(
            movementReason,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovement,
          ),
        )
        booking = OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovement); bookingId = 99 }
      }

      @Test
      internal fun `will throw exception if cannot find movement reason`() {
        whenever(movementReasonRepository.findById(MovementReason.pk("INT"))).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        }
      }

      @Test
      internal fun `will throw exception if cannot find movement type`() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransferIn(request, booking, bookingLastMovement)
        }
      }

      @Test
      internal fun `will throw exception if receive time is in the future`() {
        assertThatThrownBy {
          service.updateMovementsForTransferIn(
            RequestToTransferIn().apply {
              receiveTime = LocalDateTime.now().plusHours(1); commentText = "😩"
            },
            booking,
            bookingLastMovement,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be done in the future")
      }

      @Test
      internal fun `will throw exception if receive time before previous movement`() {
        assertThatThrownBy {
          service.updateMovementsForTransferIn(
            RequestToTransferIn().apply {
              receiveTime = bookingLastMovement.movementTime.minusHours(1)
            },
            booking,
            bookingLastMovement,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be before the previous active movement")
      }
    }
  }

  @DisplayName("updateMovementsForCourtTransferToSamePrison")
  @Nested
  inner class UpdateMovementsForCourtTransferToSamePrison {
    val movementType = MovementType().apply { code = "CRT"; description = "Court" }
    val movementReasonCourt = MovementReason().apply { code = "CRT"; description = "Transfer to court" }

    @Nested
    inner class Success {
      lateinit var booking: OffenderBooking
      private val request =
        RequestForCourtTransferIn().apply {
          agencyId = fromPrison.id; dateTime = LocalDateTime.parse("2022-04-20T10:00:00"); commentText = "😩"
        }

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.CRT)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("CRT"))).thenReturn(
          Optional.ofNullable(
            movementReasonCourt,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovementCourt,
          ),
        )
        booking =
          OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovementCourt); bookingId = 99 }
      }

      @Test
      internal fun `should make any active movements inactive and write to database`() {
        assertThat(booking.externalMovements.first().isActive).isTrue
        service.updateMovementsForCourtTransferToSamePrison(
          movementReasonCode = request.movementReasonCode,
          movementDateTime = request.dateTime,
          commentText = request.commentText,
          booking = booking,
          lastMovement = bookingLastMovementCourt,
          courtEvent = null,
        )
        assertThat(booking.externalMovements.first().isActive).isFalse

        verify(entityManager).flush()
      }

      @Test
      internal fun `should create a new transfer movement`() {
        assertThat(booking.externalMovements).hasSize(1)
        service.updateMovementsForCourtTransferToSamePrison(
          movementReasonCode = request.movementReasonCode,
          movementDateTime = request.dateTime,
          commentText = request.commentText,
          booking = booking,
          lastMovement = bookingLastMovementCourt,
          courtEvent = null,
        )
        assertThat(booking.externalMovements).hasSize(2)
      }

      @Test
      internal fun `new movement should specify the movement from court to prison`() {
        val movement =
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = request.movementReasonCode,
            movementDateTime = request.dateTime,
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        assertThat(movement.toAgency).isEqualTo(fromPrison)
        assertThat(movement.fromAgency).isEqualTo(toCourt)
      }

      @Test
      internal fun `new movement will contain reason and comment`() {
        val movement =
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = request.movementReasonCode,
            movementDateTime = request.dateTime,
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        assertThat(movement.commentText).isEqualTo("😩")
        assertThat(movement.movementReason).isEqualTo(movementReasonCourt)
      }

      @Test
      internal fun `new movement will contain movement time from receive time`() {
        val movement =
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = request.movementReasonCode,
            movementDateTime = request.dateTime,
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        assertThat(movement.movementDate).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00").toLocalDate())
        assertThat(movement.movementTime).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00"))
      }

      @Test
      internal fun `new movement will contain now when no receive time`() {
        val movement = service.updateMovementsForCourtTransferToSamePrison(
          movementReasonCode = null,
          movementDateTime = null,
          commentText = "😩",
          booking = booking,
          lastMovement = bookingLastMovementCourt,
          courtEvent = null,
        )
        assertThat(movement.movementDate).isEqualTo(LocalDate.now())
        assertThat(movement.movementTime).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `new movement will be an IN direction`() {
        val movement =
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = request.movementReasonCode,
            movementDateTime = request.dateTime,
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        assertThat(movement.movementDirection).isEqualTo(MovementDirection.IN)
      }
    }

    @Nested
    inner class Exception {
      lateinit var booking: OffenderBooking
      private val request =
        RequestForCourtTransferIn().apply { dateTime = LocalDateTime.parse("2022-04-20T10:00:00"); commentText = "😩" }

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("CRT"))).thenReturn(
          Optional.ofNullable(
            movementReasonCourt,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovement,
          ),
        )
        booking = OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovement); bookingId = 99 }
      }

      @Test
      internal fun `will throw exception if cannot find movement reason`() {
        whenever(movementReasonRepository.findById(MovementReason.pk("CRT"))).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = request.movementReasonCode,
            movementDateTime = request.dateTime,
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        }
      }

      @Test
      internal fun `will throw exception if cannot find movement type`() {
        whenever(movementTypeRepository.findById(MovementType.CRT)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = request.movementReasonCode,
            movementDateTime = request.dateTime,
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        }
      }

      @Test
      internal fun `will throw exception if receive time is in the future`() {
        assertThatThrownBy {
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = "CRT",
            movementDateTime = LocalDateTime.now().plusHours(1),
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be done in the future")
      }

      @Test
      internal fun `will throw exception if receive time before previous movement`() {
        assertThatThrownBy {
          service.updateMovementsForCourtTransferToSamePrison(
            movementReasonCode = null,
            movementDateTime = bookingLastMovement.movementTime.minusHours(1),
            commentText = request.commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            courtEvent = null,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be before the previous active movement")
      }
    }
  }

  @DisplayName("updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison")
  @Nested
  inner class UpdateMovementsForTransferInAfterTemporaryAbsenceToSamePrison {
    val movementType = MovementType().apply { code = "TAP"; description = "Temporary Absence" }
    val movementReasonFuneral = MovementReason().apply { code = "C3"; description = "Funeral" }
    val agencyId: String = fromPrison.id
    val dateTime: LocalDateTime = LocalDateTime.parse("2022-04-20T10:00:00")
    val commentText = "😩"
    val movementReasonCode = "C3"

    @Nested
    inner class Success {
      lateinit var booking: OffenderBooking

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.TAP)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("C3"))).thenReturn(
          Optional.ofNullable(
            movementReasonFuneral,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovementForTAP,
          ),
        )
        booking =
          OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovementForTAP); bookingId = 99 }
      }

      @Test
      internal fun `should make any active movements inactive and write to database`() {
        assertThat(booking.externalMovements.first().isActive).isTrue
        service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
          movementReasonCode = movementReasonCode,
          movementDateTime = dateTime,
          commentText = commentText,
          booking = booking,
          lastMovement = bookingLastMovementForTAP,
          scheduleEvent = null,
        )
        assertThat(booking.externalMovements.first().isActive).isFalse

        verify(entityManager).flush()
      }

      @Test
      internal fun `should create a new transfer movement`() {
        assertThat(booking.externalMovements).hasSize(1)
        service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
          movementReasonCode = movementReasonCode,
          movementDateTime = dateTime,
          commentText = commentText,
          booking = booking,
          lastMovement = bookingLastMovementForTAP,
          scheduleEvent = null,
        )
        assertThat(booking.externalMovements).hasSize(2)
      }

      @Test
      internal fun `new movement should specify the movement from TAP visited place to prison`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = dateTime,
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            scheduleEvent = null,
          )
        assertThat(movement.toAgency).isEqualTo(fromPrison)
        assertThat(movement.fromAddressId).isEqualTo(bookingLastMovementForTAP.toAddressId)
        // in reality either addressId or City would be set but for this test the last movement has both set
        assertThat(movement.fromCity).isEqualTo(bookingLastMovementForTAP.toCity)
      }

      @Test
      internal fun `new movement will contain reason and comment`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = dateTime,
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            scheduleEvent = null,
          )
        assertThat(movement.commentText).isEqualTo("😩")
        assertThat(movement.movementReason).isEqualTo(movementReasonFuneral)
      }

      @Test
      internal fun `new movement will contain movement time from receive time`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = dateTime,
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            scheduleEvent = null,
          )
        assertThat(movement.movementDate).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00").toLocalDate())
        assertThat(movement.movementTime).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00"))
      }

      @Test
      internal fun `new movement will contain now when no receive time`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = null,
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            scheduleEvent = null,
          )
        assertThat(movement.movementDate).isEqualTo(LocalDate.now())
        assertThat(movement.movementTime).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `new movement will be an IN direction`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = dateTime,
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            scheduleEvent = null,
          )
        assertThat(movement.movementDirection).isEqualTo(MovementDirection.IN)
      }
    }

    @Nested
    inner class Exception {
      lateinit var booking: OffenderBooking
      val agencyId: String = fromPrison.id
      val dateTime: LocalDateTime = LocalDateTime.parse("2022-04-20T10:00:00")
      val commentText = "😩"
      val movementReasonCode = "C3"

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("C3"))).thenReturn(
          Optional.ofNullable(
            movementReasonFuneral,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovement,
          ),
        )
        booking = OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovement); bookingId = 99 }
      }

      @Test
      internal fun `will throw exception if cannot find movement reason`() {
        whenever(movementReasonRepository.findById(MovementReason.pk("C3"))).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = "C3",
            movementDateTime = dateTime,
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            scheduleEvent = null,
          )
        }
      }

      @Test
      internal fun `will throw exception if cannot find movement type`() {
        whenever(movementTypeRepository.findById(MovementType.TAP)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = dateTime,
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            scheduleEvent = null,
          )
        }
      }

      @Test
      internal fun `will throw exception if receive time is in the future`() {
        assertThatThrownBy {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = LocalDateTime.now().plusHours(1),
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            scheduleEvent = null,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be done in the future")
      }

      @Test
      internal fun `will throw exception if receive time before previous movement`() {
        assertThatThrownBy {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToSamePrison(
            movementReasonCode = movementReasonCode,
            movementDateTime = bookingLastMovement.movementTime.minusHours(1),
            commentText = commentText,
            booking = booking,
            lastMovement = bookingLastMovementCourt,
            scheduleEvent = null,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be before the previous active movement")
      }
    }
  }

  @DisplayName("updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison")
  @Nested
  inner class UpdateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison {
    val movementType = MovementType().apply { code = "ADM"; description = "Admission" }
    val movementReasonTransferViaTAP = MovementReason().apply { code = "TRNTAP"; description = "Transfer via TAP" }
    val agencyId: String = fromPrison.id
    val dateTime: LocalDateTime = LocalDateTime.parse("2022-04-20T10:00:00")
    val commentText = "😩"
    val movementReasonCode = "C3"

    @Nested
    inner class Success {
      lateinit var booking: OffenderBooking

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("TRNTAP"))).thenReturn(
          Optional.ofNullable(
            movementReasonTransferViaTAP,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovementForTAP,
          ),
        )
        booking =
          OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovementForTAP); bookingId = 99 }
      }

      @Test
      internal fun `should make any active movements inactive and write to database`() {
        assertThat(booking.externalMovements.first().isActive).isTrue
        service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
          movementDateTime = dateTime,
          booking = booking,
          lastMovement = bookingLastMovementForTAP,
          toAgency = toPrison,
          commentText = commentText,
        )
        assertThat(booking.externalMovements.first().isActive).isFalse

        verify(entityManager).flush()
      }

      @Test
      internal fun `should create a new transfer movement`() {
        assertThat(booking.externalMovements).hasSize(1)
        service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
          movementDateTime = dateTime,
          booking = booking,
          lastMovement = bookingLastMovementForTAP,
          toAgency = toPrison,
          commentText = commentText,
        )
        assertThat(booking.externalMovements).hasSize(2)
      }

      @Test
      internal fun `new movement should only specify the prison they originated from`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = dateTime,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        assertThat(movement.toAgency).isEqualTo(toPrison)
        assertThat(movement.fromAddressId).isNull()
        assertThat(movement.fromCity).isNull()
      }

      @Test
      internal fun `new movement will contain reason and comment`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = dateTime,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        assertThat(movement.commentText).isEqualTo("😩")
        assertThat(movement.movementReason).isEqualTo(movementReasonTransferViaTAP)
      }

      @Test
      internal fun `new movement will contain movement time from receive time`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = dateTime,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        assertThat(movement.movementDate).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00").toLocalDate())
        assertThat(movement.movementTime).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00"))
      }

      @Test
      internal fun `new movement will contain now when no receive time`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = null,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        assertThat(movement.movementDate).isEqualTo(LocalDate.now())
        assertThat(movement.movementTime).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `new movement will be an IN direction`() {
        val movement =
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = dateTime,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        assertThat(movement.movementDirection).isEqualTo(MovementDirection.IN)
      }
    }

    @Nested
    inner class Exception {
      lateinit var booking: OffenderBooking
      val agencyId: String = fromPrison.id
      val dateTime: LocalDateTime = LocalDateTime.parse("2022-04-20T10:00:00")
      val commentText = "😩"

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("TRNTAP"))).thenReturn(
          Optional.ofNullable(
            movementReasonTransferViaTAP,
          ),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovement,
          ),
        )
        booking = OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovement); bookingId = 99 }
      }

      @Test
      internal fun `will throw exception if cannot find movement reason`() {
        whenever(movementReasonRepository.findById(MovementReason.pk("TRNTAP"))).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = dateTime,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        }
      }

      @Test
      internal fun `will throw exception if cannot find movement type`() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = dateTime,
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        }
      }

      @Test
      internal fun `will throw exception if receive time is in the future`() {
        assertThatThrownBy {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = LocalDateTime.now().plusHours(1),
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be done in the future")
      }

      @Test
      internal fun `will throw exception if receive time before previous movement`() {
        assertThatThrownBy {
          service.updateMovementsForTransferInAfterTemporaryAbsenceToDifferentPrison(
            movementDateTime = bookingLastMovement.movementTime.minusHours(1),
            booking = booking,
            lastMovement = bookingLastMovementForTAP,
            toAgency = toPrison,
            commentText = commentText,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be before the previous active movement")
      }
    }
  }

  @DisplayName("updateMovementsForRelease")
  @Nested
  inner class UpdateMovementsForRelease {
    val movementType = MovementType().apply { code = "REL"; description = "Release" }
    val movementReasonConditionalRelease = MovementReason().apply { code = "CR"; description = "Conditional Release" }
    val agencyId: String = fromPrison.id
    val dateTime: LocalDateTime = LocalDateTime.parse("2022-04-20T10:00:00")
    val commentText = "😩"
    val movementReasonCode = "I"

    @Nested
    inner class Success {
      lateinit var booking: OffenderBooking

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.REL)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("CR"))).thenReturn(
          Optional.ofNullable(movementReasonConditionalRelease),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(bookingLastMovementAdmission),
        )
        booking = OffenderBooking().apply {
          externalMovements = mutableListOf(bookingLastMovementAdmission)
          location = fromPrison
          bookingId = 99
        }
      }

      @Test
      internal fun `should make any active movements inactive and write to database`() {
        assertThat(booking.externalMovements.first().isActive).isTrue
        service.updateMovementsForRelease(
          releaseTime = dateTime,
          movementReasonCode = "CR",
          booking = booking,
          toAgency = out,
          commentText = commentText,
        )
        assertThat(booking.externalMovements.first().isActive).isFalse

        verify(entityManager).flush()
      }

      @Test
      internal fun `should create a new release movement`() {
        assertThat(booking.externalMovements).hasSize(1)
        service.updateMovementsForRelease(
          releaseTime = dateTime,
          movementReasonCode = "CR",
          booking = booking,
          toAgency = out,
          commentText = commentText,
        )
        assertThat(booking.externalMovements).hasSize(2)
      }

      @Test
      internal fun `new movement should specify the prison they originated from`() {
        val movement =
          service.updateMovementsForRelease(
            releaseTime = dateTime,
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        assertThat(movement.fromAgency).isEqualTo(fromPrison)
        assertThat(movement.toAgency.id).isEqualTo("OUT")
      }

      @Test
      internal fun `new movement will contain reason and comment`() {
        val movement =
          service.updateMovementsForRelease(
            releaseTime = dateTime,
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        assertThat(movement.commentText).isEqualTo("😩")
        assertThat(movement.movementReason).isEqualTo(movementReasonConditionalRelease)
      }

      @Test
      internal fun `new movement will contain movement time from receive time`() {
        val movement =
          service.updateMovementsForRelease(
            releaseTime = dateTime,
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        assertThat(movement.movementDate).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00").toLocalDate())
        assertThat(movement.movementTime).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00"))
      }

      @Test
      internal fun `new movement will contain now when no receive time`() {
        val movement =
          service.updateMovementsForRelease(
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        assertThat(movement.movementDate).isEqualTo(LocalDate.now())
        assertThat(movement.movementTime).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `new movement will be an OUT direction`() {
        val movement =
          service.updateMovementsForRelease(
            releaseTime = dateTime,
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        assertThat(movement.movementDirection).isEqualTo(MovementDirection.OUT)
      }
    }

    @Nested
    inner class Exception {
      lateinit var booking: OffenderBooking
      val agencyId: String = fromPrison.id
      val dateTime: LocalDateTime = LocalDateTime.parse("2022-04-20T10:00:00")
      val commentText = "😩"

      @BeforeEach
      internal fun setUp() {
        whenever(movementTypeRepository.findById(MovementType.REL)).thenReturn(Optional.ofNullable(movementType))
        whenever(movementReasonRepository.findById(MovementReason.pk("CR"))).thenReturn(
          Optional.ofNullable(movementReasonConditionalRelease),
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(bookingLastMovementAdmission),
        )
        booking = OffenderBooking().apply {
          externalMovements = mutableListOf(bookingLastMovementAdmission)
          location = fromPrison
          bookingId = 99
        }
      }

      @Test
      internal fun `will throw exception if cannot find movement reason`() {
        whenever(movementReasonRepository.findById(MovementReason.pk("UNKNOWN"))).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForRelease(
            releaseTime = dateTime,
            movementReasonCode = "UNKNOWN",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        }
      }

      @Test
      internal fun `will throw exception if cannot find movement type`() {
        whenever(movementTypeRepository.findById(MovementType.REL)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForRelease(
            releaseTime = dateTime,
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        }
      }

      @Test
      internal fun `will throw exception if receive time is in the future`() {
        assertThatThrownBy {
          service.updateMovementsForRelease(
            releaseTime = LocalDateTime.now().plusHours(1),
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be done in the future")
      }

      @Test
      internal fun `will throw exception if receive time before previous movement`() {
        assertThatThrownBy {
          service.updateMovementsForRelease(
            releaseTime = bookingLastMovementAdmission.movementTime.minusHours(1),
            movementReasonCode = "CR",
            booking = booking,
            toAgency = out,
            commentText = commentText,
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be before the previous active movement")
      }
    }
  }
}
