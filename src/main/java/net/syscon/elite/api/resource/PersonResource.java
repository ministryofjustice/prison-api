package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.PersonIdentifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Api(tags = {"/persons"})
@SuppressWarnings("unused")
public interface PersonResource {

    @GetMapping("/{personId}/identifiers")
    @ApiOperation(value = "The most recent value of each type of person identifier", notes = "The most recent value of each type of person identifier", nickname = "getPersonIdentifiers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PersonIdentifier.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PersonIdentifier> getPersonIdentifiers(@ApiParam(value = "The persons NOMIS identifier (personId).", required = true) @PathVariable("personId") Long personId);

}
