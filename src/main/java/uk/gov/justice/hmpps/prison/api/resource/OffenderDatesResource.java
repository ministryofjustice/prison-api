package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.OffenderDatesService;

@Hidden
@RestController
@Validated
@Api(tags = {"offender-dates"})
@RequestMapping("${api.base.path}/offender-dates")
public class OffenderDatesResource {
    private final OffenderDatesService offenderDatesService;

    public OffenderDatesResource(OffenderDatesService offenderDatesService) {
        this.offenderDatesService = offenderDatesService;
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "The offender key dates provided were updated"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class)
    })
    @ApiOperation(value = "Update the key dates for an offender.", notes = "Requires MAINTAIN_OFFENDER_DATES")
    @PostMapping("/{bookingId}")
    @PreAuthorize("hasRole('MAINTAIN_OFFENDER_DATES') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public ResponseEntity<Void> updateOffenderDates(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId,
                                                    @RequestBody final RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        offenderDatesService.updateOffender(bookingId, requestToUpdateOffenderDates);
        return ResponseEntity.noContent().build();
    }
}
