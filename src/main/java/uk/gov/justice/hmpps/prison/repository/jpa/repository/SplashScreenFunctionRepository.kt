package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashScreenFunction

interface SplashScreenFunctionRepository : CrudRepository<SplashScreenFunction, String> {
  fun findByFunctionName(functionName: String): SplashScreenFunction?
}
