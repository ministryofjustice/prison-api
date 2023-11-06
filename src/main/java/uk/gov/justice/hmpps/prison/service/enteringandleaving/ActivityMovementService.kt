package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import java.time.LocalDate

@Service
@Transactional
class ActivityMovementService(private val offenderProgramProfileRepository: OffenderProgramProfileRepository) {

  fun endActivitiesAndWaitlist(booking: OffenderBooking, fromAgency: AgencyLocation, endDate: LocalDate, endReason: String) {
    offenderProgramProfileRepository.endActivitiesForBookingAtPrison(booking, fromAgency, endDate, endReason)
    offenderProgramProfileRepository.endWaitListActivitiesForBookingAtPrison(booking, fromAgency, endDate, endReason)
  }
}
