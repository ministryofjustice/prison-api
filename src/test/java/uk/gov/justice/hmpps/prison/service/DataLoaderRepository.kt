package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.InstitutionArea
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.TeamCategory
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourseActivityRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalServiceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIndividualScheduleRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNoPayPeriodRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPayStatusRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTeamAssignmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ServiceAgencySwitchesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.TeamRepository
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.TeamBuilder
import uk.gov.justice.hmpps.prison.util.builders.TestDataContext

@Service
class DataLoaderRepository(
  val courseActivityRepository: CourseActivityRepository,
  val agencyLocationRepository: AgencyLocationRepository,
  val bookingRepository: BookingRepository,
  val offenderBookingRepository: OffenderBookingRepository,
  val offenderProgramProfileRepository: OffenderProgramProfileRepository,
  val offenderCourtCaseRepository: OffenderCourtCaseRepository,
  val scheduleRepository: OffenderIndividualScheduleRepository,
  var eventStatusRepository: ReferenceCodeRepository<EventStatus>,
  var escortAgencyTypeRepository: ReferenceCodeRepository<EscortAgencyType>,
  val teamRepository: TeamRepository,
  val offenderTeamAssignmentRepository: OffenderTeamAssignmentRepository,
  val legalCourtCaseTypeRepository: ReferenceCodeRepository<LegalCaseType>,
  val courtCaseStatusRepository: ReferenceCodeRepository<CaseStatus>,
  val institutionAreaRepository: ReferenceCodeRepository<InstitutionArea>,
  val teamCategoryRepository: ReferenceCodeRepository<TeamCategory>,
  val externalMovementRepository: ExternalMovementRepository,
  val bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository,
  val courtEventRepository: CourtEventRepository,
  val offenderRepository: OffenderRepository,
  val offenderSentenceAdjustmentRepository: OffenderSentenceAdjustmentRepository,
  val offenderKeyDateAdjustmentRepository: OffenderKeyDateAdjustmentRepository,
  val offenderPayStatusRepository: OffenderPayStatusRepository,
  val externalServiceRepository: ExternalServiceRepository,
  val serviceAgencySwitchesRepository: ServiceAgencySwitchesRepository,
  val offenderNoPayPeriodRepository: OffenderNoPayPeriodRepository,
  val jdbcTemplate: JdbcTemplate,
)

@Service
class DataLoaderTransaction {
  @Transactional
  fun load(
    offenderBuilder: OffenderBuilder,
    testDataContext: TestDataContext,
  ) =
    offenderBuilder.save(testDataContext)

  @Transactional
  fun load(
    teamBuilder: TeamBuilder,
    testDataContext: TestDataContext,
  ) =
    teamBuilder.save(
      dataLoader = testDataContext.dataLoader,
    )

  @Transactional
  fun <T> save(operation: () -> T) = operation()

  @Transactional
  fun <T> get(operation: () -> T) = operation()

  @Transactional
  fun <T> transaction(operation: () -> T) = operation()
}
