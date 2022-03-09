package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.SearchOffenderService;
import uk.gov.justice.hmpps.prison.service.support.SearchOffenderRequest;

import java.util.List;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

@RestController
@Tag(name = "search-offenders")
@Validated
@RequestMapping("${api.base.path}/search-offenders")
public class SearchOffenderResource {
    private final AuthenticationFacade authenticationFacade;
    private final SearchOffenderService searchOffenderService;

    public SearchOffenderResource(final AuthenticationFacade authenticationFacade, final SearchOffenderService searchOffenderService) {
        this.authenticationFacade = authenticationFacade;
        this.searchOffenderService = searchOffenderService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List offenders by location (matching keywords).", description = "Deprecated: Use <b>/locations/description/{locationPrefix}/inmates</b> instead. This API will be removed in a future release.")
    @Deprecated
    @GetMapping("/{locationPrefix}/{keywords}")
    public ResponseEntity<List<OffenderBooking>> searchForOffendersLocationAndKeyword(@PathVariable("locationPrefix") @Parameter(description = "", required = true) final String locationPrefix, @PathVariable("keywords") @Parameter(description = "", required = true) final String keywords, @RequestParam(value = "returnIep", required = false, defaultValue = "false") @Parameter(description = "return IEP data") final boolean returnIep, @RequestParam(value = "returnAlerts", required = false, defaultValue = "false") @Parameter(description = "return Alert data") final boolean returnAlerts, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of search-offender records.") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of search-offender records returned.") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b><<fieldsList>></b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        final var request = SearchOffenderRequest.builder()
                .username(authenticationFacade.getCurrentUsername())
                .keywords(keywords)
                .locationPrefix(locationPrefix)
                .returnAlerts(returnAlerts)
                .returnIep(returnIep)
                .orderBy(sortFields)
                .order(sortOrder)
                .offset(nvl(pageOffset, 0L))
                .limit(nvl(pageLimit, 10L))
                .build();

        final var offendersPaged = searchOffenderService.findOffenders(request);

        final var responseHeaders = new HttpHeaders();
        responseHeaders.set("Total-Records", String.valueOf(offendersPaged.getTotalRecords()));
        responseHeaders.set("Page-Offset",   String.valueOf(offendersPaged.getPageOffset()));
        responseHeaders.set("Page-Limit",    String.valueOf(offendersPaged.getPageLimit()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(offendersPaged.getItems());
    }
}
