package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.extern.slf4j.Slf4j
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.imprisonmentstatus.ImprisonmentStatusHistoryDto
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.service.imprisonmentstatus.ImprisonmentStatusHistoryService

@Slf4j
@RestController
@Tag(name = "imprisonment-status-history")
@Validated
@RequestMapping(
  value = ["\${api.base.path}/imprisonment-status-history"],
  produces = ["application/json"],
)
class ImprisonmentStatusHistoryResource(private val imprisonmentStatusHistoryService: ImprisonmentStatusHistoryService) {

  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The imprisonment status history.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ImprisonmentStatusHistoryDto::class),
        ),
      ],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "Returns the details of all the historic imprisonment statuses for an offender.")
  @GetMapping("/{offenderNo}")
  @VerifyOffenderAccess(overrideRoles = ["GLOBAL_SEARCH", "VIEW_PRISONER_DATA"])
  fun getImprisonmentStatusHistory(
    @PathVariable("offenderNo") @Parameter(
      description = "The required offender id (mandatory)",
      required = true,
    ) offenderNo: String,
  ): List<ImprisonmentStatusHistoryDto> {
    return imprisonmentStatusHistoryService.getImprisonmentStatusHistory(offenderNo)
  }
}
