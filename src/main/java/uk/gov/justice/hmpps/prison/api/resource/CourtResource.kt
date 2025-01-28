package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.CourtEventDetails
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.service.CourtService

@RestController
@Tag(name = "court")
@Validated
@RequestMapping(value = ["\${api.base.path}/court"], produces = ["application/json"])
class CourtResource(private val courtService: CourtService) {

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
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
  @Operation(summary = "Returns the next court event details (if one exists) related to the passed in booking")
  @PreAuthorize("hasAnyRole('GLOBAL_SEARCH', 'VIEW_PRISONER_DATA', 'RELEASE_DATES_CALCULATOR')")
  @GetMapping("/{bookingId}/next-court-event")
  fun getNextCourtEvent(
    @PathVariable
    @Parameter(description = "The bookingId to check court events against", required = true)
    bookingId: Long,
  ): CourtEventDetails? = this.courtService.getNextCourtEvent(bookingId)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
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
  @Operation(summary = "Returns the number of active court cases for the passed in bookingId")
  @PreAuthorize("hasAnyRole('GLOBAL_SEARCH', 'VIEW_PRISONER_DATA', 'RELEASE_DATES_CALCULATOR')")
  @GetMapping("/{bookingId}/count-active-cases")
  fun getCountOfActiveCourtCases(
    @PathVariable
    @Parameter(description = "The bookingId to check court cases against", required = true)
    bookingId: Long,
  ): Int = this.courtService.getCountOfActiveCases(bookingId)
}
