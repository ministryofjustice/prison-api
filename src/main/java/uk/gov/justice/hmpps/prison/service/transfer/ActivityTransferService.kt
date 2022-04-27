package uk.gov.justice.hmpps.prison.service.transfer

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import java.time.LocalDate

@Service
class ActivityTransferService(private val offenderProgramProfileRepository: OffenderProgramProfileRepository) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun endActivitiesAndWaitlist(booking: OffenderBooking, fromAgency: AgencyLocation, endDate: LocalDate, endReason: OffenderProgramEndReason) {
    offenderProgramProfileRepository.endActivitiesForBookingAtPrison(booking, fromAgency, endDate, endReason)
    offenderProgramProfileRepository.endWaitListActivitiesForBookingAtPrison(booking, fromAgency, endDate, endReason)
  }
}
