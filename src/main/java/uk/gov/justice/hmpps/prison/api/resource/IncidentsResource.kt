package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.IncidentCase
import uk.gov.justice.hmpps.prison.service.IncidentService

@RestController
@Tag(name = "incidents")
@Validated
@RequestMapping(value = ["\${api.base.path}/incidents"], produces = ["application/json"])
class IncidentsResource(
  private val incidentService: IncidentService,
) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK", content = [Content(mediaType = "application/json", schema = Schema(implementation = IncidentCase::class))]),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "Return an Incident for a given incident ID", description = "Requires the VIEW_INCIDENTS role.")
  @GetMapping("/{incidentId}")
  @PreAuthorize("hasRole('VIEW_INCIDENTS')")
  fun getIncident(
    @PathVariable("incidentId")
    @Parameter(description = "Incident Id", required = true)
    incidentId: @NotNull Long,
  ): IncidentCase {
    return incidentService.getIncidentCase(incidentId)
  }
}
