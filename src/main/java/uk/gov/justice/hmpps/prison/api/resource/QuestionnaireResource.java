package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "questionnaires")
@Validated
@RequestMapping("${api.base.path}/questionnaires")
public class QuestionnaireResource {
    private final IncidentService incidentService;

    public QuestionnaireResource(final IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Questionnaire.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary =  "Return a questionnaire for a specified category and code")
    @GetMapping("/{category}/{code}")
    public Questionnaire getQuestionnaire(@PathVariable("category") @Parameter(description = "Category", example = "IR_TYPE", required = true) @NotNull final String category, @PathVariable("code") @Parameter(description = "Code", example = "ASSAULT", required = true) @NotNull final String code) {
        return incidentService.getQuestionnaire(category, code);
    }
}
