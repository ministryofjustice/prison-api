package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.RequestSplashCondition
import uk.gov.justice.hmpps.prison.api.model.RequestSplashScreenCreateOrUpdate
import uk.gov.justice.hmpps.prison.api.model.SplashConditionDto
import uk.gov.justice.hmpps.prison.api.model.SplashScreenDto
import uk.gov.justice.hmpps.prison.service.SplashScreenService

@RestController
@Tag(name = "splash-screen")
@RequestMapping(value = ["\${api.base.path}/splash-screen"], produces = ["application/json"])
class SplashScreenResource(private val splashScreenService: SplashScreenService) {

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Get all splash screens",
    description = "Returns a list of all splash screens. Requires PRISON_API__SPLASH_SCREEN__RO.",
  )
  @PreAuthorize("hasAnyRole('PRISON_API__SPLASH_SCREEN__RO', 'PRISON_API__SPLASH_SCREEN__RW')")
  @GetMapping
  fun getAllSplashScreens(): List<SplashScreenDto> = splashScreenService.getAllSplashScreens()

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Get splash screen by module name",
    description = "Returns a splash screen by module name. Requires PRISON_API__SPLASH_SCREEN__RO.",
  )
  @PreAuthorize("hasAnyRole('PRISON_API__SPLASH_SCREEN__RO', 'PRISON_API__SPLASH_SCREEN__RW')")
  @GetMapping("/{moduleName}")
  fun getSplashScreenByModuleName(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
  ): SplashScreenDto = splashScreenService.getSplashScreenByModuleName(moduleName)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Get splash screens by condition",
    description = "Returns a list of splash screens that have the specified condition. Requires PRISON_API__SPLASH_SCREEN__RO.",
  )
  @PreAuthorize("hasAnyRole('PRISON_API__SPLASH_SCREEN__RO', 'PRISON_API__SPLASH_SCREEN__RW')")
  @GetMapping("/condition/{conditionType}/{conditionValue}")
  fun getSplashScreensByCondition(
    @PathVariable
    @Parameter(description = "Condition type", example = "CASELOAD")
    conditionType: String,
    @PathVariable
    @Parameter(description = "Condition value", example = "MDI")
    conditionValue: String,
  ): List<SplashScreenDto> = splashScreenService.getSplashScreensByCondition(conditionType, conditionValue)

  @ApiResponses(
    ApiResponse(responseCode = "201", description = "Created"),
    ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "409", description = "Conflict", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Create a new splash screen",
    description = "Creates a new splash screen. Requires PRISON_API__SPLASH_SCREEN__RW.",
  )
  @PreAuthorize("hasRole('PRISON_API__SPLASH_SCREEN__RW')")
  @PostMapping("/{moduleName}")
  @ResponseStatus(HttpStatus.CREATED)
  fun createSplashScreen(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
    @RequestBody
    @Parameter(description = "Splash screen details")
    splashScreenDto: RequestSplashScreenCreateOrUpdate,
  ): SplashScreenDto = splashScreenService.createSplashScreen(moduleName, splashScreenDto)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Update a splash screen",
    description = "Updates an existing splash screen. Requires PRISON_API__SPLASH_SCREEN__RW.",
  )
  @PreAuthorize("hasRole('PRISON_API__SPLASH_SCREEN__RW')")
  @PutMapping("/{moduleName}")
  fun updateSplashScreen(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
    @RequestBody
    @Parameter(description = "Splash screen details")
    request: RequestSplashScreenCreateOrUpdate,
  ): SplashScreenDto = splashScreenService.updateSplashScreen(moduleName, request)

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "No Content"),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Delete a splash screen",
    description = "Deletes an existing splash screen. Requires PRISON_API__SPLASH_SCREEN__RW.",
  )
  @PreAuthorize("hasRole('PRISON_API__SPLASH_SCREEN__RW')")
  @DeleteMapping("/{moduleName}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteSplashScreen(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
  ) = splashScreenService.deleteSplashScreen(moduleName)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Get conditions by type for a splash screen",
    description = "Returns a list of conditions of the specified type for a splash screen. Requires PRISON_API__SPLASH_SCREEN__RO.",
  )
  @PreAuthorize("hasAnyRole('PRISON_API__SPLASH_SCREEN__RO', 'PRISON_API__SPLASH_SCREEN__RW')")
  @GetMapping("/{moduleName}/condition/{conditionType}")
  fun getConditionsByType(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
    @PathVariable
    @Parameter(description = "Condition type", example = "CASELOAD")
    conditionType: String,
  ): List<SplashConditionDto> = splashScreenService.getConditionsByType(moduleName, conditionType)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Get conditions by type and value for a splash screen",
    description = "Returns a condition of the specified type for a splash screen. Requires PRISON_API__SPLASH_SCREEN__RO.",
  )
  @PreAuthorize("hasAnyRole('PRISON_API__SPLASH_SCREEN__RO', 'PRISON_API__SPLASH_SCREEN__RW')")
  @GetMapping("/{moduleName}/condition/{conditionType}/{conditionValue}")
  fun getConditionsByTypeAndValue(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
    @PathVariable
    @Parameter(description = "Condition type", example = "CASELOAD")
    conditionType: String,
    @PathVariable
    @Parameter(description = "Condition value", example = "MDI")
    conditionValue: String,
  ): SplashConditionDto = splashScreenService.getConditionsByTypeAndValue(moduleName, conditionType, conditionValue)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "409", description = "Conflict", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Add a condition to a splash screen",
    description = "Adds a new condition to an existing splash screen. Requires PRISON_API__SPLASH_SCREEN__RW.",
  )
  @PreAuthorize("hasRole('PRISON_API__SPLASH_SCREEN__RW')")
  @PostMapping("/{moduleName}/condition")
  fun addCondition(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
    @RequestBody
    @Parameter(description = "Condition details")
    conditionDto: RequestSplashCondition,
  ): SplashScreenDto = splashScreenService.addCondition(moduleName, conditionDto)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Bad Request", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Update a condition",
    description = "Updates an existing condition. Requires PRISON_API__SPLASH_SCREEN__RW.",
  )
  @PreAuthorize("hasRole('PRISON_API__SPLASH_SCREEN__RW')")
  @PutMapping("/{moduleName}/condition/{conditionType}/{conditionValue}/{blockAccess}")
  @ResponseStatus(HttpStatus.OK)
  fun updateCondition(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
    @PathVariable
    @Parameter(description = "Condition type", example = "CASELOAD")
    conditionType: String,
    @PathVariable
    @Parameter(description = "Condition value", example = "MDI")
    conditionValue: String,
    @PathVariable
    @Parameter(description = "Block access", example = "true")
    blockAccess: Boolean,
  ): SplashScreenDto = splashScreenService.updateCondition(moduleName, conditionType, conditionValue, blockAccess)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Not Found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Remove a condition",
    description = "Removes an existing condition. Requires PRISON_API__SPLASH_SCREEN__RW.",
  )
  @PreAuthorize("hasRole('PRISON_API__SPLASH_SCREEN__RW')")
  @DeleteMapping("/{moduleName}/condition/{conditionType}/{conditionValue}")
  fun removeCondition(
    @PathVariable
    @Parameter(description = "Module name", example = "OIDCHOLO")
    moduleName: String,
    @PathVariable
    @Parameter(description = "Condition type", example = "CASELOAD")
    conditionType: String,
    @PathVariable
    @Parameter(description = "Condition value", example = "MDI")
    conditionValue: String,
  ): SplashScreenDto = splashScreenService.removeCondition(moduleName, conditionType, conditionValue)
}
