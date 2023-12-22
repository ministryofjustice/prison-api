package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.core.ReferenceData
import uk.gov.justice.hmpps.prison.service.DatabaseRestoreInfoService
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDate

@RestController
@Tag(name = "database-restore")
@RequestMapping(value = ["\${api.base.path}/restore-info"], produces = ["application/json"])
class DatabaseRestoreInfoResource(private val service: DatabaseRestoreInfoService) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "404", description = "No restore information found - this endpoint is only guaranteed to return information when run on pre-prod.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "The last restore date or not found is returned if no restore data available")
  @ReferenceData(description = "Non-sensitive info")
  @GetMapping
  fun getLastRestoreDate(): LocalDate = service.getLastRestoreDate() ?: throw EntityNotFoundException("No restore data found")
}
