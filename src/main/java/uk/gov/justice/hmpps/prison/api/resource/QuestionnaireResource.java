package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Questionnaire;
import uk.gov.justice.hmpps.prison.service.IncidentService;

import javax.validation.constraints.NotNull;

@RestController
@Api(tags = {"questionnaires"})
@Validated
@RequestMapping("${api.base.path}/questionnaires")
public class QuestionnaireResource {
    private final IncidentService incidentService;

    public QuestionnaireResource(final IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Questionnaire.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation("Return a questionnaire for a specified category and code")
    @GetMapping("/{category}/{code}")
    public Questionnaire getQuestionnaire(@PathVariable("category") @ApiParam(value = "Category", example = "IR_TYPE", required = true) @NotNull final String category, @PathVariable("code") @ApiParam(value = "Code", example = "ASSAULT", required = true) @NotNull final String code) {
        return incidentService.getQuestionnaire(category, code);
    }
}
