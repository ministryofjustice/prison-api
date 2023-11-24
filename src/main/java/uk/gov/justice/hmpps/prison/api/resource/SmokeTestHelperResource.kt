package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.service.SmokeTestHelperService

@RestController
@Tag(name = "smoketest")
@RequestMapping(value = ["\${api.base.path}/smoketest"], produces = ["application/json"])
@Validated
@ConditionalOnProperty(name = ["smoke.test.aware"], havingValue = "true")
class SmokeTestHelperResource(private val service: SmokeTestHelperService) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "403",
      description = "Requires role ROLE_SMOKE_TEST",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "Sets imprisonment status smoke test data for this offender")
  @PostMapping("/offenders/{offenderNo}/imprisonment-status")
  @ProxyUser
  fun imprisonmentDataSetup(
    @PathVariable("offenderNo")
    @Parameter(description = "offenderNo", required = true, example = "A1234AA")
    offenderNo: String,
  ) {
    service.imprisonmentDataSetup(offenderNo)
  }

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "403",
      description = "Requires role ROLE_SMOKE_TEST",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "Releases this offender, with smoke test data")
  @PutMapping("/offenders/{offenderNo}/release")
  @ProxyUser
  fun releasePrisoner(
    @PathVariable("offenderNo")
    @Parameter(description = "offenderNo", required = true, example = "A1234AA")
    offenderNo: String,
  ) {
    service.releasePrisoner(offenderNo)
  }

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "403",
      description = "Requires role ROLE_SMOKE_TEST",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(summary = "Recalls this offender, with smoke test data")
  @PutMapping("/offenders/{offenderNo}/recall")
  @ProxyUser
  fun recallPrisoner(
    @PathVariable("offenderNo")
    @Parameter(description = "offenderNo", required = true, example = "A1234AA")
    offenderNo: String,
  ) {
    service.recallPrisoner(offenderNo)
  }
}
