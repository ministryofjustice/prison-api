package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = {"prisoners"})
@RequestMapping("${api.base.path}/prisoners")
@AllArgsConstructor
public class PrisonerStatusController {

    private final PrisonerInformationService service;

    @GetMapping("/{offenderNo}/full-status")
    @ApiOperation(value = "Status and core offender information", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerInformation.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "User not found.", response = ErrorResponse.class)})
    public PrisonerInformation getPrisonerInformationById(@ApiParam(name = "offenderNo", value = "Offender No (NOMS ID)", required = true, example = "A1234AA")
                                                         @PathVariable("offenderNo") final String offenderNo) {
        return service.getPrisonerInformationById(offenderNo);
    }

    /* NOTE: This is the old way of sending paging and returning information to existing prison-api ways of doing things **/

    @Deprecated
    @GetMapping("/at-location/{establishmentCode}")
    @ApiOperation(value = "List of prisoners at a prison establishment", notes = "Pagination In Headers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerInformation.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public ResponseEntity<List<PrisonerInformation>> getPrisonerDetailAtLocationOld(
                                         @ApiParam(value = "Establishment Code", required = true, example = "MDI") @PathVariable("establishmentCode") final String establishmentCode,
                                         @ApiParam(value = "Requested offset of first record in returned collection of prisoner records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                         @ApiParam(value = "Requested limit to number of prisoner records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                         @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingId, nomsId, cellLocation</b>", defaultValue = "bookingId") @RequestHeader(value = "Sort-Fields", defaultValue = "bookingId", required = false) String sortFields,
                                         @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder) {

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
    @ApiOperation(value = "List of prisoners at a prison establishment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrisonerInformation.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public Page<PrisonerInformation> getPrisonerDetailAtLocation(
            @ApiParam(value = "Establishment Code", required = true, example = "MDI") @PathVariable("establishmentCode") final String establishmentCode,
            @PageableDefault(sort = {"bookingId"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getPrisonerInformationByPrison(establishmentCode,  pageable);
    }
}
