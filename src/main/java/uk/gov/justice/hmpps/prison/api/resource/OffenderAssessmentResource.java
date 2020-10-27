package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Api(tags = {"/offender-assessments"})
@SuppressWarnings("unused")
public interface OffenderAssessmentResource {

    @GetMapping("/{assessmentCode}")
    @ApiOperation(value = "Offender assessment detail for multiple offenders.", nickname = "getOffenderAssessmentsAssessmentCode")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Assessment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Assessment> getOffenderAssessmentsAssessmentCode(@ApiParam(value = "Assessment Type Code", required = true) @PathVariable("assessmentCode") String assessmentCode,
                                                          @ApiParam(value = "The required offender numbers", required = true) @RequestParam("offenderNo") @NotEmpty @RequestBody List<String> offenderNo,
                                                          @ApiParam(value = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included", defaultValue = "true") @RequestParam(value = "latestOnly", required = false, defaultValue = "true") Boolean latestOnly,
                                                          @ApiParam(value = "Returns only active assessments if true, otherwise inactive and pending assessments are included", defaultValue = "true") @RequestParam(value = "activeOnly", required = false, defaultValue = "true") Boolean activeOnly,
                                                          @ApiParam(value = "Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") @RequestParam(value = "mostRecentOnly", required = false) Boolean mostRecentOnly);

    @PostMapping("/{assessmentCode}")
    @ApiOperation(value = "Retrieves Offender assessment details for multiple offenders - POST version to allow large offender lists.", nickname = "postOffenderAssessmentsAssessmentCode")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The assessment list is returned.", response = Assessment.class, responseContainer = "List")})
    List<Assessment> postOffenderAssessmentsAssessmentCode(@ApiParam(value = "Assessment Type Code", required = true) @PathVariable("assessmentCode") String assessmentCode,
                                                           @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body,
                                                           @ApiParam(value = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included", defaultValue = "true") @RequestParam(value = "latestOnly", required = false, defaultValue = "true") Boolean latestOnly,
                                                           @ApiParam(value = "Returns only active assessments if true, otherwise inactive and pending assessments are included", defaultValue = "true") @RequestParam(value = "activeOnly", required = false, defaultValue = "true") Boolean activeOnly,
                                                           @ApiParam(value = "Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") @RequestParam(value = "mostRecentOnly", required = false) Boolean mostRecentOnly);

    @PostMapping("/csra/list")
    @ApiOperation(value = "Retrieves Offender CRSAs for multiple offenders - POST version to allow large offender lists.", nickname = "postOffenderAssessmentsCsraList")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The CSRA assessment list is returned, 1 per offender.", response = Assessment.class, responseContainer = "List")})
    List<Assessment> postOffenderAssessmentsCsraList(@ApiParam(value = "The required offender numbers (mandatory)", required = true) @NotEmpty @RequestBody List<String> body);

    @GetMapping("/assessments")
    @ApiOperation(value = "Returns assessment information on Offenders at a prison.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Assessment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. no offender numbers provided.", response = ErrorResponse.class)})
    List<Assessment> getAssessments(@ApiParam(value = "The required offender numbers Ids (mandatory)", required = true) @RequestParam(value = "offenderNo") final List<String> offenderList,
                                    @ApiParam(value = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included", defaultValue = "true") @RequestParam(value = "latestOnly", required = false, defaultValue = "true") Boolean latestOnly,
                                    @ApiParam(value = "Returns only active assessments if true, otherwise inactive and pending assessments are included", defaultValue = "true") @RequestParam(value = "activeOnly", required = false, defaultValue = "true") Boolean activeOnly,
                                    @ApiParam(value = "Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") @RequestParam(value = "mostRecentOnly", required = false) Boolean mostRecentOnly);

    @GetMapping("/category/{agencyId}")
    @ApiOperation(value = "Returns category information on Offenders at a prison.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderCategorise.class, responseContainer = "List")})
    List<OffenderCategorise> getOffenderCategorisations(@ApiParam(value = "Prison id", required = true) @PathVariable("agencyId") String agencyId,
                                                        @ApiParam(value = "Indicates which type of category information is required." +
                                                                "<li>UNCATEGORISED: Offenders who need to be categorised,</li>" +
                                                                "<li>CATEGORISED: Offenders who have an approved categorisation,</li>" +
                                                                "<li>RECATEGORISATIONS: Offenders who will soon require recategorisation</li>", required = true) @RequestParam("type") @NotNull(message = "Categorisation type must not be null") String type,
                                                        @ApiParam(value = "For type CATEGORISED: The past date from which categorisations are returned.<br />" +
                                                                "For type RECATEGORISATIONS: the future cutoff date: list includes all prisoners who require re-categorisation on or before this date.<br />" +
                                                                "For type UNCATEGORISED: Ignored; do not set this parameter.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "date", required = false) LocalDate date);

    @PostMapping("/category/{agencyId}")
    @ApiOperation(value = "Returns Categorisation details for supplied Offenders - POST version to allow large offender lists.",
            notes = "Categorisation details for supplied Offenders where agencyId is their create agency and is in the caseload")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of offenders with categorisation details is returned if categorisation record exists and their create agency is in the caseload", response = OffenderCategorise.class, responseContainer = "List")})
    List<OffenderCategorise> getOffenderCategorisations(@ApiParam(value = "Prison id", required = true) @PathVariable("agencyId") String agencyId,
                                                        @ApiParam(value = "The required booking Ids (mandatory)", required = true) @RequestBody Set<Long> bookingIds,
                                                        @ApiParam(value = "Only get the latest category for each booking", defaultValue = "true") @RequestParam(value = "latestOnly", required = false, defaultValue = "true") Boolean latestOnly);

    @PostMapping("/category")
    @ApiOperation(value = "Returns Categorisation details for supplied Offenders - POST version to allow large offender lists.",
            notes = "Categorisation details for all supplied Offenders using SYSTEM access")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of offenders with categorisation details is returned if categorisation record exists", response = OffenderCategorise.class, responseContainer = "List")})
    List<OffenderCategorise> getOffenderCategorisationsSystem(@ApiParam(value = "The required booking Ids (mandatory)", required = true) @RequestBody Set<Long> bookingIds,
                                                              @ApiParam(value = "Only get the latest category for each booking", defaultValue = "true") @RequestParam(value = "latestOnly", required = false, defaultValue = "true") Boolean latestOnly);

    @PostMapping("/category/categorise")
    @ApiOperation(value = "Record new offender categorisation.", notes = "Create new categorisation record. The booking id and new sequence number is returned.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class)})
    ResponseEntity<Map<String, Long>> createCategorisation(@ApiParam(value = "Categorisation details", required = true) @RequestBody @Valid CategorisationDetail detail);

    @PutMapping("/category/categorise")
    @ApiOperation(value = "Update a pending offender categorisation.",
            notes = "This is intended for use by the categoriser to correct any problems with a pending (in-progress) categorisation." +
                    " Fields left as null will be left unchanged")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class)})
    ResponseEntity<Void> updateCategorisation(@ApiParam(value = "Categorisation details", required = true) @RequestBody @Valid CategorisationUpdateDetail detail);

    @PutMapping("/category/approve")
    @ApiOperation(value = "Approve a pending offender categorisation.", notes = "Update categorisation record with approval.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Validation error - e.g. category does not exist.", response = ErrorResponse.class)})
    ResponseEntity<Void> approveCategorisation(@ApiParam(value = "Approval details", required = true) @RequestBody @Valid CategoryApprovalDetail detail);

    @PutMapping("/category/reject")
    @ApiOperation(value = "Reject a pending offender categorisation.", notes = "Update categorisation record with rejection.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Validation error - e.g. comment too long or committee code does not exist.", response = ErrorResponse.class)})
    ResponseEntity<Void> rejectCategorisation(@ApiParam(value = "Rejection details", required = true) @RequestBody @Valid CategoryRejectionDetail detail);

    @PutMapping("/category/{bookingId}/inactive")
    @ApiOperation(value = "Set all active or pending (status A or P) categorisations inactive", notes = "This endpoint should only be used with edge case categorisations.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. invalid status.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to update categorisations.", response = ErrorResponse.class)})
    ResponseEntity<Void> setCategorisationInactive(
            @ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
            @ApiParam(value = "Indicates which categorisation statuses to set." +
                    "<li>ACTIVE (default): set all active (i.e. approved) categorisations inactive,</li>" +
                    "<li>PENDING: set all pending (i.e. awaiting approval) categorisations inactive,</li>", allowableValues = "ACTIVE,PENDING", defaultValue = "ACTIVE") @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status);

    @PutMapping("/category/{bookingId}/nextReviewDate/{nextReviewDate}")
    @ApiOperation(value = "Update the next review date on the latest active categorisation", notes = "Update categorisation record with new next review date.", nickname = "updateCategorisationNextReviewDate")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Active categorisation not found.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to update the categorisation.", response = ErrorResponse.class)})
    ResponseEntity<Void> updateCategorisationNextReviewDate(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                                            @ApiParam(value = "The new next review date (in YYYY-MM-DD format)", required = true) @PathVariable("nextReviewDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextReviewDate);

}
