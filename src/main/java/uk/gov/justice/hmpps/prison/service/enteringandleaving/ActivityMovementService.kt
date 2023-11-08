package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository
import uk.gov.justice.hmpps.prison.service.ServiceAgencySwitchesService
import java.time.LocalDate

@Service
@Transactional
class ActivityMovementService(
  private val offenderProgramProfileRepository: OffenderProgramProfileRepository,
  private val serviceAgencySwitchesService: ServiceAgencySwitchesService,
) {

  fun endActivitiesAndWaitlist(booking: OffenderBooking, fromAgency: AgencyLocation, endDate: LocalDate, endReason: String) {
    if (!serviceAgencySwitchesService.checkServiceSwitchedOnForPrison("ACTIVITY", fromAgency.id)) {
      offenderProgramProfileRepository.endActivitiesForBookingAtPrison(booking, fromAgency, endDate, endReason)
      offenderProgramProfileRepository.endWaitListActivitiesForBookingAtPrison(booking, fromAgency, endDate, endReason)
    }
  }
}
