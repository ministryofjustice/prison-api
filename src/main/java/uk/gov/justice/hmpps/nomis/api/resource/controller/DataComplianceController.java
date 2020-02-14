package uk.gov.justice.hmpps.nomis.api.resource.controller;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Slf4j
@RestController
@Api(tags = {"/data-compliance"})
@RequestMapping("${api.base.path}/data-compliance")
public class DataComplianceController {

    @ApiIgnore("Not yet implemented")
    @PostMapping("/offenders/pending-deletions")
    @ApiOperation(value = "Request a list of offender records to be considered for deletion under data protection law.",
            notes = "This is an asynchronous request, the resulting list will be pushed onto a queue rather than returned in the response body.",
            authorizations = { @Authorization("SYSTEM_USER") })
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted", response = IncidentCase.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<Void> requestOffenderPendingDeletions(@Valid @NotNull @RequestBody PendingDeletionRequest request) {

        log.warn("Pending deletions request is not yet implemented, ignoring request: {}", request);

        return ResponseEntity.accepted().build();
    }
}
