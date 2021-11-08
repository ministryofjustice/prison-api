package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyDates;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.OffenderDatesService;

@Hidden
@RestController
@Validated
@Api(tags = {"offender-dates"})
@RequestMapping("${api.base.path}/offender-dates")
@AllArgsConstructor
public class OffenderDatesResource {

    private final OffenderDatesService offenderDatesService;

    @ApiResponses({
        @ApiResponse(code = 201, message = "Offender key dates calculation created", response = OffenderKeyDates.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "Forbidden - user not authorised to update a agency location", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)
    })
    @ApiOperation(value = "Update the key dates for an offender.", notes = "Requires MAINTAIN_OFFENDER_DATES")
    @PostMapping("/{bookingId}")
    @PreAuthorize("hasRole('MAINTAIN_OFFENDER_DATES') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public ResponseEntity<OffenderKeyDates> updateOffenderKeyDates(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId,
                                                                   @RequestBody final RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(offenderDatesService.updateOffenderKeyDates(bookingId, requestToUpdateOffenderDates));
    }
}
