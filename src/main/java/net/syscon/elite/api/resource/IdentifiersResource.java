package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.OffenderIdentifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = {"/identifiers"})
public interface IdentifiersResource {

    @GetMapping("/{type}/{value}")
    @ApiOperation(value = "Identifiers for a specified type and value", notes = "Empty list will be returned for no matches", nickname = "getOffenderIdentifiersByTypeAndValue"
            , authorizations = {@Authorization("SYSTEM_USER")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderIdentifier.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(@ApiParam(value = "Identifier Type", example = "PNC", required = true) @PathVariable("type") @NotNull String type,
                                                                 @ApiParam(value = "Identifier Value", example = "1234/XX", required = true) @PathVariable("value") @NotNull String value);

}
