package uk.gov.justice.hmpps.nomis.datacompliance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataComplianceReferralService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderImageUpdateService;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.PendingDeletionRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.data.domain.Pageable.unpaged;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Slf4j
@RestController
@Api(tags = {"/data-compliance"})
@RequestMapping("${api.base.path}/data-compliance")
@AllArgsConstructor
public class DataComplianceController {

    private final DataComplianceReferralService offenderDataComplianceService;
    private final OffenderImageUpdateService offenderImageUpdateService;

    @PostMapping("/offenders/pending-deletions")
    @ApiOperation(value = "Request a list of offender records to be considered for deletion under data protection law.",
            notes = "This is an asynchronous request, the resulting list will be pushed onto a queue rather than returned in the response body.")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<Void> requestOffendersPendingDeletion(@Valid @NotNull @RequestBody final PendingDeletionRequest request) {

        final var pageRequest = Optional.ofNullable(request.getLimit())
                .map(limit -> (Pageable) PageRequest.of(0, limit))
                .orElse(unpaged());

        offenderDataComplianceService.acceptOffendersPendingDeletionRequest(
                request.getBatchId(),
                request.getDueForDeletionWindowStart(),
                request.getDueForDeletionWindowEnd(),
                pageRequest)
                .exceptionally(error -> {
                    log.error("Failed to handle pending deletion request", error);
                    return null;
                });

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/offenders-with-images")
    @ApiOperation(value = "Get offenders with images captured in provided range")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public Page<OffenderNumber> getOffendersWithImagesCapturedInRange(
            @ApiParam(value = "fromDateTime", required = true) @DateTimeFormat(iso = DATE_TIME) @RequestParam("fromDateTime") final LocalDateTime fromDate,
            @ApiParam(value = "toDateTime") @DateTimeFormat(iso = DATE_TIME) @RequestParam(value = "toDateTime", required = false) final LocalDateTime toDate,
            @PageableDefault(direction = ASC, sort = "offender_id_display") final Pageable pageable) {
        return offenderImageUpdateService.getOffendersWithImagesCapturedBetween(fromDate, toDate, pageable);
    }
}
