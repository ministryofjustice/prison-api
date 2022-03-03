package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenceDto;
import uk.gov.justice.hmpps.prison.service.reference.OffenceService;

import javax.validation.constraints.NotBlank;


@RestController
@Tag(name = "offences")
@RequestMapping("${api.base.path}/offences")
@Validated
@AllArgsConstructor
public class OffenceResource {

    private final OffenceService service;

    @GetMapping()
    @Operation(summary = "Paged List of active offences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getActiveOffences(
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getOffences(true, pageable);
    }

    @GetMapping("/all")
    @Operation(summary = "Paged List of all offences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffences(
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getOffences(false, pageable);
    }

    @GetMapping("/ho-code")
    @Operation(summary = "Paged List of offences by HO Code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffencesByHoCode(
        @Parameter(description = "HO Code", required = true, example = "825/99") @RequestParam("code") @NotBlank final String code,
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findByHoCode(code, pageable);
    }

    @GetMapping("/statute")
    @Operation(summary = "Paged List of offences by Statute")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffencesByStatute(
        @Parameter(description = "Statute Code", required = true, example = "RR84") @RequestParam("code") @NotBlank final String code,
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findByStatute(code, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Paged List of offences matching offence description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffencesByDescription(
        @Parameter(description = "Search text of the offence", required = true, example = "RR84") @RequestParam("searchText") @NotBlank final String searchText,
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findOffences(searchText, pageable);
    }
}
