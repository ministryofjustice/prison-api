package uk.gov.justice.hmpps.nomis.datacompliance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataComplianceReferralService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderImageUpdateService;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;

import java.time.LocalDateTime;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Slf4j
@RestController
@Api(tags = {"data-compliance"})
@RequestMapping("${api.base.path}/data-compliance")
@AllArgsConstructor
public class DataComplianceController {

    private final DataComplianceReferralService offenderDataComplianceService;
    private final OffenderImageUpdateService offenderImageUpdateService;

    @GetMapping("/offenders-with-images")
    @ApiOperation(value = "Get offenders with images captured in provided range")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @PreAuthorize("hasRole('SYSTEM_USER')")
    public Page<OffenderNumber> getOffendersWithImagesCapturedInRange(
            @ApiParam(value = "fromDateTime", required = true) @DateTimeFormat(iso = DATE_TIME) @RequestParam("fromDateTime") final LocalDateTime fromDate,
            @PageableDefault(direction = ASC, sort = "offender_id_display") final Pageable pageable) {
        return offenderImageUpdateService.getOffendersWithImagesCapturedAfter(fromDate, pageable);
    }
}
