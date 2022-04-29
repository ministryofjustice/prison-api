package uk.gov.justice.hmpps.prison.util

import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import java.time.LocalDate

class OffenderProgramProfileBuilder(
  var startDate: LocalDate = LocalDate.of(2016, 11, 9),
  var programStatus: String = "ALLOC",
  var waitListDecisionCode: String? = null,
  var courseActivityId: Long = -1,
  var programId: Long = -1
) {

  fun save(
    offenderBookingId: Long,
    prisonId: String,
    dataLoader: DataLoaderRepository,
  ): OffenderProgramProfile {

    val prison = dataLoader.agencyLocationRepository.let {
      it.findByIdOrNull(prisonId) ?: throw BadRequestException("prison $prisonId not found")
    }

    val offenderBooking = dataLoader.offenderBookingRepository.findByBookingId(offenderBookingId).orElseThrow(BadRequestException("booking $offenderBookingId not found"))

    val courseActivity = dataLoader.courseActivityRepository.findByIdOrNull(courseActivityId) ?: throw BadRequestException("prison $prisonId not found")

    val offenderProgramProfile =
      OffenderProgramProfile.builder().offenderBooking(offenderBooking).programStatus(programStatus)
        .programId(programId).agencyLocation(prison).courseActivity(courseActivity).startDate(startDate)
        .waitlistDecisionCode(waitListDecisionCode).build()

    return dataLoader.offenderProgramProfileRepository.save(offenderProgramProfile)
  }
}
