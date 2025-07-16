package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.RequestSplashConditionUpdate
import uk.gov.justice.hmpps.prison.api.model.RequestSplashScreenCreateOrUpdate
import uk.gov.justice.hmpps.prison.api.model.SplashConditionDto
import uk.gov.justice.hmpps.prison.api.model.SplashScreenDto
import uk.gov.justice.hmpps.prison.api.model.SplashScreenFunctionDto
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashCondition
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashScreen
import uk.gov.justice.hmpps.prison.repository.jpa.model.SplashScreenFunction
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SplashConditionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SplashScreenFunctionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SplashScreenRepository

@Service
@Transactional
class SplashScreenService(
  private val splashScreenRepository: SplashScreenRepository,
  private val splashScreenFunctionRepository: SplashScreenFunctionRepository,
  private val splashConditionRepository: SplashConditionRepository,
) {

  fun getSplashScreenByModuleName(moduleName: String): SplashScreenDto {
    val splashScreen = splashScreenRepository.findByModuleNameWithConditions(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")
    return mapToDto(splashScreen)
  }

  fun getAllSplashScreens(): List<SplashScreenDto> = splashScreenRepository.findAll().map { mapToDto(it) }

  fun getSplashScreensByCondition(conditionType: String, conditionValue: String): List<SplashScreenDto> = splashScreenRepository.findByConditionTypeAndValue(conditionType, conditionValue)
    .map { mapToDto(it) }

  fun createSplashScreen(moduleName: String, splashScreenDto: RequestSplashScreenCreateOrUpdate): SplashScreenDto {
    // Check if the module name already exists
    if (splashScreenRepository.findByModuleName(moduleName) != null) {
      throw ConflictingRequestException("Splash screen with module name $moduleName already exists")
    }

    // Find or create the function if provided
    val function = splashScreenDto.functionName?.let { functionName ->
      splashScreenFunctionRepository.findByFunctionName(functionName)
        ?: throw EntityNotFoundException("Function not found: $functionName")
    }

    // Create the splash screen
    val splashScreen = SplashScreen(
      moduleName = moduleName,
      function = function,
      warningText = splashScreenDto.warningText,
      blockedText = splashScreenDto.blockedText,
      blockAccessType = splashScreenDto.blockAccessType,
    )

    splashScreenDto.conditions.forEach {
      splashScreen.addCondition(
        conditionType = it.conditionType,
        conditionValue = it.conditionValue,
        blockAccess = it.blockAccess,
      )
    }
    val savedSplashScreen = splashScreenRepository.save(splashScreen)

    return mapToDto(splashScreenRepository.findByModuleNameWithConditions(moduleName)!!)
  }

  fun updateSplashScreen(moduleName: String, splashScreenDto: RequestSplashScreenCreateOrUpdate): SplashScreenDto {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    // Find function if provided and different from the current
    val function = if (splashScreenDto.functionName != null &&
      (splashScreen.function == null || splashScreenDto.functionName != splashScreen.function.functionName)
    ) {
      splashScreenFunctionRepository.findByFunctionName(splashScreenDto.functionName)
        ?: throw EntityNotFoundException("Function not found: ${splashScreenDto.functionName}")
    } else {
      splashScreen.function
    }

    // Update the splash screen
    val updatedSplashScreen = splashScreen.copy(
      function = function,
      warningText = splashScreenDto.warningText ?: splashScreen.warningText,
      blockedText = splashScreenDto.blockedText ?: splashScreen.blockedText,
      blockAccessType = splashScreenDto.blockAccessType,
    )

    splashScreenRepository.save(updatedSplashScreen)

    return mapToDto(splashScreenRepository.findByModuleNameWithConditions(moduleName)!!)
  }

  fun deleteSplashScreen(moduleName: String) {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    // Delete the splash screen
    splashScreenRepository.delete(splashScreen)
  }

  fun addCondition(moduleName: String, conditionDto: RequestSplashConditionUpdate): SplashScreenDto {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    // Check if the condition already exists
    if (splashConditionRepository.findBySplashScreenAndConditionTypeAndConditionValue(
        splashScreen,
        conditionDto.conditionType,
        conditionDto.conditionValue,
      ) != null
    ) {
      throw ConflictingRequestException("Condition already exists for this splash screen")
    }

    createCondition(splashScreen, conditionDto)

    return mapToDto(splashScreenRepository.findByModuleNameWithConditions(moduleName)!!)
  }

  fun updateCondition(
    moduleName: String,
    conditionType: String,
    conditionValue: String,
    blockAccess: Boolean,
  ): SplashScreenDto {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    val condition = splashConditionRepository.findBySplashScreenAndConditionTypeAndConditionValue(
      splashScreen,
      conditionType,
      conditionValue,
    ) ?: throw EntityNotFoundException("Condition not found")

    // Update the condition
    val updatedCondition = condition.copy(
      blockAccess = blockAccess,
    )

    splashConditionRepository.save(updatedCondition)

    return mapToDto(splashScreenRepository.findByModuleNameWithConditions(moduleName)!!)
  }

  fun removeCondition(moduleName: String, conditionType: String, conditionValue: String): SplashScreenDto {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    val condition = splashConditionRepository.findBySplashScreenAndConditionTypeAndConditionValue(
      splashScreen,
      conditionType,
      conditionValue,
    ) ?: throw EntityNotFoundException("Condition not found")

    splashConditionRepository.delete(condition)

    return mapToDto(splashScreenRepository.findByModuleNameWithConditions(moduleName)!!)
  }

  fun getConditionsByType(moduleName: String, conditionType: String): List<SplashConditionDto> {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    return splashConditionRepository.findBySplashScreenAndConditionType(splashScreen, conditionType)
      .map { mapToDto(it) }
  }

  private fun createCondition(splashScreen: SplashScreen, conditionDto: RequestSplashConditionUpdate): SplashCondition {
    val condition = SplashCondition(
      splashScreen = splashScreen,
      conditionType = conditionDto.conditionType,
      conditionValue = conditionDto.conditionValue,
      blockAccess = conditionDto.blockAccess,
    )

    return splashConditionRepository.save(condition)
  }

  private fun mapToDto(splashScreen: SplashScreen): SplashScreenDto = SplashScreenDto(
    splashId = splashScreen.splashId!!,
    moduleName = splashScreen.moduleName,
    functionName = splashScreen.function?.functionName,
    function = splashScreen.function?.let { mapToDto(it) },
    warningText = splashScreen.warningText,
    blockedText = splashScreen.blockedText,
    blockAccessType = splashScreen.blockAccessType,
    conditions = splashScreen.conditions.map { mapToDto(it) },
  )

  private fun mapToDto(function: SplashScreenFunction): SplashScreenFunctionDto = SplashScreenFunctionDto(
    functionName = function.functionName,
    description = function.description,
  )

  private fun mapToDto(condition: SplashCondition): SplashConditionDto = SplashConditionDto(
    splashConditionId = condition.splashConditionId!!,
    conditionType = condition.conditionType,
    conditionValue = condition.conditionValue,
    blockAccess = condition.blockAccess,
  )
}
