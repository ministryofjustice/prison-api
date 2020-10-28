package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.service.IncidentService;

import javax.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("${api.base.path}/incidents")
@AllArgsConstructor
public class IncidentsResource {

    private final IncidentService incidentService;

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = IncidentCase.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Return an Incident for a given incident ID", notes = "System access only")
    @GetMapping("/{incidentId}")
    public IncidentCase getIncident(@NotNull @PathVariable("incidentId") @ApiParam(value = "Incident Id", required = true) final Long incidentId) {
        return incidentService.getIncidentCase(incidentId);

    }

}
