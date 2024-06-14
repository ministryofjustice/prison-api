package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException

abstract class StaffAwareMovementService(
  private val staffUserAccountRepository: StaffUserAccountRepository,
  private val authenticationFacade: AuthenticationFacade,
) {
  internal fun getLoggedInStaff(): Result<StaffUserAccount> {
    return staffUserAccountRepository.findByIdOrNull(authenticationFacade.currentPrincipal)
      ?.let { Result.success(it) } ?: Result.failure(
      EntityNotFoundException.withId(authenticationFacade.currentPrincipal),
    )
  }
}
