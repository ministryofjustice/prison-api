package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateResult;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.courtdates.CourtDateService;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "court-dates")
@Validated
@RequestMapping(value = {"${api.base.path}/court-date-results", "${api.base.path}/digital-warrant/court-date-results"}, produces = "application/json")
public class CourtDateResource {
    @Autowired
    private CourtDateService courtDateService;
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The court date results.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CourtDateResult.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Returns details of all court dates and the result of each.")
    @GetMapping("/{offenderId}")
    @PreAuthorize("hasRole('MANAGE_DIGITAL_WARRANT') and hasAuthority('SCOPE_read')")
    @SlowReportQuery
    public List<CourtDateResult> getCourtDateResults(@PathVariable("offenderId") @Parameter(description = "The required offender id (mandatory)", required = true) final String offenderId) {
        return courtDateService.getCourtDateResults(offenderId);
    }
}
