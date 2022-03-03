package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = {"search-offenders"})
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
            @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List offenders by location (matching keywords).", notes = "Deprecated: Use <b>/locations/description/{locationPrefix}/inmates</b> instead. This API will be removed in a future release.", nickname = "searchForOffendersLocationAndKeyword")
    @Deprecated
    @GetMapping("/{locationPrefix}/{keywords}")
    public ResponseEntity<List<OffenderBooking>> searchForOffendersLocationAndKeyword(@PathVariable("locationPrefix") @ApiParam(value = "", required = true) final String locationPrefix, @PathVariable("keywords") @ApiParam(value = "", required = true) final String keywords, @RequestParam(value = "returnIep", required = false, defaultValue = "false") @ApiParam(value = "return IEP data", defaultValue = "false") final boolean returnIep, @RequestParam(value = "returnAlerts", required = false, defaultValue = "false") @ApiParam(value = "return Alert data", defaultValue = "false") final boolean returnAlerts, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of search-offender records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of search-offender records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b><<fieldsList>></b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
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
