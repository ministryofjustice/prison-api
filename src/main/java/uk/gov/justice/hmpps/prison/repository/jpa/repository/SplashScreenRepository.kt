package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashScreen

interface SplashScreenRepository : CrudRepository<SplashScreen, Long> {
  fun findByModuleName(moduleName: String): SplashScreen?

  @Query("SELECT s FROM SplashScreen s LEFT JOIN FETCH s.conditions WHERE s.moduleName = :moduleName")
  fun findByModuleNameWithConditions(moduleName: String): SplashScreen?

  @Query("SELECT DISTINCT s FROM SplashScreen s LEFT JOIN FETCH s.conditions c WHERE c.conditionType = :conditionType AND c.conditionValue = :conditionValue")
  fun findByConditionTypeAndValue(conditionType: String, conditionValue: String): List<SplashScreen>
}
