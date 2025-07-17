package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.RequestSplashCondition
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

  fun createSplashScreen(moduleName: String, createRequest: RequestSplashScreenCreateOrUpdate): SplashScreenDto {
    // Check if the module name already exists
    if (splashScreenRepository.findByModuleName(moduleName) != null) {
      throw ConflictingRequestException("Splash screen with module name $moduleName already exists")
    }

    // Find or create the function if provided
    val function = createRequest.functionName?.let { functionName ->
      splashScreenFunctionRepository.findByFunctionName(functionName)
        ?: throw EntityNotFoundException("Function not found: $functionName")
    }

    // Create the splash screen
    return mapToDto(
      splashScreenRepository.saveAndFlush(
        SplashScreen(
          moduleName = moduleName,
          function = function,
          warningText = createRequest.warningText,
          blockedText = createRequest.blockedText,
          blockAccessType = createRequest.blockAccessType,
        ).apply {
          createRequest.conditions.forEach {
            addCondition(
              conditionType = it.conditionType,
              conditionValue = it.conditionValue,
              blockAccess = it.blockAccess,
            )
          }
        },
      ),
    )
  }

  fun updateSplashScreen(moduleName: String, updateRequest: RequestSplashScreenCreateOrUpdate): SplashScreenDto {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    // Find function if provided and different from the current
    val function = if (updateRequest.functionName != null &&
      (splashScreen.function == null || updateRequest.functionName != splashScreen.function!!.functionName)
    ) {
      splashScreenFunctionRepository.findByFunctionName(updateRequest.functionName)
        ?: throw EntityNotFoundException("Function not found: ${updateRequest.functionName}")
    } else {
      splashScreen.function
    }

    // Update the splash screen
    splashScreen.function = function
    splashScreen.warningText = updateRequest.warningText ?: splashScreen.warningText
    splashScreen.blockedText = updateRequest.blockedText ?: splashScreen.blockedText
    splashScreen.blockAccessType = updateRequest.blockAccessType

    return mapToDto(splashScreen)
  }

  fun deleteSplashScreen(moduleName: String) {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    // Delete the splash screen
    splashScreenRepository.delete(splashScreen)
  }

  fun addCondition(moduleName: String, conditionDto: RequestSplashCondition): SplashScreenDto {
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

    splashScreen.addCondition(
      conditionType = conditionDto.conditionType,
      conditionValue = conditionDto.conditionValue,
      blockAccess = conditionDto.blockAccess,
    )
    return mapToDto(splashScreenRepository.saveAndFlush(splashScreen))
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
    condition.blockAccess = blockAccess

    return mapToDto(splashScreen)
  }

  fun removeCondition(moduleName: String, conditionType: String, conditionValue: String): SplashScreenDto {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    if (!splashScreen.removeCondition(conditionType, conditionValue)) {
      throw EntityNotFoundException("Condition not found")
    }

    return mapToDto(splashScreen)
  }

  fun getConditionsByType(moduleName: String, conditionType: String): List<SplashConditionDto> {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    return splashConditionRepository.findBySplashScreenAndConditionType(splashScreen, conditionType)
      .map { mapToDto(it) }
  }

  fun getConditionsByTypeAndValue(moduleName: String, conditionType: String, conditionValue: String): SplashConditionDto {
    val splashScreen = splashScreenRepository.findByModuleName(moduleName)
      ?: throw EntityNotFoundException("Splash screen not found for module: $moduleName")

    val condition = splashConditionRepository.findBySplashScreenAndConditionTypeAndConditionValue(
      splashScreen,
      conditionType,
      conditionValue,
    ) ?: throw EntityNotFoundException("Condition not found")

    return mapToDto(condition)
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
