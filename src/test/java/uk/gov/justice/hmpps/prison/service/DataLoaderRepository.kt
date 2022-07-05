package uk.gov.justice.hmpps.prison.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.InstitutionArea
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.TeamCategory
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourseActivityRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTeamAssignmentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.TeamRepository
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.TeamBuilder
import uk.gov.justice.hmpps.prison.util.builders.TestDataContext
import javax.transaction.Transactional

@Service
class DataLoaderRepository(
  val courseActivityRepository: CourseActivityRepository,
  val agencyLocationRepository: AgencyLocationRepository,
  val bookingRepository: BookingRepository,
  val offenderBookingRepository: OffenderBookingRepository,
  val offenderProgramProfileRepository: OffenderProgramProfileRepository,
  val offenderCourtCaseRepository: OffenderCourtCaseRepository,
  val teamRepository: TeamRepository,
  val offenderTeamAssignmentRepository: OffenderTeamAssignmentRepository,
  val legalCourtCaseTypeRepository: ReferenceCodeRepository<LegalCaseType>,
  val courtCaseStatusRepository: ReferenceCodeRepository<CaseStatus>,
  val institutionAreaRepository: ReferenceCodeRepository<InstitutionArea>,
  val teamCategoryRepository: ReferenceCodeRepository<TeamCategory>,
  val externalMovementRepository: ExternalMovementRepository,
  val bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository,
  val courtEventRepository: CourtEventRepository,
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
      dataLoader = testDataContext.dataLoader
    )
}
