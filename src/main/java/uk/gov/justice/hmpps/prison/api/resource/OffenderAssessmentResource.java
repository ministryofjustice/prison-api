package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.api.support.AssessmentStatusType;
import uk.gov.justice.hmpps.prison.api.support.CategoryInformationType;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.OffenderAssessmentService;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Implementation of Offender Assessments (/offender-assessments) endpoint.
 */
@RestController
@Api(tags = {"offender-assessments"})
@Validated
@RequestMapping("${api.base.path}/offender-assessments")
public class OffenderAssessmentResource {
    private final InmateService inmateService;
    private final OffenderAssessmentService offenderAssessmentService;

    public OffenderAssessmentResource(final InmateService inmateService, final OffenderAssessmentService offenderAssessmentService) {
        this.inmateService = inmateService;
        this.offenderAssessmentService = offenderAssessmentService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Assessment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Offender assessment detail for multiple offenders.", nickname = "getOffenderAssessmentsAssessmentCode")
    @GetMapping("/{assessmentCode}")
    public List<Assessment> getOffenderAssessmentsAssessmentCode(@PathVariable("assessmentCode") @ApiParam(value = "Assessment Type Code", required = true) final String assessmentCode, @RequestBody @NotEmpty @RequestParam("offenderNo") @ApiParam(value = "The required offender numbers", required = true) final List<String> offenderList, @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included", defaultValue = "true") final Boolean latestOnly, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only active assessments if true, otherwise inactive and pending assessments are included", defaultValue = "true") final Boolean activeOnly, @RequestParam(value = "mostRecentOnly", required = false) @ApiParam("Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") final Boolean mostRecentOnly) {

        return applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly, mostRecentOnly);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The assessment list is returned.", response = Assessment.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves Offender assessment details for multiple offenders - POST version to allow large offender lists.", nickname = "postOffenderAssessmentsAssessmentCode")
    @PostMapping("/{assessmentCode}")
    public List<Assessment> postOffenderAssessmentsAssessmentCode(@PathVariable("assessmentCode") @ApiParam(value = "Assessment Type Code", required = true) final String assessmentCode, @RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> offenderList, @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included", defaultValue = "true") final Boolean latestOnly, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only active assessments if true, otherwise inactive and pending assessments are included", defaultValue = "true") final Boolean activeOnly, @RequestParam(value = "mostRecentOnly", required = false) @ApiParam("Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") final Boolean mostRecentOnly) {
        validateOffenderList(offenderList);

        return applyDefaultsAndGetAssessmentsByCode(assessmentCode, offenderList, latestOnly, activeOnly, mostRecentOnly);
    }

    private List<Assessment> applyDefaultsAndGetAssessmentsByCode(final String assessmentCode, final List<String> offenderList, final Boolean latestOnly, final Boolean activeOnly, final Boolean mostRecentOnly) {
        final var latest = latestOnly == null || latestOnly;
        final var active = activeOnly == null || activeOnly;
        final var mostRecent = mostRecentOnly == null ? latest : mostRecentOnly; // backwards compatibility

        return inmateService.getInmatesAssessmentsByCode(offenderList, assessmentCode, latest, active, false, mostRecent);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The CSRA assessment list is returned, 1 per offender.", response = Assessment.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves Offender CRSAs for multiple offenders - POST version to allow large offender lists.", nickname = "postOffenderAssessmentsCsraList")
    @PostMapping("/csra/list")
    public List<Assessment> postOffenderAssessmentsCsraList(@RequestBody @NotEmpty @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> offenderList) {
        validateOffenderList(offenderList);
        return inmateService.getInmatesAssessmentsByCode(offenderList, null, true, true, true, true);
    }

    @ApiResponses({
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Retrieves details of a single CSRA assessment.", nickname = "getOffenderCsraAssessment")
    @GetMapping("/csra/{bookingId}/assessment/{assessmentSeq}")
    public AssessmentDetail getOffenderCsraAssessment(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender") final Long bookingId, @PathVariable("assessmentSeq") @ApiParam(value = "The assessment sequence number for the given offender booking") final Long assessmentSeq) {
        return offenderAssessmentService.getOffenderAssessment(bookingId, assessmentSeq);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Assessment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. no offender numbers provided.", response = ErrorResponse.class)})
    @ApiOperation("Returns assessment information on Offenders at a prison.")
    @GetMapping("/assessments")
    public List<Assessment> getAssessments(@RequestParam("offenderNo") @ApiParam(value = "The required offender numbers Ids (mandatory)", required = true) final List<String> offenderList, @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only assessments for the current sentence if true, otherwise assessments for all previous sentences are included", defaultValue = "true") final Boolean latestOnly, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only active assessments if true, otherwise inactive and pending assessments are included", defaultValue = "true") final Boolean activeOnly, @RequestParam(value = "mostRecentOnly", required = false) @ApiParam("Returns only the last assessment per sentence if true, otherwise all assessments for the booking are included") final Boolean mostRecentOnly) {
        final var latest = latestOnly == null || latestOnly;
        final var active = activeOnly == null || activeOnly;
        final var mostRecent = mostRecentOnly == null ? latest : mostRecentOnly; // backwards compatibility
        validateOffenderList(offenderList);
        return inmateService.getInmatesAssessmentsByCode(offenderList, null, latest, active, false, mostRecent);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = OffenderCategorise.class, responseContainer = "List")})
    @ApiOperation("Returns category information on Offenders at a prison.")
    @GetMapping("/category/{agencyId}")
    public List<OffenderCategorise> getOffenderCategorisations(@PathVariable("agencyId") @ApiParam(value = "Prison id", required = true) final String agencyId, @NotNull(message = "Categorisation type must not be null") @RequestParam("type") @ApiParam(value = "Indicates which type of category information is required." +
            "<li>UNCATEGORISED: Offenders who need to be categorised,</li>" +
            "<li>CATEGORISED: Offenders who have an approved categorisation,</li>" +
            "<li>RECATEGORISATIONS: Offenders who will soon require recategorisation</li>", required = true) final String type, @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("For type CATEGORISED: The past date from which categorisations are returned.<br />" +
            "For type RECATEGORISATIONS: the future cutoff date: list includes all prisoners who require re-categorisation on or before this date.<br />" +
            "For type UNCATEGORISED: Ignored; do not set this parameter.") final LocalDate date) {
        final CategoryInformationType enumType;
        try {
            enumType = CategoryInformationType.valueOf(type);
        } catch (final IllegalArgumentException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Categorisation type is invalid: " + type);
        }
        return inmateService.getCategory(agencyId, enumType, date);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The list of offenders with categorisation details is returned if categorisation record exists and their create agency is in the caseload", response = OffenderCategorise.class, responseContainer = "List")})
    @ApiOperation(value = "Returns Categorisation details for supplied Offenders - POST version to allow large offender lists.", notes = "Categorisation details for supplied Offenders where agencyId is their create agency and is in the caseload")
    @PostMapping("/category/{agencyId}")
    public List<OffenderCategorise> getOffenderCategorisations(@PathVariable("agencyId") @ApiParam(value = "Prison id", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required booking Ids (mandatory)", required = true) final Set<Long> bookingIds, @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @ApiParam(value = "Only get the latest category for each booking", defaultValue = "true") final Boolean latestOnly) {
        final var latest = latestOnly == null || latestOnly;
        return inmateService.getOffenderCategorisations(agencyId, bookingIds, latest);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The list of offenders with categorisation details is returned if categorisation record exists", response = OffenderCategorise.class, responseContainer = "List")})
    @ApiOperation(value = "Returns Categorisation details for supplied Offenders - POST version to allow large offender lists.", notes = "Categorisation details for all supplied Offenders using SYSTEM access")
    @PostMapping("/category")
    public List<OffenderCategorise> getOffenderCategorisationsSystem(@RequestBody @ApiParam(value = "The required booking Ids (mandatory)", required = true) final Set<Long> bookingIds, @RequestParam(value = "latestOnly", required = false, defaultValue = "true") @ApiParam(value = "Only get the latest category for each booking", defaultValue = "true") final Boolean latestOnly) {
        final var latest = latestOnly == null || latestOnly;
        return inmateService.getOffenderCategorisationsSystem(bookingIds, latest);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class)})
    @ApiOperation(value = "Record new offender categorisation.", notes = "Create new categorisation record. The booking id and new sequence number is returned.")
    @PostMapping("/category/categorise")
    @ProxyUser
    public ResponseEntity<Map<String, Long>> createCategorisation(@javax.validation.Valid @RequestBody @ApiParam(value = "Categorisation details", required = true) final CategorisationDetail detail) {
        final var resultMap = inmateService.createCategorisation(detail.getBookingId(), detail);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultMap);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class)})
    @ApiOperation(value = "Update a pending offender categorisation.", notes = "This is intended for use by the categoriser to correct any problems with a pending (in-progress) categorisation." +
            " Fields left as null will be left unchanged")
    @PutMapping("/category/categorise")
    @ProxyUser
    public ResponseEntity<Void> updateCategorisation(@javax.validation.Valid @RequestBody @ApiParam(value = "Categorisation details", required = true) final CategorisationUpdateDetail detail){
        inmateService.updateCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Validation error - e.g. category does not exist.", response = ErrorResponse.class)})
    @ApiOperation(value = "Approve a pending offender categorisation.", notes = "Update categorisation record with approval.")
    @PutMapping("/category/approve")
    @ProxyUser
    public ResponseEntity<Void> approveCategorisation(@javax.validation.Valid @RequestBody @ApiParam(value = "Approval details", required = true) final CategoryApprovalDetail detail) {
        inmateService.approveCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Validation error - e.g. comment too long or committee code does not exist.", response = ErrorResponse.class)})
    @ApiOperation(value = "Reject a pending offender categorisation.", notes = "Update categorisation record with rejection.")
    @PutMapping("/category/reject")
    @ProxyUser
    public ResponseEntity<Void> rejectCategorisation(@javax.validation.Valid @RequestBody @ApiParam(value = "Rejection details", required = true) final CategoryRejectionDetail detail) {
        inmateService.rejectCategorisation(detail.getBookingId(), detail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. invalid status.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to update categorisations.", response = ErrorResponse.class)})
    @ApiOperation(value = "Set all active or pending (status A or P) categorisations inactive", notes = "This endpoint should only be used with edge case categorisations.")
    @PutMapping("/category/{bookingId}/inactive")
    @ProxyUser
    public ResponseEntity<Void> setCategorisationInactive(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") @ApiParam(value = "Indicates which categorisation statuses to set." +
            "<li>ACTIVE (default): set all active (i.e. approved) categorisations inactive,</li>" +
            "<li>PENDING: set all pending (i.e. awaiting approval) categorisations inactive,</li>", allowableValues = "ACTIVE,PENDING", defaultValue = "ACTIVE") final String status){

        final AssessmentStatusType enumType;
        try {
            enumType = StringUtils.isEmpty(status) ? null : AssessmentStatusType.valueOf(status);
        } catch (final IllegalArgumentException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Assessment status type is invalid: " + status);
        }

        inmateService.setCategorisationInactive(bookingId, enumType);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Active categorisation not found.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to update the categorisation.", response = ErrorResponse.class)})
    @ApiOperation(value = "Update the next review date on the latest active categorisation", notes = "Update categorisation record with new next review date.", nickname = "updateCategorisationNextReviewDate")
    @PutMapping("/category/{bookingId}/nextReviewDate/{nextReviewDate}")
    @ProxyUser
    public ResponseEntity<Void> updateCategorisationNextReviewDate(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable("nextReviewDate") @ApiParam(value = "The new next review date (in YYYY-MM-DD format)", required = true) final LocalDate nextReviewDate) {
        inmateService.updateCategorisationNextReviewDate(bookingId, nextReviewDate);
        return ResponseEntity.ok().build();
    }

    private void validateOffenderList(final List<?> offenderList) {
        if (CollectionUtils.isEmpty(offenderList)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "List of Offender Ids must be provided.");
        }
    }
}
