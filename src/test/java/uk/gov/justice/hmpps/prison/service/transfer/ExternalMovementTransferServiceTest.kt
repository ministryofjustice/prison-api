package uk.gov.justice.hmpps.prison.service.transfer

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
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
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
import javax.persistence.EntityManager

internal class ExternalMovementTransferServiceTest {
  private val movementReasonRepository: ReferenceCodeRepository<MovementReason> = mock()
  private val externalMovementRepository: ExternalMovementRepository = mock()
  private val movementTypeRepository: ReferenceCodeRepository<MovementType> = mock()
  private val entityManager: EntityManager = mock()

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

  private val service = ExternalMovementTransferService(
    movementReasonRepository = movementReasonRepository,
    externalMovementRepository = externalMovementRepository,
    movementTypeRepository = movementTypeRepository,
    entityManager = entityManager
  )

  @DisplayName("updateMovementsForTransfer")
  @Nested
  inner class UpdateMovementsForTransfer {
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
            movementReason
          )
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovement
          )
        )
        booking = OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovement); bookingId = 99 }
      }

      @Test
      internal fun `should make any active movements inactive and write to database`() {
        assertThat(booking.externalMovements.first().isActive).isTrue
        service.updateMovementsForTransfer(request, booking, bookingLastMovement)
        assertThat(booking.externalMovements.first().isActive).isFalse

        verify(entityManager).flush()
      }

      @Test
      internal fun `should create a new transfer movement`() {
        assertThat(booking.externalMovements).hasSize(1)
        service.updateMovementsForTransfer(request, booking, bookingLastMovement)
        assertThat(booking.externalMovements).hasSize(2)
      }

      @Test
      internal fun `new movement should specify the movement from and to prisons`() {
        val movement = service.updateMovementsForTransfer(request, booking, bookingLastMovement)
        assertThat(movement.toAgency).isEqualTo(toPrison)
        assertThat(movement.fromAgency).isEqualTo(fromPrison)
      }

      @Test
      internal fun `new movement will contain reason and comment`() {
        val movement = service.updateMovementsForTransfer(request, booking, bookingLastMovement)
        assertThat(movement.commentText).isEqualTo("😩")
        assertThat(movement.movementReason).isEqualTo(movementReason)
      }

      @Test
      internal fun `new movement will contain movement time from receive time`() {
        val movement = service.updateMovementsForTransfer(request, booking, bookingLastMovement)
        assertThat(movement.movementDate).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00").toLocalDate())
        assertThat(movement.movementTime).isEqualTo(LocalDateTime.parse("2022-04-20T10:00:00"))
      }

      @Test
      internal fun `new movement will contain now when no receive time`() {
        val movement = service.updateMovementsForTransfer(
          RequestToTransferIn().apply {
            commentText = "😩"
          },
          booking, bookingLastMovement
        )
        assertThat(movement.movementDate).isEqualTo(LocalDate.now())
        assertThat(movement.movementTime).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS))
      }

      @Test
      internal fun `new movement will be an IN direction`() {
        val movement = service.updateMovementsForTransfer(request, booking, bookingLastMovement)
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
            movementReason
          )
        )
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingIdAndActive(99, true)).thenReturn(
          listOf(
            bookingLastMovement
          )
        )
        booking = OffenderBooking().apply { externalMovements = mutableListOf(bookingLastMovement); bookingId = 99 }
      }

      @Test
      internal fun `will throw exception if cannot find movement reason`() {
        whenever(movementReasonRepository.findById(MovementReason.pk("INT"))).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransfer(request, booking, bookingLastMovement)
        }
      }

      @Test
      internal fun `will throw exception if cannot find movement type`() {
        whenever(movementTypeRepository.findById(MovementType.ADM)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
          service.updateMovementsForTransfer(request, booking, bookingLastMovement)
        }
      }

      @Test
      internal fun `will throw exception if receive time is in the future`() {
        assertThatThrownBy {
          service.updateMovementsForTransfer(
            RequestToTransferIn().apply {
              receiveTime = LocalDateTime.now().plusHours(1); commentText = "😩"
            },
            booking, bookingLastMovement
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Transfer cannot be done in the future")
      }

      @Test
      internal fun `will throw exception if receive time before previous movement`() {
        assertThatThrownBy {
          service.updateMovementsForTransfer(
            RequestToTransferIn().apply {
              receiveTime = bookingLastMovement.movementTime.minusHours(1)
            },
            booking, bookingLastMovement
          )
        }
          .isInstanceOf(BadRequestException::class.java)
          .hasMessage("Movement cannot be before the previous active movement")
      }
    }
  }
}
