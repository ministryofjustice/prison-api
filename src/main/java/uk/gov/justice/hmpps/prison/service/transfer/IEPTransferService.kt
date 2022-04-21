package uk.gov.justice.hmpps.prison.service.transfer

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AvailablePrisonIepLevelRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.BadRequestException

@Service
class IEPTransferService(
  private val availablePrisonIepLevelRepository: AvailablePrisonIepLevelRepository,
  private val visitBalanceTransferService: VisitBalanceTransferService,
  staffUserAccountRepository: StaffUserAccountRepository,
  authenticationFacade: AuthenticationFacade,
) : StaffAwareTransferService(
  staffUserAccountRepository = staffUserAccountRepository,
  authenticationFacade = authenticationFacade
) {
  fun resetLevelForPrison(booking: OffenderBooking, transferMovement: ExternalMovement) {
    availablePrisonIepLevelRepository.findByAgencyLocation_IdAndDefaultIep(booking.location.id, true)
      .firstOrNull()?.run {
        booking.addIepLevel(
          this.iepLevel,
          "Admission to ${transferMovement.toAgency.description}",
          transferMovement.movementTime,
          getLoggedInStaff().getOrThrow()
        ).also {
          visitBalanceTransferService.adjustVisitBalances(booking)
        }
      } ?: throw BadRequestException("No default IEP level found")
  }
}
