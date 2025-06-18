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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierCreateRequest
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierUpdateRequest
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.service.OffenderIdentifierService

@RestController
@Tag(name = "offender-identifiers")
@Validated
@RequestMapping(value = ["\${api.base.path}"], produces = ["application/json"])
class OffenderIdentifierResource(private val offenderIdentifierService: OffenderIdentifierService) {

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "Forbidden - user not authorised to view identifiers.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA', 'GLOBAL_SEARCH')")
  @Operation(summary = "Get all identifiers for the prisoner (with or without alias identifiers)")
  @GetMapping("/offenders/{offenderNo}/offender-identifiers")
  fun getAllOffenderIdentifiers(
    @PathVariable("offenderNo") @Parameter(
      description = "The prisoner number",
      required = true,
    ) prisonerNumber: String?,
    @RequestParam(value = "includeAliases", required = false) includeAliases: Boolean,
  ): List<OffenderIdentifier> = offenderIdentifierService.getOffenderIdentifiers(prisonerNumber!!, includeAliases)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "Forbidden - user not authorised to view identifiers.",
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
  @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA', 'GLOBAL_SEARCH')")
  @Operation(summary = "Get a single identifier for the alias by sequence id")
  @GetMapping("/aliases/{offenderId}/offender-identifiers/{offenderIdSeq}")
  fun getOffenderIdentifier(
    @PathVariable("offenderId") @Parameter(
      description = "The alias identifier (offenderId)",
      required = true,
    ) offenderId: Long,
    @PathVariable("offenderIdSeq") @Parameter(
      description = "The sequence id",
      required = true,
    ) offenderIdSeq: Long,
  ): OffenderIdentifier = offenderIdentifierService.getOffenderIdentifierForAlias(offenderId, offenderIdSeq)

  @ApiResponses(
    ApiResponse(responseCode = "204", description = "Identifier updated."),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "Forbidden - user not authorised to update identifiers.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Identifier not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @Operation(summary = "Update an existing identifier for the alias")
  @PutMapping("/aliases/{offenderId}/offender-identifiers/{offenderIdSeq}")
  @ResponseStatus(
    HttpStatus.NO_CONTENT,
  )
  @ProxyUser
  fun updateOffenderIdentifier(
    @PathVariable("offenderId") @Parameter(
      description = "The alias identifier (offenderId)",
      required = true,
    ) offenderId: Long,
    @PathVariable("offenderIdSeq") @Parameter(
      description = "The identifier sequence",
      required = true,
    ) offenderIdSeq: Long,
    @RequestBody offenderIdentifierRequest:
    @NotNull @Valid
    OffenderIdentifierUpdateRequest,
  ) {
    offenderIdentifierService.updateOffenderIdentifierForAlias(offenderId, offenderIdSeq, offenderIdentifierRequest)
  }

  @ApiResponses(
    ApiResponse(responseCode = "201", description = "Identifiers added."),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "Forbidden - user not authorised to add identifiers.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Prisoner not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @Operation(summary = "Add identifiers for the prisoner on the current working name")
  @PostMapping("/offenders/{offenderNo}/offender-identifiers")
  @ProxyUser
  fun addOffenderIdentifiers(
    @PathVariable("offenderNo") @Parameter(
      description = "The prisoner number",
      required = true,
    ) prisonerNumber: String,
    @RequestBody offenderIdentifierRequests:
    @NotNull @Valid
    List<OffenderIdentifierCreateRequest>,
  ): ResponseEntity<Void> {
    offenderIdentifierService.addOffenderIdentifiers(prisonerNumber, offenderIdentifierRequests)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }
}
