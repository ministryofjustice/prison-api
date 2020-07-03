package uk.gov.justice.hmpps.nomis.datacompliance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.ErrorResponse;
import net.syscon.prison.api.model.PendingDeletionRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataComplianceReferralService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static org.springframework.data.domain.Pageable.unpaged;

@Slf4j
@RestController
@Api(tags = {"/data-compliance"})
@RequestMapping("${api.base.path}/data-compliance")
@AllArgsConstructor
public class DataComplianceController {

    private final DataComplianceReferralService offenderDataComplianceService;

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
}
