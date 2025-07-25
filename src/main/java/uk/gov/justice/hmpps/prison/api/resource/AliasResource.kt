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
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.CreateAlias
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.UpdateAlias
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateService

@RestController
@Tag(name = "aliases")
@Validated
@RequestMapping(value = ["\${api.base.path}"], produces = ["application/json"])
class AliasResource(private val prisonerProfileUpdateService: PrisonerProfileUpdateService) {

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "Aliases successfully returned."),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Retrieve all aliases (including the working name alias) for the prisoner. " +
      "Requires the PRISON_API__PRISONER_PROFILE__RW role.",
  )
  @GetMapping("/offenders/{offenderNo}/aliases")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @ResponseStatus(OK)
  fun getAliases(
    @PathVariable("offenderNo") @Parameter(description = "The prisoner number", required = true) prisonerNumber: String,
  ) = prisonerProfileUpdateService.getAliases(prisonerNumber)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "Alias successfully returned."),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Retrieve a specific aliases for the prisoner. " +
      "Requires the PRISON_API__PRISONER_PROFILE__RW role.",
  )
  @GetMapping("/aliases/{offenderId}")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @ResponseStatus(OK)
  fun getAlias(
    @PathVariable("offenderId") @Parameter(description = "The alias identifier (offenderId)", required = true) offenderId: Long,
  ) = prisonerProfileUpdateService.getAlias(offenderId)

  @ApiResponses(
    ApiResponse(responseCode = "201", description = "A new alias for the prisoner has been created."),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Create a new alias for the prisoner. Requires the PRISON_API__PRISONER_PROFILE__RW role.",
  )
  @PostMapping("/offenders/{offenderNo}/aliases")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @ResponseStatus(CREATED)
  @ProxyUser
  fun createAlias(
    @PathVariable("offenderNo") @Parameter(description = "The prisoner number", required = true) prisonerNumber: String,
    @RequestBody @NotNull @Valid createAlias: CreateAlias,
  ) = prisonerProfileUpdateService.createAlias(prisonerNumber, createAlias)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "The prisoner's alias has been updated."),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Update the prisoner's alias. Requires the PRISON_API__PRISONER_PROFILE__RW role.",
  )
  @PutMapping("/aliases/{offenderId}")
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @ProxyUser
  fun updateAlias(
    @PathVariable("offenderId") @Parameter(description = "The alias identifier (offenderId)", required = true) offenderId: Long,
    @RequestBody @NotNull @Valid updateAlias: UpdateAlias,
  ) = prisonerProfileUpdateService.updateAlias(offenderId, updateAlias)
}
