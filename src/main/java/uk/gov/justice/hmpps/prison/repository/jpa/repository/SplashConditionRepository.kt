package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashCondition
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashScreen

interface SplashConditionRepository : CrudRepository<SplashCondition, Long> {
  fun findBySplashScreen(splashScreen: SplashScreen): List<SplashCondition>

  fun findBySplashScreenAndConditionType(splashScreen: SplashScreen, conditionType: String): List<SplashCondition>

  fun findBySplashScreenAndConditionTypeAndConditionValue(
    splashScreen: SplashScreen,
    conditionType: String,
    conditionValue: String,
  ): SplashCondition?

  fun deleteBySplashScreenAndConditionTypeAndConditionValue(
    splashScreen: SplashScreen,
    conditionType: String,
    conditionValue: String,
  )
}
