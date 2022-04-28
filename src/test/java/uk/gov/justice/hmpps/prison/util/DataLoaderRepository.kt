package uk.gov.justice.hmpps.prison.util

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourseActivityRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository

@Service
class DataLoaderRepository(
  val courseActivityRepository: CourseActivityRepository,
  val agencyLocationRepository: AgencyLocationRepository,
  val bookingRepository: BookingRepository,
  val offenderBookingRepository: OffenderBookingRepository,
  val offenderProgramProfileRepository: OffenderProgramProfileRepository,

)
