package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"/offender-sentences"})
@SuppressWarnings("unused")
public interface OffenderSentenceResource {

    @GetMapping
    @ApiOperation(value = "List of offenders (with associated sentence detail).", nickname = "getOffenderSentences",
            notes = "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderSentenceDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenderSentenceDetail> getOffenderSentences(@ApiParam(value = "agency/prison to restrict results, if none provided current active caseload will be used, unless offenderNo list is specified") @RequestParam("agencyId") String agencyId,
                                                      @ApiParam(value = "a list of offender numbers to search.") @RequestParam("offenderNo") List<String> offenderNo);

    @GetMapping("/home-detention-curfew-candidates")


    @ApiOperation(value = "List of offenders Eligibile for HDC", notes = "Version 1", nickname = "getOffenderSentencesHomeDetentionCurfewCandidates")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Sentence details for offenders who are candidates for Home Detention Curfew.", response = OffenderSentenceCalculation.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenderSentenceCalc> getOffenderSentencesHomeDetentionCurfewCandidates();

    @GetMapping("/booking/{bookingId}/home-detention-curfews/latest")

    @ApiOperation(value = "Retrieve the current state of the latest Home Detention Curfew for a booking")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "HDC information", response = HomeDetentionCurfew.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    HomeDetentionCurfew getLatestHomeDetentionCurfew(@PathVariable("bookingId") Long bookingId);

    @PutMapping("/booking/{bookingId}/home-detention-curfews/latest/checks-passed")

    @ApiOperation(value = "Set the HDC checks passed flag")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The checks passed flag was set"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    ResponseEntity<Void> setCurfewChecks(@PathVariable("bookingId") Long bookingId, HdcChecks hdcChecks);

    @DeleteMapping("/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
    @ApiOperation(value = "Clear the HDC checks passed flag")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The checks passed flag was cleared"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    ResponseEntity<Void> clearCurfewChecks(@PathVariable("bookingId") Long bookingId);

    @PutMapping("/booking/{bookingId}/home-detention-curfews/latest/approval-status")

    @ApiOperation(value = "Set the HDC approval status")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The new approval status was set"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    ResponseEntity<Void> setApprovalStatus(@PathVariable("bookingId") Long bookingId, ApprovalStatus approvalStatus);

    @DeleteMapping("/booking/{bookingId}/home-detention-curfews/latest/approval-status")
    @ApiOperation(value = "Clear the HDC approval status")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The new approval status was cleared"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    ResponseEntity<Void> clearApprovalStatus(@PathVariable("bookingId") Long bookingId);

    @PostMapping
    @ApiOperation(value = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.", notes = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.", nickname = "postOffenderSentences")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of offenders is returned.", response = OffenderSentenceDetail.class, responseContainer = "List")})
    List<OffenderSentenceDetail> postOffenderSentences(@ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body);

    @PostMapping("/bookings")
    @ApiOperation(value = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.", notes = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.", nickname = "postOffenderSentencesBookings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of offenders is returned.", response = OffenderSentenceDetail.class, responseContainer = "List")})
    List<OffenderSentenceDetail> postOffenderSentencesBookings(@ApiParam(value = "The required booking ids (mandatory)", required = true) List<Long> body);

    @GetMapping("/booking/{bookingId}/sentenceTerms")
    @ApiOperation(value = "Sentence term details for a prisoner", nickname = "getOffenderSentenceTerms")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Sentence term details for a prisoner.", response = OffenderSentenceTerms.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenderSentenceTerms> getOffenderSentenceTerms(@ApiParam(value = "The required booking id (mandatory)", required = true) @PathVariable("bookingId") Long bookingId);

}
