@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.service

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.RequestToDischargePrisoner
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ImprisonmentStatusRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MovementTypeAndReasonRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNoPayPeriodRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPayStatusRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.enteringandleaving.BookingIntoPrisonService
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.util.Optional

internal class PrisonerReleaseAndTransferServiceTest {
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val agencyLocationRepository: AgencyLocationRepository = mock()
  private val externalMovementRepository: ExternalMovementRepository = mock()
  private val movementTypeRepository: ReferenceCodeRepository<MovementType> = mock()
  private val agencyLocationTypeRepository: ReferenceCodeRepository<AgencyLocationType> = mock()
  private val movementReasonRepository: ReferenceCodeRepository<MovementReason> = mock()
  private val bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository = mock()
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository = mock()
  private val movementTypeAndReasonRepository: MovementTypeAndReasonRepository = mock()
  private val offenderSentenceAdjustmentRepository: OffenderSentenceAdjustmentRepository = mock()
  private val offenderKeyDateAdjustmentRepository: OffenderKeyDateAdjustmentRepository = mock()
  private val caseNoteRepository: OffenderCaseNoteRepository = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val offenderNoPayPeriodRepository: OffenderNoPayPeriodRepository = mock()
  private val offenderPayStatusRepository: OffenderPayStatusRepository = mock()
  private val imprisonmentStatusRepository: ImprisonmentStatusRepository = mock()
  private val caseNoteTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteType> = mock()
  private val caseNoteSubTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteSubType> = mock()
  private val cityReferenceCodeRepository: ReferenceCodeRepository<City> = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()

  private val offenderTransformer: OffenderTransformer = mock()
  private val offenderProgramProfileRepository: OffenderProgramProfileRepository = mock()
  private val entityManager: EntityManager = mock()
  private val courtEventRepository: CourtEventRepository = mock()
  private val offenderIndividualScheduleRepository: OffenderIndividualScheduleRepository = mock()
  private val eventStatusRepository: ReferenceCodeRepository<EventStatus> = mock()
  private val bookingIntoPrisonService: BookingIntoPrisonService = mock()
  private val serviceAgencySwitchesService: ServiceAgencySwitchesService = mock()

  private val service: PrisonerReleaseAndTransferService = PrisonerReleaseAndTransferService(
    offenderBookingRepository,
    offenderRepository,
    agencyLocationRepository,
    externalMovementRepository,
    movementTypeRepository,
    agencyLocationTypeRepository,
    movementReasonRepository,
    bedAssignmentHistoriesRepository,
    agencyInternalLocationRepository,
    movementTypeAndReasonRepository,
    offenderSentenceAdjustmentRepository,
    offenderKeyDateAdjustmentRepository,
    caseNoteRepository,
    authenticationFacade,
    offenderNoPayPeriodRepository,
    offenderPayStatusRepository,
    imprisonmentStatusRepository,
    caseNoteTypeReferenceCodeRepository,
    caseNoteSubTypeReferenceCodeRepository,
    cityReferenceCodeRepository,
    staffUserAccountRepository,
    offenderTransformer,
    offenderProgramProfileRepository,
    entityManager,
    courtEventRepository,
    offenderIndividualScheduleRepository,
    eventStatusRepository,
    bookingIntoPrisonService,
    serviceAgencySwitchesService,
  )

  @Nested
  inner class dischargeToHospital {
    val movement = ExternalMovement().also { m -> m.movementType = MovementType().also { t -> t.code = "REL" } }
    val offenderBooking = OffenderBooking().also { it.externalMovements = listOf(movement) }

    @BeforeEach
    internal fun before() {
      val prisoner = Offender().also { it.bookings.add(OffenderBooking()) }
      whenever(offenderRepository.findOffenderByNomsId(any())).thenReturn(Optional.of(prisoner))

      val hospital = AgencyLocation().also { l ->
        l.type = AgencyLocationType().also { t -> t.code = "HSHOSP" }
        l.description = "Some Hospital"
      }
      whenever(agencyLocationRepository.findById(any())).thenReturn(Optional.of(hospital))

      whenever(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(any(), any())).thenReturn(
        Optional.of(offenderBooking),
      )

      whenever(movementReasonRepository.findById(any())).thenReturn(Optional.of(MovementReason()))
    }

    @Test
    internal fun `existing booking with last movement as release - no comment`() {
      service.dischargeToHospital(
        "A2345C",
        RequestToDischargePrisoner(
          "BMARSH",
          null,
          "comment text",
          "MDI",
          "MDI",
        ),
      )

      assertThat(movement.commentText).isEqualTo("Psychiatric Hospital Discharge to Some Hospital")
    }

    @Test
    internal fun `existing booking with last movement as release - blank comment`() {
      movement.commentText = "       "

      service.dischargeToHospital(
        "A2345C",
        RequestToDischargePrisoner(
          "BMARSH",
          null,
          "comment text",
          "MDI",
          "MDI",
        ),
      )

      assertThat(movement.commentText).isEqualTo("Psychiatric Hospital Discharge to Some Hospital")
    }

    @Test
    internal fun `existing booking with last movement as release - small comment`() {
      movement.commentText = "Some comment"
      service.dischargeToHospital(
        "A2345C",
        RequestToDischargePrisoner(
          "BMARSH",
          null,
          "comment text",
          "MDI",
          "MDI",
        ),
      )

      assertThat(movement.commentText).isEqualTo("Some comment. Psychiatric Hospital Discharge to Some Hospital")
    }

    @Test
    internal fun `existing booking with last movement as release - long comment`() {
      movement.commentText = "Some comment".repeat(19)
      service.dischargeToHospital(
        "A2345C",
        RequestToDischargePrisoner(
          "BMARSH",
          null,
          "comment text",
          "MDI",
          "MDI",
        ),
      )

      // we trim at 240 chars - 12 x 19 = 238 so gets trimmed
      assertThat(movement.commentText).isEqualTo("${"Some comment".repeat(19)}. Psychiatri")
    }
  }
}
