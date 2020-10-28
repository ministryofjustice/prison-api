package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.HdcChecks;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalc;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.curfews.OffenderCurfewService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("${api.base.path}/offender-sentences")
public class OffenderSentenceResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;
    private final OffenderCurfewService offenderCurfewService;

    public OffenderSentenceResource(
            final AuthenticationFacade authenticationFacade,
            final BookingService bookingService,
            final OffenderCurfewService offenderCurfewService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
        this.offenderCurfewService = offenderCurfewService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderSentenceDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of offenders (with associated sentence detail).", nickname = "getOffenderSentences", notes = "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>")
    @GetMapping
    public List<OffenderSentenceDetail> getOffenderSentences(@RequestParam(value = "agencyId", required = false) @ApiParam("agency/prison to restrict results, if none provided current active caseload will be used, unless offenderNo list is specified") final String agencyId, @RequestParam(value = "offenderNo", required = false) @ApiParam("a list of offender numbers to search.") final List<String> offenderNos) {
        return bookingService.getOffenderSentencesSummary(
                agencyId,
                offenderNos);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "Sentence details for offenders who are candidates for Home Detention Curfew.", response = OffenderSentenceCalc.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of offenders eligible for HDC", notes = "Version 1", nickname = "getOffenderSentencesHomeDetentionCurfewCandidates")
    @GetMapping("/home-detention-curfew-candidates")
    public List<OffenderSentenceCalc> getOffenderSentencesHomeDetentionCurfewCandidates() {
        return offenderCurfewService.getHomeDetentionCurfewCandidates(authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "HDC information", response = HomeDetentionCurfew.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    @ApiOperation("Retrieve the current state of the latest Home Detention Curfew for a booking")
    @GetMapping("/booking/{bookingId}/home-detention-curfews/latest")
    public HomeDetentionCurfew getLatestHomeDetentionCurfew(@PathVariable("bookingId") Long bookingId) {
        return offenderCurfewService.getLatestHomeDetentionCurfew(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 204, message = "The checks passed flag was set"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    @ApiOperation("Set the HDC checks passed flag")
    @PutMapping("/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
    @ProxyUser
    public ResponseEntity<Void> setCurfewChecks(@PathVariable("bookingId") final Long bookingId, @RequestBody @javax.validation.Valid final HdcChecks hdcChecks) {
        offenderCurfewService.setHdcChecks(bookingId, hdcChecks);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
            @ApiResponse(code = 204, message = "The checks passed flag was cleared"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    @ApiOperation("Clear the HDC checks passed flag")
    @DeleteMapping("/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
    @ProxyUser
    public ResponseEntity<Void> clearCurfewChecks(@PathVariable("bookingId") Long bookingId) {
        offenderCurfewService.deleteHdcChecks(bookingId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
            @ApiResponse(code = 204, message = "The new approval status was set"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    @ApiOperation("Set the HDC approval status")
    @PutMapping("/booking/{bookingId}/home-detention-curfews/latest/approval-status")
    @ProxyUser
    public ResponseEntity<Void> setApprovalStatus(@PathVariable("bookingId") final Long bookingId, @RequestBody @javax.validation.Valid final ApprovalStatus approvalStatus) {

        offenderCurfewService.setApprovalStatus(bookingId, approvalStatus);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
            @ApiResponse(code = 204, message = "The new approval status was cleared"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    @ApiOperation("Clear the HDC approval status")
    @DeleteMapping("/booking/{bookingId}/home-detention-curfews/latest/approval-status")
    @ProxyUser
    public ResponseEntity<Void> clearApprovalStatus(@PathVariable("bookingId") Long bookingId) {
        offenderCurfewService.deleteApprovalStatus(bookingId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The list of offenders is returned.", response = OffenderSentenceDetail.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.", notes = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.", nickname = "postOffenderSentences")
    @PostMapping
    public List<OffenderSentenceDetail> postOffenderSentences(@RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos) {
        validateOffenderList(offenderNos);

        //no agency id filter required here as offenderNos will always be provided
        return bookingService.getOffenderSentencesSummary(null, offenderNos);

    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The list of offenders is returned.", response = OffenderSentenceDetail.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.", notes = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.", nickname = "postOffenderSentencesBookings")
    @PostMapping("/bookings")
    public List<OffenderSentenceDetail> postOffenderSentencesBookings(@RequestBody @ApiParam(value = "The required booking ids (mandatory)", required = true) final List<Long> bookingIds) {
        validateOffenderList(bookingIds);
        return bookingService.getBookingSentencesSummary(bookingIds);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "Sentence term details for a prisoner.", response = OffenderSentenceTerms.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Sentence term details for a prisoner", nickname = "getOffenderSentenceTerms")
    @GetMapping("/booking/{bookingId}/sentenceTerms")
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(@PathVariable("bookingId") @ApiParam(value = "The required booking id (mandatory)", required = true) final Long bookingId, @RequestParam(value = "filterBySentenceTermCodes", required = false) final List<String> filterBySentenceTermCodes) {
        return bookingService.getOffenderSentenceTerms(bookingId, filterBySentenceTermCodes);
    }

    private void validateOffenderList(final List<?> offenderList) {
        if (CollectionUtils.isEmpty(offenderList)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "List of Offender Ids must be provided");
        }
    }
}
