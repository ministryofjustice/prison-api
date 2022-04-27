package uk.gov.justice.hmpps.prison.util

import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail.PK
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.WaitlistDecisionCode
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourseActivityRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import java.time.LocalDate

class OffenderProgramProfileBuilder(
  var prisonId: String = "MDI",
  var startDate: LocalDate = LocalDate.of(2016, 11, 9),
  var programStatus: String = "ALLOC",
  var waitListDecisionCode: ReferenceCode.Pk? = null,
  var courseActivityId: Long = -1,
  var offenderBookingId: Long = -1,
  var programId: Long = -1
) : WebClientEntityBuilder() {

  fun save(
    courseActivityRepository: CourseActivityRepository? = null,
    agencyLocationRepository: AgencyLocationRepository? = null,
    waitListDecisionCodeRepository: ReferenceCodeRepository<WaitlistDecisionCode>? = null,
    bookingRepository: OffenderBookingRepository? = null,
    offenderProgramProfileRepository: OffenderProgramProfileRepository? = null
  ): OffenderProgramProfile {

    val prison = agencyLocationRepository?.let {
      it.findByIdOrNull(prisonId) ?: throw BadRequestException("prison $prisonId not found")
    } ?: throw BadRequestException("No agency location repository provided")

    val offenderBooking = bookingRepository?.let {
      it.findByBookingId(offenderBookingId).orElseThrow(BadRequestException("booking $offenderBookingId not found"))
    } ?: throw BadRequestException("No offender booking repository provided")

    val courseActivity = courseActivityRepository?.let {
      it.findByIdOrNull(courseActivityId) ?: throw BadRequestException("prison $prisonId not found")
    } ?: throw BadRequestException("No course activity repository provided")

    val decisionCode = waitListDecisionCode?.let {
      waitListDecisionCodeRepository?.let {
        it.findByIdOrNull(waitListDecisionCode)
          ?: throw BadRequestException("decision code  $waitListDecisionCode not found")
      } ?: throw BadRequestException("No decision code repository provided")
    }

    val offenderProgramProfile =
      OffenderProgramProfile.builder().offenderBooking(offenderBooking).programStatus(programStatus)
        .programId(programId).agencyLocation(prison).courseActivity(courseActivity).startDate(startDate)
        .build()

    return offenderProgramProfileRepository?.save(offenderProgramProfile)
      ?: throw BadRequestException("No offender program profile repository provided")
  }
}
