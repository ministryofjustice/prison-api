package net.syscon.elite.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ReferenceCodeInfo;
import net.syscon.elite.api.support.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Api(tags = {"/reference-domains"})
public interface ReferenceDomainResource {

    @GetMapping("/alertTypes")
    @ApiOperation(value = "List of alert types (with alert codes).", notes = "List of alert types (with alert codes).", nickname = "getAlertTypes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<ReferenceCode>> getAlertTypes(@ApiParam(value = "Requested offset of first record in returned collection of alertType records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                      @ApiParam(value = "Requested limit to number of alertType records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>code, description</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/caseNoteSources")
    @ApiOperation(value = "List of case note source codes.", notes = "List of case note source codes.", nickname = "getCaseNoteSources")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<ReferenceCode>> getCaseNoteSources(@ApiParam(value = "Requested offset of first record in returned collection of caseNoteSource records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                  @ApiParam(value = "Requested limit to number of caseNoteSource records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>code, description</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/caseNoteTypes")
    @ApiOperation(value = "List of all used case note types (with sub-types).", notes = "List of all used case note types (with sub-types).", nickname = "getCaseNoteTypes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ReferenceCode> getCaseNoteTypes();

    @GetMapping("/domains/{domain}")
    @ApiOperation(value = "List of reference codes for reference domain.", notes = "List of reference codes for reference domain.", nickname = "getReferenceCodesByDomain")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<ReferenceCode>> getReferenceCodesByDomain(@ApiParam(value = "The domain identifier/name.", required = true) @PathVariable("domain") String domain,
                                                                @ApiParam(value = "Specify whether or not to return reference codes with their associated sub-codes.", defaultValue = "false") @RequestParam(value = "withSubCodes", required = false, defaultValue = "false") boolean withSubCodes,
                                                                @ApiParam(value = "Requested offset of first record in returned collection of domain records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                @ApiParam(value = "Requested limit to number of domain records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                                @ApiParam(value = "Comma separated list of one or more of the following fields - <b>code, description</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                                @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/domains/{domain}/codes/{code}")
    @ApiOperation(value = "Reference code detail for reference domain and code (with sub-codes).", notes = "Reference code detail for reference domain and code (with sub-codes).", nickname = "getReferenceCodeByDomainAndCode")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ReferenceCode getReferenceCodeByDomainAndCode(@ApiParam(value = "The domain identifier/name.", required = true) @PathVariable("domain") String domain,
                                                                            @ApiParam(value = "The reference code.", required = true) @PathVariable("code") String code,
                                                                            @ApiParam(value = "Specify whether or not to return the reference code with its associated sub-codes.", defaultValue = "false") @RequestParam(value = "withSubCodes", required = false, defaultValue = "false") boolean withSubCodes);


    @PostMapping("/domains/{domain}/codes/{code}")
    @ApiOperation(value = "Creates a reference code")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = ReferenceCode.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ReferenceCode createReferenceCode(@ApiParam(value = "The domain identifier/name.", required = true) @PathVariable("domain") @NotNull @Size(max = 12) String domain,
                                      @ApiParam(value = "The reference code.", required = true) @PathVariable("code") @NotNull @Size(max = 12) String code,
                                      @ApiParam(value = "Reference Information", required = true) @NotNull @Valid @RequestBody ReferenceCodeInfo referenceData);

    @PutMapping("/domains/{domain}/codes/{code}")
    @ApiOperation(value = "Updates a reference code")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated", response = ReferenceCode.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ReferenceCode updateReferenceCode(@ApiParam(value = "The domain identifier/name.", required = true) @PathVariable("domain") @NotNull @Size(max = 12) String domain,
                                      @ApiParam(value = "The reference code.", required = true) @PathVariable("code") @NotNull @Size(max = 12) String code,
                                      @ApiParam(value = "Reference Information", required = true) @RequestBody @NotNull @Valid ReferenceCodeInfo referenceData);

    @GetMapping("/scheduleReasons")
    @ApiOperation(value = "Get possible reason codes for created event.", notes = "Get possible reason codes for created event.", nickname = "getScheduleReasons")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReferenceCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ReferenceCode> getScheduleReasons(@ApiParam(value = "Specify event type.", required = true) @RequestParam("eventType") String eventType);

}
