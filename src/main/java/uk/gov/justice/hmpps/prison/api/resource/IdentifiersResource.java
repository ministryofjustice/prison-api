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
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.InmateService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Validated
@Tag(name = "identifiers")
@RequestMapping(value = "${api.base.path}/identifiers", produces = "application/json")
public class IdentifiersResource {
    private final InmateService inmateService;

    public IdentifiersResource(final InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Identifiers for a specified type and value", description = "Empty list will be returned for no matches")
    @GetMapping("/{type}/{value}")
    @SlowReportQuery
    public List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(@NotNull @PathVariable("type") @Parameter(description = "Identifier Type", example = "PNC", required = true) final String identifierType, @NotNull @PathVariable("value") @Parameter(description = "Identifier Value", example = "1234/XX", required = true) final String identifierValue) {
        return inmateService.getOffenderIdentifiersByTypeAndValue(identifierType, identifierValue);
    }
}
