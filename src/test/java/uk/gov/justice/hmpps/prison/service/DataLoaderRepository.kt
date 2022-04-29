package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourseActivityRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import uk.gov.justice.hmpps.prison.util.OffenderBuilder
import javax.transaction.Transactional

@Service
class DataLoaderRepository(
  val courseActivityRepository: CourseActivityRepository,
  val agencyLocationRepository: AgencyLocationRepository,
  val bookingRepository: BookingRepository,
  val offenderBookingRepository: OffenderBookingRepository,
  val offenderProgramProfileRepository: OffenderProgramProfileRepository,
  val offenderCourtCaseRepository: OffenderCourtCaseRepository,
  val legalCourtCaseTypeRepository: ReferenceCodeRepository<LegalCaseType>,
  val courtCaseStatusRepository: ReferenceCodeRepository<CaseStatus>,
) {
  @Transactional
  fun save(
    offenderBuilder: OffenderBuilder,
    webTestClient: WebTestClient,
    jwtAuthenticationHelper: JwtAuthenticationHelper
  ) =
    offenderBuilder.save(
      webTestClient = webTestClient,
      jwtAuthenticationHelper = jwtAuthenticationHelper,
      dataLoader = this
    )
}
