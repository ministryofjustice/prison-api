package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Questionnaire;

import javax.validation.constraints.NotNull;

@Api(tags = {"/questionnaires"})
public interface QuestionnaireResource {

    @GetMapping("/{category}/{code}")
    @ApiOperation(value = "Return a questionnaire for a specified category and code")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Questionnaire.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Questionnaire getQuestionnaire(@ApiParam(value = "Category", example = "IR_TYPE", required = true) @PathVariable("category") @NotNull String category,
                                           @ApiParam(value = "Code", example = "ASSAULT", required = true) @PathVariable("code") @NotNull String code);



}
