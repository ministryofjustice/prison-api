package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.Questionnaire;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Api(tags = {"/questionnaires"})
public interface QuestionnaireResource {

    @GET
    @Path("/{category}/{code}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a questionnaire for a specified category and code")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Questionnaire.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    QuestionnaireResponse getQuestionnaire(@ApiParam(value = "Category", example = "IR_TYPE", required = true) @PathParam("category") @NotNull String category,
                                 @ApiParam(value = "Code", example = "ASSAULT", required = true) @PathParam("code") @NotNull String code);

    class QuestionnaireResponse extends ResponseDelegate {
        public QuestionnaireResponse(final Response response, final Questionnaire questionnaire) {
            super(response, questionnaire);
        }
    }


}
