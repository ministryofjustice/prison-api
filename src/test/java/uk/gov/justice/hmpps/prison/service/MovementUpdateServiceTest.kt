package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory.BedAssignmentHistoryPK
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional

internal class MovementUpdateServiceTest {
  private val referenceDomainService: ReferenceDomainService = mock()
  private val bedAssignmentHistoryService: BedAssignmentHistoryService = mock()
  private val bookingService: BookingService = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository = mock()
  private val service = MovementUpdateService(
    referenceDomainService,
    bedAssignmentHistoryService,
    bookingService,
    offenderBookingRepository,
    agencyInternalLocationRepository,
    clock,
  )

  @BeforeEach
  fun before() {
    whenever(
      bedAssignmentHistoryService.add(
        anyLong(),
        anyLong(),
        anyString(),
        any(),
      ),
    ).thenReturn(BedAssignmentHistoryPK(1L, 2))
  }

  @Nested
  internal inner class MoveToCellError {
    @Test
    fun reasonCodeEmpty_throwsIllegalArgument() {
      assertThatThrownBy {
        service.moveToCellOrReception(
          SOME_BOOKING_ID,
          NEW_LIVING_UNIT_DESC,
          "",
          SOME_TIME,
          false,
        )
      }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Reason code")
    }

    @Test
    fun dateTimeInFuture_throwsIllegalArgument() {
      val theFuture = LocalDateTime.now(Clock.offset(clock, Duration.ofDays(1L)))
      assertThatThrownBy {
        service.moveToCellOrReception(
          SOME_BOOKING_ID,
          NEW_LIVING_UNIT_DESC,
          SOME_REASON_CODE,
          theFuture,
          false,
        )
      }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("date")
        .hasMessageContaining("future")
    }

    @Test
    fun reasonCodeNotFound_throwsIllegalArgument() {
      val badReasonCode = "not_a_reason_code"
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          ReferenceDomain.CELL_MOVE_REASON.domain,
          badReasonCode,
          false,
        ),
      )
        .thenThrow(
          EntityNotFoundException.withMessage(
            "Reference code for domain [%s] and code [%s] not found.",
            ReferenceDomain.CELL_MOVE_REASON,
            badReasonCode,
          ),
        )

      assertThatThrownBy {
        service.moveToCellOrReception(
          SOME_BOOKING_ID,
          NEW_LIVING_UNIT_DESC,
          badReasonCode,
          SOME_TIME,
          false,
        )
      }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining(ReferenceDomain.CELL_MOVE_REASON.name)
        .hasMessageContaining(badReasonCode)
    }

    @Test
    fun bookingIdNotFound_throwsNotFound() {
      val badBookingId: Long = SOME_BOOKING_ID
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(Optional.empty<OffenderBooking>())

      assertThatThrownBy {
        service.moveToCellOrReception(
          badBookingId,
          NEW_LIVING_UNIT_DESC,
          SOME_REASON_CODE,
          SOME_TIME,
          false,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessageContaining(String.format(" %d ", badBookingId))
        .hasMessageContaining("Booking id")
        .hasMessageContaining("not found")
    }

    @Test
    fun bookingNotActive_throwsNotFound() {
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(SOME_BOOKING_ID))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, false))

      assertThatThrownBy {
        service.moveToCellOrReception(
          SOME_BOOKING_ID,
          NEW_LIVING_UNIT_DESC,
          SOME_REASON_CODE,
          SOME_TIME,
          false,
        )
      }
        .hasMessage(String.format("Offender booking with id %s is not active.", SOME_BOOKING_ID))
    }

    @Test
    fun exceptionFromOffenderBookingRepository_propagates() {
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenThrow(RuntimeException("Fake runtime exception"))

      assertThatThrownBy {
        service.moveToCellOrReception(
          SOME_BOOKING_ID,
          NEW_LIVING_UNIT_DESC,
          SOME_REASON_CODE,
          SOME_TIME,
          false,
        )
      }
        .isInstanceOf(RuntimeException::class.java)
        .hasMessage("Fake runtime exception")
    }

    @Test
    fun noCapacity_throwsException() {
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, true))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC, true))
      whenever(
        agencyInternalLocationRepository.findOneByDescription(
          NEW_LIVING_UNIT_DESC,
        ),
      )
        .thenReturn(
          Optional.of(
            AgencyInternalLocation.builder()
              .locationId(NEW_LIVING_UNIT_ID)
              .locationCode(NEW_LIVING_UNIT_DESC)
              .description("MDI-1-3")
              .operationalCapacity(10)
              .capacity(10)
              .locationType("CELL")
              .active(true)
              .build(),
          ),
        )

      assertThatThrownBy {
        service.moveToCellOrReception(
          SOME_BOOKING_ID,
          NEW_LIVING_UNIT_DESC,
          SOME_REASON_CODE,
          SOME_TIME,
          false,
        )
      }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("Location MDI-1-3 is either not a cell or reception, active or is at maximum capacity")
    }
  }

  @Nested
  internal inner class MoveToCellSuccess {
    @Test
    fun updatesBooking() {
      mockSuccess()

      service.moveToCellOrReception(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME, false)

      verify(bookingService).updateLivingUnit(
        SOME_BOOKING_ID,
        aLocation(
          NEW_LIVING_UNIT_ID,
          NEW_LIVING_UNIT_DESC,
        ).get(),
        false,
      )
    }

    @Test
    fun updatesBookingReception() {
      mockSuccessForReception()

      service.moveToCellOrReception(SOME_BOOKING_ID, NEW_RECEPTION_DESC, SOME_REASON_CODE, SOME_TIME, false)

      verify(bookingService).updateLivingUnit(
        SOME_BOOKING_ID,
        receptionLocation(
          NEW_LIVING_UNIT_ID,
          NEW_RECEPTION_DESC,
        ).get(),
        false,
      )
    }

    @Test
    fun updatesBookingCellSwap() {
      mockSuccessForCellSwap()

      service.moveToCellOrReception(SOME_BOOKING_ID, NEW_CELL_SWAP_DESC, SOME_REASON_CODE, SOME_TIME, false)

      verify(bookingService).updateLivingUnit(
        SOME_BOOKING_ID,
        cellSwapDestination().get(),
        false,
      )
      verify(bedAssignmentHistoryService)
        .add(SOME_BOOKING_ID, CELL_SWAP_LOCATION_ID, SOME_REASON_CODE, SOME_TIME)
    }

    @Test
    fun cellSwapAtCapacity_stillMoves() {
      // Cell swap is deliberately uncapped. The gate short-circuits on isCellSwap() before reaching
      // hasSpace(), so a CSWAP location recorded as full must still accept the move.
      mockSuccessForCellSwap(fullCellSwapDestination())

      service.moveToCellOrReception(SOME_BOOKING_ID, NEW_CELL_SWAP_DESC, SOME_REASON_CODE, SOME_TIME, false)

      verify(bookingService).updateLivingUnit(
        SOME_BOOKING_ID,
        fullCellSwapDestination().get(),
        false,
      )
    }

    @Test
    fun writesToBedAssignmentHistories() {
      mockSuccess()

      service.moveToCellOrReception(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME, false)

      verify(bedAssignmentHistoryService)
        .add(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME)
    }

    @Test
    fun missingDateTime_defaultsToNow() {
      mockSuccess()

      service.moveToCellOrReception(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, null, false)

      verify(bedAssignmentHistoryService).add(
        SOME_BOOKING_ID,
        NEW_LIVING_UNIT_ID,
        SOME_REASON_CODE,
        LocalDateTime.now(
          clock,
        ),
      )
    }

    @Test
    fun returnsUpdatedOffenderBooking() {
      mockSuccess()

      val offenderBooking =
        service.moveToCellOrReception(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME, false)

      assertThat(offenderBooking.assignedLivingUnitId).isEqualTo(NEW_LIVING_UNIT_ID)
    }

    @Test
    fun cellNotChanged_doesntTriggerUpdates() {
      mockCellNotChanged()

      service.moveToCellOrReception(SOME_BOOKING_ID, OLD_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME, false)

      verify(bookingService, Mockito.never()).updateLivingUnit(
        any(),
        any(),
        eq(false),
      )
      verify(bedAssignmentHistoryService, Mockito.never()).add(
        SOME_BOOKING_ID,
        OLD_LIVING_UNIT_ID,
        SOME_REASON_CODE,
        SOME_TIME,
      )
    }

    @Test
    fun cellNotChanged_returnsExistingBooking() {
      mockCellNotChanged()

      val offenderSummary =
        service.moveToCellOrReception(SOME_BOOKING_ID, OLD_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME, false)

      assertThat(offenderSummary.assignedLivingUnitId).isEqualTo(OLD_LIVING_UNIT_ID)
      verify(offenderBookingRepository, Mockito.times(1)).findById(
        SOME_BOOKING_ID,
      )
    }

    private fun mockSuccess() {
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, true))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC, true))
      whenever(
        agencyInternalLocationRepository.findOneByDescription(
          NEW_LIVING_UNIT_DESC,
        ),
      )
        .thenReturn(aLocation(NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC))
      whenever(
        agencyInternalLocationRepository.findOneByDescription(
          NEW_RECEPTION_DESC,
        ),
      )
        .thenReturn(aLocation(NEW_LIVING_UNIT_ID, NEW_RECEPTION_DESC))
    }

    private fun mockSuccessForReception() {
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, true))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC, true))
      whenever(
        agencyInternalLocationRepository.findOneByDescription(
          NEW_RECEPTION_DESC,
        ),
      )
        .thenReturn(receptionLocation(NEW_LIVING_UNIT_ID, RECEPTION_CODE))
      whenever(
        agencyInternalLocationRepository.findOneByDescription(
          NEW_RECEPTION_DESC,
        ),
      )
        .thenReturn(receptionLocation(NEW_LIVING_UNIT_ID, RECEPTION_CODE))
    }

    private fun mockSuccessForCellSwap(destination: Optional<AgencyInternalLocation> = cellSwapDestination()) {
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(anyLong()))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, true))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, CELL_SWAP_LOCATION_ID, NEW_CELL_SWAP_DESC, true))
      whenever(
        agencyInternalLocationRepository.findOneByDescription(
          NEW_CELL_SWAP_DESC,
        ),
      )
        .thenReturn(destination)
    }

    private fun mockCellNotChanged() {
      whenever(
        referenceDomainService.getReferenceCodeByDomainAndCode(
          anyString(),
          anyString(),
          eq(false),
        ),
      )
        .thenReturn(Optional.of(mock(ReferenceCode::class.java)))
      whenever(offenderBookingRepository.findById(SOME_BOOKING_ID))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, true))
      whenever(
        agencyInternalLocationRepository.findOneByDescription(
          OLD_LIVING_UNIT_DESC,
        ),
      )
        .thenReturn(aLocation(OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC))
    }
  }

  private fun anOffenderBooking(
    bookingId: Long,
    agency: String?,
    livingUnitId: Long?,
    livingUnitDesc: String?,
    active: Boolean,
  ): Optional<OffenderBooking> {
    val livingUnit = AgencyInternalLocation.builder().locationId(livingUnitId).description(livingUnitDesc).build()
    return Optional.of(
      OffenderBooking.builder()
        .active(active)
        .bookingId(bookingId)
        .location(AgencyLocation.builder().id(agency).build())
        .assignedLivingUnit(livingUnit)
        .build(),
    )
  }

  /**
   * A prison's cell swap location as it really is in NOMIS: uncertified, with no parent and location
   * code CSWAP - so it satisfies isCellSwap() but neither isActiveCellWithSpace() nor
   * isActiveReceptionWithSpace(). The locationType below is only one of the values the live estate
   * uses - these locations are recorded as WING, HOLD and AREA - and isCellSwap() ignores it, so do
   * not read this as the type a cell swap location must have.
   */
  private fun cellSwapDestination(): Optional<AgencyInternalLocation> = Optional.of(
    AgencyInternalLocation.builder()
      .locationId(CELL_SWAP_LOCATION_ID)
      .locationCode(CELL_SWAP_LOCATION_CODE)
      .description(NEW_CELL_SWAP_DESC)
      .locationType("WING")
      .certifiedFlag(false)
      .active(true)
      .build(),
  )

  private fun fullCellSwapDestination(): Optional<AgencyInternalLocation> = Optional.of(
    AgencyInternalLocation.builder()
      .locationId(CELL_SWAP_LOCATION_ID)
      .locationCode(CELL_SWAP_LOCATION_CODE)
      .description(NEW_CELL_SWAP_DESC)
      .locationType("WING")
      .certifiedFlag(false)
      .active(true)
      .capacity(2)
      .currentOccupancy(2)
      .build(),
  )

  private fun aLocation(locationId: Long?, locationCode: String?): Optional<AgencyInternalLocation> = Optional.of(
    AgencyInternalLocation.builder()
      .locationId(locationId)
      .operationalCapacity(10)
      .currentOccupancy(1)
      .locationType("CELL")
      .locationCode(locationCode)
      .active(true)
      .build(),
  )

  private fun receptionLocation(locationId: Long?, locationCode: String?): Optional<AgencyInternalLocation> = Optional.of(
    AgencyInternalLocation.builder()
      .locationId(locationId)
      .operationalCapacity(null)
      .currentOccupancy(1)
      .locationType("AREA")
      .locationCode(locationCode)
      .userDescription(null)
      .certifiedFlag(false)
      .active(true)
      .capacity(100)
      .description("NMI-RECP")
      .livingUnit(null)
      .build(),
  )

  companion object {
    private const val SOME_BOOKING_ID = 1L
    private const val OLD_LIVING_UNIT_ID = 2L
    private const val OLD_LIVING_UNIT_DESC = "MDI-1-2"
    private const val NEW_LIVING_UNIT_ID = 3L
    private const val NEW_LIVING_UNIT_DESC = "MDI-1-3"
    private const val NEW_RECEPTION_DESC = "MDI-RECP"
    private const val RECEPTION_CODE = "RECP"
    private const val SOME_AGENCY_ID = "MDI"
    private const val SOME_REASON_CODE = "ADM"
    private const val CELL_SWAP_LOCATION_CODE = "CSWAP"

    // The bookings in these tests are at MDI, and moveToCellOrReception resolves the destination by
    // description, so the cell swap location has to be MDI's own.
    private const val NEW_CELL_SWAP_DESC = "MDI-CSWAP"
    private const val CELL_SWAP_LOCATION_ID = 123L

    private val clock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val SOME_TIME: LocalDateTime = LocalDateTime.now(clock)
  }
}
