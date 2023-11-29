package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelope
import uk.gov.justice.hmpps.prison.service.BookingService

@RestController
@Tag(name = "prison")
@Validated
@RequestMapping(value = ["/api/prison"], produces = ["application/json"])
class PrisonResource(private val bookingService: BookingService) {

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
  @Operation(summary = "Do not use - replaced with paginated version. Details of the active sentence envelope, a combination of the person information, the active booking and calculable sentences at a particular establishment", deprecated = true, hidden = true)
  @PreAuthorize("hasRole('RELEASE_DATE_MANUAL_COMPARER')")
  @GetMapping("/{establishmentId}/booking/latest/calculable-sentence-envelope")
  fun getCalculableSentenceEnvelopeByEstablishment(
    @PathVariable
    @Parameter(description = "The identifier of the establishment(prison) to get the active bookings for", required = true)
    establishmentId: String,
  ): List<CalculableSentenceEnvelope> {
    return this.bookingService.getCalculableSentenceEnvelopeByEstablishment(establishmentId)
  }

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
  @Operation(summary = "Details of the active sentence envelope, a combination of the person information, the active booking and calculable sentences at a particular establishment (paged response)")
  @PreAuthorize("hasRole('RELEASE_DATE_MANUAL_COMPARER')")
  @GetMapping("/{establishmentId}/booking/latest/paged/calculable-sentence-envelope")
  fun getPagedCalculableSentenceEnvelopeByEstablishment(
    @PathVariable
    @Parameter(description = "The identifier of the establishment(prison) to get the active bookings for", required = true)
    establishmentId: String,
    @RequestParam(value = "page", defaultValue = "0", required = false)
    @Parameter(description = "The page number to retrieve of the paged results (starts at zero)")
    page: Int,
    @RequestParam(value = "size", defaultValue = "200", required = false)
    @Parameter(description = "Requested limit of the page size (i.e. the number of bookings in response)")
    size: Int,
  ): Page<CalculableSentenceEnvelope> {
    return this.bookingService.getPagedCalculableSentenceEnvelopeByEstablishment(establishmentId, page, size)
  }
}
