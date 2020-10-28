package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.service.PersonService;

import java.util.List;

@RestController
@Validated
@RequestMapping("${api.base.path}/persons")
public class PersonResource {
    private final PersonService service;

    public PersonResource(final PersonService service) {
        this.service = service;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = PersonIdentifier.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The most recent value of each type of person identifier", notes = "The most recent value of each type of person identifier", nickname = "getPersonIdentifiers")
    @GetMapping("/{personId}/identifiers")
    public List<PersonIdentifier> getPersonIdentifiers(@PathVariable("personId") @ApiParam(value = "The persons NOMIS identifier (personId).", required = true) final Long personId) {
        return service.getPersonIdentifiers(personId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The addresses for person", notes = "The addresses for person", nickname = "getPersonAddresses")
    @GetMapping("/{personId}/addresses")
    public List<AddressDto> getPersonAddresses(@PathVariable("personId") @ApiParam(value = "The persons NOMIS identifier (personId).", required = true) final Long personId) {
        return service.getAddresses(personId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The phone numbers for person", notes = "The phone numbers for person", nickname = "getPersonPhones")
    @GetMapping("/{personId}/phones")
    public List<Telephone> getPersonPhones(@PathVariable("personId") @ApiParam(value = "The persons NOMIS identifier (personId).", required = true) final Long personId) {
        return service.getPhones(personId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The emails for person", notes = "The emails for person", nickname = "getPersonEmails")
    @GetMapping("/{personId}/emails")
    public List<Email> getPersonEmails(@PathVariable("personId") @ApiParam(value = "The persons NOMIS identifier (personId).", required = true) final Long personId) {
        return service.getEmails(personId);
    }
}
