package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.api.model.Telephone;

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

    @GetMapping("/{personId}/addresses")
    @ApiOperation(value = "The addresses for person", notes = "The addresses for person", nickname = "getPersonAddresses")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<AddressDto> getPersonAddresses(@ApiParam(value = "The persons NOMIS identifier (personId).", required = true) @PathVariable("personId") Long personId);

    @GetMapping("/{personId}/phones")
    @ApiOperation(value = "The phone numbers for person", notes = "The phone numbers for person", nickname = "getPersonPhones")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Telephone> getPersonPhones(@ApiParam(value = "The persons NOMIS identifier (personId).", required = true) @PathVariable("personId") Long personId);

    @GetMapping("/{personId}/emails")
    @ApiOperation(value = "The emails for person", notes = "The emails for person", nickname = "getPersonEmails")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Email> getPersonEmails(@ApiParam(value = "The persons NOMIS identifier (personId).", required = true) @PathVariable("personId") Long personId);
}
