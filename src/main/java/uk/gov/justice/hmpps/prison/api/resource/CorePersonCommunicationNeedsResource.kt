package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.CorePersonCommunicationNeeds
import uk.gov.justice.hmpps.prison.api.model.CorePersonLanguagePreferencesRequest
import uk.gov.justice.hmpps.prison.api.model.CorePersonSecondaryLanguageRequest
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService

@RestController
@Tag(name = "offenders")
@Tag(name = "core-person-record")
@Validated
@RequestMapping(value = ["/api/offenders"], produces = ["application/json"])
class CorePersonCommunicationNeedsResource(
  private val prisonerProfileUpdateService: PrisonerProfileUpdateService,
) {

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "404", description = "Offender not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Get core person record physical attributes of a prisoner", description = "Requires the PRISON_API__PRISONER_PROFILE__RW role.")
  @GetMapping("/{offenderNo}/core-person-record/communication-needs")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  fun getCommunicationNeeds(
    @PathVariable("offenderNo") @Parameter(description = "The offender number", required = true) offenderNo: @NotNull String,
  ): ResponseEntity<CorePersonCommunicationNeeds> {
    val communicationNeeds = prisonerProfileUpdateService.getCommunicationNeeds(offenderNo)
    return ResponseEntity.ok(communicationNeeds)
  }

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "The core person language preferences have been updated."),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Update core person record language preferences of a prisoner.", description = "Requires the PRISON_API__PRISONER_PROFILE__RW role.")
  @PutMapping("/{offenderNo}/core-person-record/language-preferences")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ProxyUser
  fun updateLanguagePreferences(
    @PathVariable("offenderNo") @Parameter(description = "The offender number", required = true) offenderNo: @NotNull String,
    @RequestBody(required = true) @Valid languagePreferences: CorePersonLanguagePreferencesRequest,
  ) {
    prisonerProfileUpdateService.createOrUpdateLanguagePreferences(offenderNo, languagePreferences)
  }

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "A core person secondary language has been added or updated."),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Adds or replaces core person record secondary language data.", description = "Requires the PRISON_API__PRISONER_PROFILE__RW role.")
  @PutMapping("/{offenderNo}/core-person-record/secondary-language")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ProxyUser
  fun addSecondaryLanguage(
    @PathVariable("offenderNo") @Parameter(description = "The offender number", required = true) offenderNo: @NotNull String,
    @RequestBody(required = true) @Valid secondaryLanguage: CorePersonSecondaryLanguageRequest,
  ) {
    prisonerProfileUpdateService.addOrUpdateSecondaryLanguage(offenderNo, secondaryLanguage)
  }

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "A core person secondary language has been deleted."),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Deletes core person record secondary language data.", description = "Requires the PRISON_API__PRISONER_PROFILE__RW role.")
  @DeleteMapping("/{offenderNo}/core-person-record/secondary-language/{languageCode}")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ProxyUser
  fun deleteSecondaryLanguage(
    @PathVariable("offenderNo") @Parameter(description = "The offender number", required = true) offenderNo: @NotNull String,
    @PathVariable("languageCode") @Parameter(description = "The code for the secondary language to delete", required = true) languageCode: @NotNull String,
  ) {
    prisonerProfileUpdateService.deleteSecondaryLanguage(offenderNo, languageCode)
  }
}
