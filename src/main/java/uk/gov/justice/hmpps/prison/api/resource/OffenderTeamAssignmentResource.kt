package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.OffenderTeamAssignmentResponse
import uk.gov.justice.hmpps.prison.service.OffenderTeamAssignmentService

@RestController
@Validated
@Tag(name = "offender-team-assignments")
@RequestMapping(value = [$$"${api.base.path}/offender-team-assignments"], produces = ["application/json"])
@PreAuthorize("hasRole('VIEW_PRISONER_DATA') and hasAuthority('SCOPE_read')")
class OffenderTeamAssignmentResource(
  private val service: OffenderTeamAssignmentService,
) {
  @GetMapping
  @Operation(
    summary = "Get a page of team assignments across all prisoner bookings.",
    description = """
      Requires VIEW_PRISONER_DATA and read scope. Includes historical bookings and inactive teams.
      Results are always ordered by booking ID, then function type, ascending; caller-supplied sort is ignored.
      Increment page, keeping size unchanged, until last is true. Size is capped at 5000.
      Complete extraction assumes the source does not change between requests; this is not a snapshot.
    """,
  )
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "A page of team assignments, possibly empty."),
    ApiResponse(responseCode = "400", description = "Invalid pagination.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "401", description = "Unauthenticated.", content = [Content()]),
    ApiResponse(responseCode = "403", description = "Missing the required role or read scope.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
  )
  fun getAssignments(
    @RequestParam(defaultValue = "0")
    @Parameter(description = "Zero-based page number.", example = "0")
    @Min(0)
    page: Int,
    @RequestParam(defaultValue = "500")
    @Parameter(description = "Page size, capped at 5000.", example = "500")
    @Min(1)
    size: Int,
  ): Page<OffenderTeamAssignmentResponse> = service.getAssignments(page, size)
}
