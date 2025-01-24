package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException

abstract class StaffAwareMovementService(
  private val staffUserAccountRepository: StaffUserAccountRepository,
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder,
) {
  internal fun getLoggedInStaff(): Result<StaffUserAccount> = hmppsAuthenticationHolder.username?. let {
    staffUserAccountRepository.findByIdOrNull(hmppsAuthenticationHolder.username)
      ?.let { Result.success(it) } ?: Result.failure(
      EntityNotFoundException.withId(hmppsAuthenticationHolder.username),
    )
  } ?: Result.failure(EntityNotFoundException.withId("no username supplied"))
}
