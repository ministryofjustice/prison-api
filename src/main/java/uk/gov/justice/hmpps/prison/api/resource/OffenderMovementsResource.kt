package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.CellMoveResult
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.core.ProxyUser
import uk.gov.justice.hmpps.prison.service.BookingService
import uk.gov.justice.hmpps.prison.service.MovementUpdateService
import java.time.LocalDateTime

@RestController
@Tag(name = "offenders")
@RequestMapping(value = ["\${api.base.path}/offenders"], produces = ["application/json"])
@Validated
class OffenderMovementsResource(
  private val movementUpdateService: MovementUpdateService,
  private val bookingService: BookingService,
) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "Move the prisoner to the specified cell.",
    description = """Unilink specific version of /api/bookings/{bookingId}/living-unit/{internalLocationDescription}.<br/>
      Requires either a valid user token or a token with UNILINK role.""",
  )
  @Tag(name = "unilink")
  @PutMapping("/{offenderNo}/living-unit/{internalLocationDescription}")
  @ProxyUser
  fun moveToCell(
    @PathVariable("offenderNo")
    @Parameter(name = "offenderNo", description = "Offender No", example = "A1234AA", required = true)
    offenderNo: @NotNull String?,
    @PathVariable("internalLocationDescription")
    @Parameter(description = "The cell location the offender has been moved to", example = "MDI-1-1", required = true)
    internalLocationDescription: String?,
    @RequestParam("reasonCode")
    @Parameter(description = "The reason code for the move (from reason code domain CHG_HOUS_RSN)", example = "ADM", required = true)
    reasonCode: String?,
    @RequestParam(value = "dateTime", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Parameter(description = "The date / time of the move (defaults to current)", example = "2020-03-24T12:13:40")
    dateTime: LocalDateTime?,
  ): CellMoveResult {
    val booking = bookingService.getLatestBookingByOffenderNo(offenderNo)
    return movementUpdateService.moveToCell(booking.bookingId, internalLocationDescription, reasonCode, dateTime)
  }
}
