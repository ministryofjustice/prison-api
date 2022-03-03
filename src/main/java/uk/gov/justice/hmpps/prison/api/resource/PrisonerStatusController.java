package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInformation;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.PrisonerInformationService;

import java.util.List;

@Slf4j
@RestController
@Validated
@Tag(name = "prisoners")
@RequestMapping("${api.base.path}/prisoners")
@AllArgsConstructor
public class PrisonerStatusController {

    private final PrisonerInformationService service;

    @GetMapping("/{offenderNo}/full-status")
    @Operation(summary = "Status and core offender information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PrisonerInformation.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "User not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public PrisonerInformation getPrisonerInformationById(@Parameter(name = "offenderNo", description = "Offender No (NOMS ID)", required = true, example = "A1234AA")
                                                         @PathVariable("offenderNo") final String offenderNo) {
        return service.getPrisonerInformationById(offenderNo);
    }

    /* NOTE: This is the old way of sending paging and returning information to existing prison-api ways of doing things **/

    @Deprecated
    @GetMapping("/at-location/{establishmentCode}")
    @Operation(summary = "List of prisoners at a prison establishment", description = "Pagination In Headers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public ResponseEntity<List<PrisonerInformation>> getPrisonerDetailAtLocationOld(
                                         @Parameter(description = "Establishment Code", required = true, example = "MDI") @PathVariable("establishmentCode") final String establishmentCode,
                                         @Parameter(description = "Requested offset of first record in returned collection of prisoner records.") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                         @Parameter(description = "Requested limit to number of prisoner records returned.") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                         @Parameter(description = "Comma separated list of one or more of the following fields - <b>bookingId, nomsId, cellLocation</b>") @RequestHeader(value = "Sort-Fields", defaultValue = "bookingId", required = false) String sortFields,
                                         @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder) {

        final var prisonerInfo =  service.getPrisonerInformationByPrison(establishmentCode,
                PageRequest.of(sortFields, sortOrder, pageOffset, pageLimit));

        final var responseHeaders = new HttpHeaders();
        responseHeaders.set("Total-Records", String.valueOf(prisonerInfo.getTotalElements()));
        responseHeaders.set("Page-Offset",   String.valueOf(prisonerInfo.getPageable().getOffset()));
        responseHeaders.set("Page-Limit",    String.valueOf(prisonerInfo.getPageable().getPageSize()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(prisonerInfo.getContent());
    }

    /* NOTE: This is the new way of sending paging and returning information to match spring data patterns **/

    @GetMapping("/by-establishment/{establishmentCode}")
    @Operation(summary = "List of prisoners at a prison establishment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<PrisonerInformation> getPrisonerDetailAtLocation(
            @Parameter(description = "Establishment Code", required = true, example = "MDI") @PathVariable("establishmentCode") final String establishmentCode,
            @PageableDefault(sort = {"bookingId"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getPrisonerInformationByPrison(establishmentCode,  pageable);
    }
}
