package uk.gov.justice.hmpps.nomis.datacompliance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.model.PendingDeletionRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDataComplianceService;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@Api(tags = {"/data-compliance"})
@RequestMapping("${api.base.path}/data-compliance")
@AllArgsConstructor
public class DataComplianceController {

    private final OffenderDataComplianceService offenderDataComplianceService;

    @PostMapping("/offenders/pending-deletions")
    @ApiOperation(value = "Request a list of offender records to be considered for deletion under data protection law.",
            notes = "This is an asynchronous request, the resulting list will be pushed onto a queue rather than returned in the response body.",
            authorizations = { @Authorization("SYSTEM_USER") })
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted", response = IncidentCase.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<Void> requestOffenderPendingDeletions(@Valid @NotNull @RequestBody PendingDeletionRequest request) {

        offenderDataComplianceService.acceptOffendersPendingDeletionRequest(
                request.getRequestId(),
                request.getDueForDeletionWindowStart(),
                request.getDueForDeletionWindowEnd())
                .exceptionally(error -> {
                    log.error("Failed to handle pending deletion request", error);
                    return null;
                });

        return ResponseEntity.accepted().build();
    }
}
