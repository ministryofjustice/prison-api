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
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.service.GangMemberSummary
import uk.gov.justice.hmpps.prison.service.GangService

@RestController
@Tag(name = "gang")
@Validated
@RequestMapping(value = ["/api/gang"], produces = ["application/json"])
class GangResource(private val gangService: GangService) {

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
  @Operation(summary = "Returns a list of gangs for a prisoner and the gang non-associations and their members")
  @PreAuthorize("hasRole('VIEW_GANG')")
  @GetMapping("/non-associations/{offenderNo}")
  fun getNonAssociationGangsForPrisoner(
    @PathVariable
    @Parameter(description = "Prisoner number", required = true, example = "A1234AA")
    offenderNo: String,
  ): GangMemberSummary {
    return gangService.getNonAssociatesInGangs(offenderNo)
  }
}
