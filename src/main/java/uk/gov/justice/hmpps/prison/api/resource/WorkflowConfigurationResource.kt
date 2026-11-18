package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.WorkflowFolderResponse
import uk.gov.justice.hmpps.prison.api.model.WorkflowScreenResponse
import uk.gov.justice.hmpps.prison.service.WorkflowConfigurationService

@RestController
@Validated
@Tag(name = "workflow-configuration")
@RequestMapping(value = [$$"${api.base.path}"], produces = ["application/json"])
@PreAuthorize("hasRole('VIEW_PRISONER_DATA') and hasAuthority('SCOPE_read')")
@ApiResponses(
  ApiResponse(responseCode = "400", description = "Invalid pagination.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
  ApiResponse(responseCode = "401", description = "Unauthenticated.", content = [Content()]),
  ApiResponse(responseCode = "403", description = "Missing the required role or read scope.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
  ApiResponse(responseCode = "500", description = "Unrecoverable error.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
)
class WorkflowConfigurationResource(
  private val service: WorkflowConfigurationService,
) {
  @GetMapping("/workflow-folders")
  @Operation(
    summary = "Get a page of NOMIS workflow folders, including folders without screens.",
    description = """
      Requires VIEW_PRISONER_DATA and read scope. All source columns are returned, including audit metadata and explicit nulls.
      Results are ordered by workflow code ascending; caller-supplied sort is ignored.
      Increment page, keeping size unchanged, until last is true. Size is capped at 5000.
      Complete extraction assumes both source tables remain unchanged; this endpoint does not provide a snapshot.
    """,
  )
  @ApiResponse(responseCode = "200", description = "A page of workflow folders, possibly empty.")
  fun getWorkflowFolders(
    @RequestParam(defaultValue = "0")
    @Parameter(description = "Zero-based page number.", example = "0")
    @Min(0)
    @Max(Int.MAX_VALUE.toLong() - 1)
    page: Int,
    @RequestParam(defaultValue = "500")
    @Parameter(description = "Page size, capped at 5000.", example = "500")
    @Min(1)
    size: Int,
  ): Page<WorkflowFolderResponse> = service.getFolders(page, size)

  @GetMapping("/workflow-screens")
  @Operation(
    summary = "Get a page of NOMIS workflow screens across all folders.",
    description = """
      Requires VIEW_PRISONER_DATA and read scope. All source columns are returned, including audit metadata and explicit nulls.
      Results are ordered by workflow code, then module name, ascending; caller-supplied sort is ignored.
      Increment page, keeping size unchanged, until last is true. Size is capped at 5000.
      Complete extraction assumes both source tables remain unchanged; this endpoint does not provide a snapshot.
    """,
  )
  @ApiResponse(responseCode = "200", description = "A page of workflow screens, possibly empty.")
  fun getWorkflowScreens(
    @RequestParam(defaultValue = "0")
    @Parameter(description = "Zero-based page number.", example = "0")
    @Min(0)
    @Max(Int.MAX_VALUE.toLong() - 1)
    page: Int,
    @RequestParam(defaultValue = "500")
    @Parameter(description = "Page size, capped at 5000.", example = "500")
    @Min(1)
    size: Int,
  ): Page<WorkflowScreenResponse> = service.getScreens(page, size)
}
