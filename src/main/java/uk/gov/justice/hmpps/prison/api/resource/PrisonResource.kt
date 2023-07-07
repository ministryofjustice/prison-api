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
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary
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
  @Operation(summary = "Details of the active offender booking and calculable sentences at a particular establishment")
  @PreAuthorize("hasRole('RELEASE_DATE_MANUAL_COMPARER')")
  @GetMapping("/{establishmentId}/booking/latest/sentence-summary")
  fun getActiveOffenderBookingsByEstablishment(
    @PathVariable
    @Parameter(description = "The identifier of the establishment(prison) to get the active bookings for", required = true)
    establishmentId: String,
  ): List<SentenceSummary> {
    return this.bookingService.getActiveBookingsForEstablishment(establishmentId)
  }
}
