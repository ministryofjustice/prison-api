package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = {"offences"})
@RequestMapping("${api.base.path}/offences")
@Validated
@AllArgsConstructor
public class OffenceResource {

    private final OffenceService service;

    @GetMapping()
    @ApiOperation(value = "Paged List of active offences")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public Page<OffenceDto> getActiveOffences(
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getOffences(true, pageable);
    }

    @GetMapping("/all")
    @ApiOperation(value = "Paged List of all offences")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public Page<OffenceDto> getOffences(
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getOffences(false, pageable);
    }

    @GetMapping("/ho-code")
    @ApiOperation(value = "Paged List of offences by HO Code")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public Page<OffenceDto> getOffencesByHoCode(
        @ApiParam(value = "HO Code", required = true, example = "825/99") @RequestParam("code") @NotBlank final String code,
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findByHoCode(code, pageable);
    }

    @GetMapping("/statute")
    @ApiOperation(value = "Paged List of offences by Statute")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public Page<OffenceDto> getOffencesByStatute(
        @ApiParam(value = "Statute Code", required = true, example = "RR84") @RequestParam("code") @NotBlank final String code,
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findByStatute(code, pageable);
    }

    @GetMapping("/search")
    @ApiOperation(value = "Paged List of offences matching offence description")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    public Page<OffenceDto> getOffencesByDescription(
        @ApiParam(value = "Search text of the offence", required = true, example = "RR84") @RequestParam("searchText") @NotBlank final String searchText,
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findOffences(searchText, pageable);
    }
}
