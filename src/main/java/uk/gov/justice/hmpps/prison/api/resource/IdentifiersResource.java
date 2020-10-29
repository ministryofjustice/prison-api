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
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.service.InmateService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Validated
@RequestMapping("${api.base.path}/identifiers")
public class IdentifiersResource {
    private final InmateService inmateService;

    public IdentifiersResource(final InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderIdentifier.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Identifiers for a specified type and value", notes = "Empty list will be returned for no matches", nickname = "getOffenderIdentifiersByTypeAndValue")
    @GetMapping("/{type}/{value}")
    public List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(@NotNull @PathVariable("type") @ApiParam(value = "Identifier Type", example = "PNC", required = true) final String identifierType, @NotNull @PathVariable("value") @ApiParam(value = "Identifier Value", example = "1234/XX", required = true) final String identifierValue) {
        return inmateService.getOffenderIdentifiersByTypeAndValue(identifierType, identifierValue);
    }
}
