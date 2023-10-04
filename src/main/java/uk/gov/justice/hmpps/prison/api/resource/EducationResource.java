package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.Education;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.service.OffenderEducationService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "education")
@RequestMapping(value = "${api.base.path}/education", produces = "application/json")
public class EducationResource {

    private final OffenderEducationService offenderEducationService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "A list of offender educations.", description = "A list of offender educations.")
    @GetMapping("/prisoner/{offenderNo}")
    @PreAuthorize("hasAnyRole('GLOBAL_SEARCH', 'VIEW_PRISONER_DATA')")
    public Page<Education> getPrisonerEducations(
        @PathVariable(value = "offenderNo") @Parameter(description = "The offender NOMS number. NOMS numbers have the format:<b>G0364GX</b>") final String offenderNo,
        @RequestParam(value = "page", defaultValue = "0", required = false) @Parameter(description = "The page number of the paged results") final Integer page,
        @RequestParam(value = "size", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of results returned.") final Integer size
    ) {
        log.info("get prisoner educations for offenderNo: {}", offenderNo);
        return offenderEducationService.getOffenderEducations(offenderNo, PageRequest.of(page, size));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "A list of offender educations.", description = "A list of offender educations given a list of offender identifiers")
    @PostMapping("/prisoners")
    @PreAuthorize("hasAnyRole('GLOBAL_SEARCH', 'VIEW_PRISONER_DATA')")
    public List<Education> getPrisonerEducationsInBulk(
        @RequestBody @Parameter(description = "List of offender NOMS numbers. NOMS numbers have the format:<b>G0364GX</b>", required = true) final List<String> offenderIds
    ) {
        return offenderEducationService.getOffenderEducations(offenderIds);
    }
}
